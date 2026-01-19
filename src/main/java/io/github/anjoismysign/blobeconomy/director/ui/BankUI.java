package io.github.anjoismysign.blobeconomy.director.ui;

import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibListenerAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventory;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryButton;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryDataRegistry;
import io.github.anjoismysign.bloblib.middleman.itemstack.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankUI {
    private static BankUI instance;
    private final EconomyManagerDirector director;
    private final Map<UUID, BankSession> sessions;

    private record BankSession(Currency currency, boolean isDeposit) {}

    public static BankUI getInstance(EconomyManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            return new BankUI(director);
        } else
            return instance;
    }

    public static BankUI getInstance() {
        return getInstance(null);
    }

    private BankUI(EconomyManagerDirector director) {
        instance = this;
        this.director = director;
        this.sessions = new HashMap<>();
        reload();
    }

    /**
     * Opens the main Bank menu.
     */
    public void openBank(Player player) {
        BlobLibInventoryAPI.getInstance().trackInventory(player, "Bank").getInventory().open(player);
    }

    public void reload() {
        BlobLibInventoryAPI inventoryAPI = BlobLibInventoryAPI.getInstance();

        InventoryDataRegistry<InventoryButton> bankRegistry = inventoryAPI.getInventoryDataRegistry("Bank");
        bankRegistry.onClick("Deposit", event -> openCurrencySelector((Player) event.getWhoClicked(), true));
        bankRegistry.onClick("Withdraw", event -> openCurrencySelector((Player) event.getWhoClicked(), false));

        InventoryDataRegistry<InventoryButton> bankDepositRegistry = inventoryAPI.getInventoryDataRegistry("BankDeposit");
        setupAmountButtons(bankDepositRegistry);

        InventoryDataRegistry<InventoryButton> bankWithdrawRegistry = inventoryAPI.getInventoryDataRegistry("BankWithdraw");
        setupAmountButtons(bankWithdrawRegistry);
    }

    private void setupAmountButtons(InventoryDataRegistry<InventoryButton> registry) {
        registry.onClick("All", event -> processBankAction((Player) event.getWhoClicked(), 1.0));
        registry.onClick("Half", event -> processBankAction((Player) event.getWhoClicked(), 0.5));
        registry.onClick("One-Fifth", event -> processBankAction((Player) event.getWhoClicked(), 0.2));
        registry.onClick("Custom", event -> openCustomAmountPrompt((Player) event.getWhoClicked()));
    }

    private void openCurrencySelector(Player player, boolean isDeposit) {
        BlobLibInventoryAPI.getInstance().customSelector(
                "BankCurrency", player, "Currencies", "Currency",
                () -> director.getCurrencyDirector().getObjectManager().values().stream().toList(),
                currency -> openAmountUI(player, currency, isDeposit),
                currency -> ItemStackBuilder
                        .build(Material.PAPER)
                        .itemName(currency.getDisplayName(player))
                        .lore(isDeposit ? "&7Click to deposit" : "&7Click to withdraw")
                        .build(),
                this::openBank,
                null,
                null
        );
    }

    private void openAmountUI(Player player, Currency currency, boolean isDeposit) {
        String registryName = isDeposit ? "BankDeposit" : "BankWithdraw";
        sessions.put(player.getUniqueId(), new BankSession(currency, isDeposit));

        BlobDepositor depositor = getDepositor(player);
        if (depositor == null) return;

        double balance = isDeposit ?
                depositor.getWallet().getOrDefault(currency.getKey(), 0.0) :
                depositor.getBankWallet().getOrDefault(currency.getKey(), 0.0);

        BlobInventory inventory = BlobLibInventoryAPI.getInstance().trackInventory(player, registryName).getInventory();
        String balanceDisplay = currency.display(balance);

        inventory.modder("All", m -> m
                .replace("%balance%", balanceDisplay)
                .replace("%amount%", balanceDisplay));

        inventory.modder("Half", m -> m
                .replace("%balance%", balanceDisplay)
                .replace("%amount%", currency.display(balance * 0.5)));

        inventory.modder("One-Fifth", m -> m
                .replace("%balance%", balanceDisplay)
                .replace("%amount%", currency.display(balance * 0.2)));

        inventory.modder("Custom", m -> m
                .replace("%balance%", balanceDisplay));

        inventory.open(player);
    }

    private void processBankAction(Player player, double multiplier) {
        BankSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        BlobDepositor depositor = getDepositor(player);
        if (depositor == null) return;

        String key = session.currency().getKey();
        double currentBalance = session.isDeposit() ?
                depositor.getWallet().getOrDefault(key, 0.0) :
                depositor.getBankWallet().getOrDefault(key, 0.0);

        double amount = currentBalance * multiplier;
        executeTransaction(depositor, session.currency(), amount, session.isDeposit());
        player.closeInventory();
    }

    private void openCustomAmountPrompt(Player player) {
        BankSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        player.closeInventory();
        BlobLibListenerAPI.getInstance().addChatListener(player, 300, input -> {
            BlobDepositor depositor = getDepositor(player);
            if (depositor == null) return;

            try {
                double amount = Double.parseDouble(input);
                executeTransaction(depositor, session.currency(), amount, session.isDeposit());
                sessions.remove(player.getUniqueId());
            } catch (NumberFormatException e) {
                BlobLibMessageAPI.getInstance().getMessage("Builder.Number-Exception", player).handle(player);
            }
        }, "Withdraw.Amount-Timeout", "Withdraw.Amount");
    }

    private void executeTransaction(BlobDepositor depositor, Currency currency, double amount, boolean isDeposit) {
        if (amount <= 0) return;
        String key = currency.getKey();

        if (isDeposit) {
            if (depositor.getWallet().getOrDefault(key, 0.0) >= amount) {
                depositor.withdraw(key, amount);
                depositor.getBankWallet().put(key, depositor.getBankWallet().getOrDefault(key, 0.0) + amount);
                sendSuccessMessage(depositor.getPlayer(), currency, amount, "Deposit");
            } else {
                sendFailureMessage(depositor.getPlayer());
            }
        } else {
            if (depositor.getBankWallet().getOrDefault(key, 0.0) >= amount) {
                depositor.getBankWallet().put(key, depositor.getBankWallet().getOrDefault(key, 0.0) - amount);
                depositor.deposit(key, amount);
                sendSuccessMessage(depositor.getPlayer(), currency, amount, "Withdraw");
            } else {
                sendFailureMessage(depositor.getPlayer());
            }
        }
    }

    private void sendSuccessMessage(Player player, Currency currency, double amount, String type) {
        BlobLibMessageAPI.getInstance().getMessage(type+".Successful", player)
                .modder().replace("%display%", currency.display(amount)).get()
                .handle(player);
    }

    private void sendFailureMessage(Player player) {
        BlobLibMessageAPI.getInstance().getMessage("Withdraw.Insufficient-Balance", player).handle(player);
    }

    @Nullable
    private BlobDepositor getDepositor(Player player) {
        BlobDepositor depositor = director.getDepositorManager().isWalletOwner(player.getUniqueId())
                .orElse(null);
        if (depositor == null)
            BlobLibMessageAPI.getInstance().getMessage("Player.Not-Inside-Plugin-Cache")
                    .toCommandSender(Bukkit.getConsoleSender());
        return depositor;
    }
}