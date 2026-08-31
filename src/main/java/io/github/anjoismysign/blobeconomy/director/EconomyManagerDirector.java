package io.github.anjoismysign.blobeconomy.director;

import io.github.anjoismysign.blobeconomy.BlobEconomy;
import io.github.anjoismysign.blobeconomy.director.commands.BankCommand;
import io.github.anjoismysign.blobeconomy.director.commands.BlobEconomyCommand;
import io.github.anjoismysign.blobeconomy.director.commands.BoycottCmd;
import io.github.anjoismysign.blobeconomy.director.commands.Deposit;
import io.github.anjoismysign.blobeconomy.director.commands.TraderCmd;
import io.github.anjoismysign.blobeconomy.director.commands.Withdraw;
import io.github.anjoismysign.blobeconomy.director.commands.WithdrawerCmd;
import io.github.anjoismysign.blobeconomy.director.manager.BlobDepositorManager;
import io.github.anjoismysign.blobeconomy.director.manager.EconomyConfigManager;
import io.github.anjoismysign.blobeconomy.director.manager.TradeableDirector;
import io.github.anjoismysign.blobeconomy.director.ui.BankUI;
import io.github.anjoismysign.blobeconomy.director.ui.TraderUI;
import io.github.anjoismysign.blobeconomy.director.ui.WithdrawerUI;
import io.github.anjoismysign.blobeconomy.domain.BlobDepositor;
import io.github.anjoismysign.blobeconomy.economy.BlobMultiEconomy;
import io.github.anjoismysign.bloblib.currency.Currency;
import io.github.anjoismysign.bloblib.currency.Wallet;
import io.github.anjoismysign.bloblib.manager.GenericManagerDirector;
import io.github.anjoismysign.bloblib.manager.ObjectDirector;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class EconomyManagerDirector extends GenericManagerDirector<BlobEconomy> {
    private static final Pattern pattern = Pattern.compile("^.");
    private WithdrawerUI withdrawerUI;
    private TraderUI traderUI;
    private BankUI bankUI;

    private final BlobDepositorManager depositorManager;

    public EconomyManagerDirector(BlobEconomy plugin) {
        super(plugin);
        getRealFileManager().unpackYamlFile("/currency", "default");
        registerBlobInventory("Trade","Trade-Amount","Bank","BankCurrency","BankDeposit","BankWithdraw");
        addManager("ConfigManager", new EconomyConfigManager(this));
        addCurrencyDirector("Currency");
        addManager("TradeableDirector", new TradeableDirector(this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> Withdraw.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(executorData -> Withdraw.tabCompleter(executorData, this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> Deposit.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(Deposit::tabCompleter);
        getCurrencyDirector().addNonAdminChildCommand(executorData -> WithdrawerCmd.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(WithdrawerCmd::tabCompleter);
        getCurrencyDirector().addAdminChildCommand(executorData -> BoycottCmd.command(executorData, this));
        getCurrencyDirector().addAdminChildTabCompleter(BoycottCmd::tabCompleter);
        getCurrencyDirector().addNonAdminChildCommand(executorData -> TraderCmd.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(TraderCmd::tabCompleter);
        depositorManager = new BlobDepositorManager(this);
        getDepositorManager().setNotEnoughEvent(notEnoughBalance -> {
            BlobDepositor blobDepositor = notEnoughBalance.owner();
            String currency = notEnoughBalance.currency();
            double missing = notEnoughBalance.missing();
            Wallet bankWallet = blobDepositor.getBankWallet();
            double current = bankWallet.get(currency);
            if (current < missing){
                return false;
            }
            blobDepositor.deposit(currency, missing);
            bankWallet.put(currency, current - missing);
            return true;
        });
        getCurrencyDirector().whenObjectManagerFilesLoad(manager -> {
            try {
                withdrawerUI = WithdrawerUI.getInstance(this);
                traderUI = TraderUI.getInstance(this);
                bankUI = BankUI.getInstance(this);
                Bukkit.getScheduler().runTask(getPlugin(), () -> {
                    if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                        BlobMultiEconomy.load(this);
                    }
                    instantiateBlobPHExpansion("economy", expansion ->
                            expansion.putStartsWith("", this::economyPlaceholder));
                    BankCommand.INSTANCE.load();
                    BlobEconomyCommand.INSTANCE.load();
                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void reload() {
        getConfigManager().reload();
        getCurrencyDirector().reload();
        getCurrencyDirector().whenReloaded(() ->
                getTradeableDirector().reload());
        bankUI.reload();
    }

    @Override
    public void unload() {
        getDepositorManager().unload();
    }

    @Override
    public void postWorld() {
        getTradeableDirector().postWorld();
    }

    @NotNull
    public final ObjectDirector<Currency> getCurrencyDirector() {
        return getCurrencyDirector("Currency");
    }

    @NotNull
    public final TradeableDirector getTradeableDirector() {
        return getManager("TradeableDirector", TradeableDirector.class);
    }

    @NotNull
    public final BlobDepositorManager getDepositorManager() {
        return depositorManager;
    }

    @NotNull
    public final EconomyConfigManager getConfigManager() {
        return getManager("ConfigManager", EconomyConfigManager.class);
    }

    @Nullable
    private String economyPlaceholder(@NotNull OfflinePlayer offlinePlayer, @NotNull String identifier) {
        String[] split = identifier.split("_");
        if (split.length != 2) {
            return null;
        }
        String currencyName = split[0];
        Set<String> customCrypto = getCurrencyDirector().getObjectManager().keys();
        if (!customCrypto.contains(currencyName)) {
            return "Invalid currency: " + currencyName;
        }
        Optional<BlobDepositor> optional = getDepositorManager().isWalletOwner(offlinePlayer.getUniqueId());
        if (optional.isEmpty()) {
            return "Invalid player: " + offlinePlayer.getName();
        }
        BlobDepositor walletOwner = optional.get();
        @Nullable Currency currency = getCurrencyDirector().getObjectManager().getObject(currencyName);
        if (currency == null){
            return "Not a currency: "+currencyName;
        }
        String subIdentifier = split[1];
        Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());
        switch (subIdentifier) {
            case "bankdisplay":
                Wallet bankWallet = walletOwner.getBankWallet();
                return currency.display(bankWallet.balance(currency.getKey()));
            case "display":
                return currency.display(walletOwner.getBalance(currency.getKey()));
            case "balance":
                return "" + walletOwner.getBalance(currency.getKey());
            case "displayName":
                if (player == null) {
                    return currency.getDisplayName();
                }
                return currency.getDisplayName(player);
            case "capitalizeDisplayName":
                String displayName;
                if (player == null) {
                    displayName = currency.getDisplayName();
                } else {
                    displayName = currency.getDisplayName(player);
                }
                displayName = pattern.matcher(displayName).replaceFirst(m -> m.group().toUpperCase(Locale.ROOT));
                return displayName;
            default:
                return "Invalid sub-identifier: " + subIdentifier;
        }
    }
}