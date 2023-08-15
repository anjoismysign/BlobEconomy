package us.mytheria.blobeconomy.director.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.bloblib.BlobLibAPI;
import us.mytheria.bloblib.BlobLibAssetAPI;
import us.mytheria.bloblib.entities.BlobSelector;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.entities.inventory.MetaBlobInventory;
import us.mytheria.bloblib.entities.inventory.MetaInventoryButton;
import us.mytheria.bloblib.itemstack.ItemStackBuilder;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WithdrawerUI {
    private static WithdrawerUI instance;
    private final EconomyManagerDirector director;
    private Map<String, ItemStack> currencyDisplay;
    private final Map<UUID, MetaBlobInventory> inventories;
    private String uiTitle;

    public static WithdrawerUI getInstance(EconomyManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            return new WithdrawerUI(director);
        } else
            return instance;
    }

    public static WithdrawerUI getInstance() {
        return getInstance(null);
    }

    private WithdrawerUI(EconomyManagerDirector director) {
        this.instance = this;
        this.director = director;
        this.inventories = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(new WithdrawerListener(), director.getPlugin());
        reload();
    }

    public void withdraw(Player player, Collection<Currency> collection) {
        new Withdrawer(player, collection);
    }

    @Nullable
    private BlobDepositor getDepositor(Player player) {
        BlobDepositor depositor = director.getDepositorManager().isWalletOwner(player.getUniqueId())
                .orElse(null);
        if (depositor == null)
            BlobLibAssetAPI.getMessage("Player.Not-Inside-Plugin-Cache")
                    .toCommandSender(Bukkit.getConsoleSender());
        return depositor;
    }

    public void reload() {
        uiTitle = BlobLibAssetAPI.getMetaBlobInventory("Withdraw") == null ? null :
                BlobLibAssetAPI.getMetaBlobInventory("Withdraw").getTitle();
        if (uiTitle == null)
            throw new NullPointerException("It seems that Withdraw.yml does no longer exist");
        currencyDisplay = new HashMap<>();
        director.getCurrencyDirector().getObjectManager().values()
                .forEach(currency -> {
                    String key = currency.getKey();
                    if (!currency.isTangible())
                        return;
                    ItemStack itemStack = currency.getTangibleShape(999999)
                            .shape().get(0);
                    ItemStackBuilder builder = ItemStackBuilder.build(itemStack);
                    builder.displayName(currency.getDisplayName());
                    builder.lore();
                    itemStack = builder.build();
                    currencyDisplay.put(key, itemStack);
                    return;
                });
    }

    private class WithdrawerListener implements Listener {
        @EventHandler
        public void clickHandle(InventoryClickEvent event) {
            Player player = (Player) event.getWhoClicked();
            MetaBlobInventory inventory = inventories.get(player);
            if (inventory == null)
                return;
            event.setCancelled(true);
            MetaInventoryButton close = inventory.getButton("Close");
            if (close == null)
                return;
            if (!close.containsSlot(event.getRawSlot()))
                return;
            player.closeInventory();
            inventories.remove(player.getUniqueId());
        }

        @EventHandler
        public void quitHandle(PlayerQuitEvent event) {
            inventories.remove(event.getPlayer().getUniqueId());
        }
    }

    private class Withdrawer {
        private final MetaBlobInventory inventory;

        private Withdrawer(Player player, Collection<Currency> collection) {
            inventory = BlobLibAssetAPI.getMetaBlobInventory("Withdraw");
            inventories.put(player.getUniqueId(), inventory);
            BlobSelector<Currency> playerSelector = BlobSelector.COLLECTION_INJECTION(player.getUniqueId(),
                    "Currency", collection);
            playerSelector.setItemsPerPage(playerSelector.getSlots("Currencies")
                    == null ? 1 : playerSelector.getSlots("Currencies").size());
            playerSelector.selectElement(player, currency -> {
                player.closeInventory();
                BlobLibAPI.addChatListener(player, 300, input -> {
                            try {
                                double amount = Double.parseDouble(input);
                                BigDecimal x = BigDecimal.valueOf(amount);
                                BlobDepositor depositor = getDepositor(player);
                                if (depositor == null)
                                    return;
                                depositor.withdrawTargetCurrency(x, currency);
                                inventories.remove(player.getUniqueId());
                            } catch (NumberFormatException ignored) {
                                BlobLibAssetAPI.getMessage("Builder.Number-Exception").handle(player);
                                inventories.remove(player.getUniqueId());
                            }
                        }, "Withdraw.Amount-Timeout",
                        "Withdraw.Amount");
            }, null, currency -> {
                ItemStack result = currencyDisplay.get(currency.getKey());
                if (result == null) {
                    inventories.remove(player.getUniqueId());
                    throw new NullPointerException("Currency display is not expected to be null!");
                }
                return result;
            });
        }
    }
}
