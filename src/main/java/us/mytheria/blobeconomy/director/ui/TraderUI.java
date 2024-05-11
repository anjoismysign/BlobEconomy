package us.mytheria.blobeconomy.director.ui;

import me.anjoismysign.anjo.entities.Result;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobeconomy.BlobEconomyAPI;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.blobeconomy.entities.tradeable.Tradeable;
import us.mytheria.blobeconomy.entities.tradeable.TradeableOperator;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.api.BlobLibListenerAPI;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.ObjectManager;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.bloblib.itemstack.ItemStackModder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TraderUI {
    private static TraderUI instance;
    private final EconomyManagerDirector director;

    public static TraderUI getInstance(EconomyManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            return new TraderUI(director);
        } else
            return instance;
    }

    public static TraderUI getInstance() {
        return getInstance(null);
    }

    private TraderUI(EconomyManagerDirector director) {
        instance = this;
        this.director = director;
    }

    public void trade(Player player) {
        BlobDepositor blobDepositor = getDepositor(player);
        if (blobDepositor == null)
            return;
        ObjectManager<Currency> objectManager = director.getCurrencyDirector()
                .getObjectManager();
        List<Currency> list = blobDepositor.getWallet().keySet().stream()
                .map(objectManager::searchObject)
                .map(Result::toOptional)
                .flatMap(Optional::stream)
                .filter(currency -> BlobEconomyAPI.getInstance().getTradeable(currency.getKey()) != null)
                .collect(Collectors.toList());
        Bukkit.getScheduler().runTask(director.getPlugin(), () -> {
            blobDepositor.trade(true);
        });
        BlobLibInventoryAPI.getInstance().customSelector("Trade",
                player, "Currencies", "Currency",
                () -> list,
                currency -> {
                    trade(player);
                    List<Currency> currencies;
                    if (BlobEconomyAPI.getInstance().isFreeTraderCurrencyMarket())
                        currencies = BlobEconomyAPI.getInstance().getTradeableCurrencies();
                    else
                        currencies = list.stream()
                                .filter(c -> !c.equals(currency))
                                .toList();
                    to(player, currencies, currency);
                },
                currency -> {
                    Tradeable tradeable = BlobEconomyAPI.getInstance().getTradeable(currency.getKey());
                    if (tradeable == null)
                        throw new NullPointerException("Tradeable is null");
                    TranslatableItem translatableItem = TranslatableItem.by("BlobEconomy.Tradeable-From")
                            .localize(player);
                    ItemStack clone = translatableItem.getClone();
                    ItemStackModder.mod(clone)
                            .replace("%currency%", currency.getDisplayName(player))
                            .replace("%currentRate%", TradeableOperator.DECIMAL_FORMAT.format(tradeable.operator().getRate()))
                            .replace("%change%", tradeable.operator().displayChange());
                    return clone;
                },
                player1 -> {
                    BlobDepositor depositor = getDepositor(player1);
                    if (depositor == null) {
                        player1.closeInventory();
                        return;
                    }
                    depositor.trade(false);
                    player1.closeInventory();
                },
                player1 -> {
                    BlobDepositor depositor = getDepositor(player1);
                    if (depositor == null)
                        return;
                    depositor.trade(false);
                });
    }

    private void to(Player player,
                    List<Currency> list,
                    Currency from) {
        BlobLibInventoryAPI.getInstance().customSelector("Trade",
                player, "Currencies", "Currency",
                () -> list,
                currency -> {
                    player.closeInventory();
                    BlobLibListenerAPI.getInstance().addChatListener(player, 300, input -> {
                                try {
                                    double amount = Double.parseDouble(input);
                                    BigDecimal x = new BigDecimal(amount);
                                    BlobDepositor depositor = getDepositor(player);
                                    if (depositor == null)
                                        return;
                                    depositor.trade(false);
                                    depositor.trade(x, from, currency);
                                } catch (NumberFormatException ignored) {
                                    BlobLibMessageAPI.getInstance().getMessage("Builder.Number-Exception").handle(player);
                                }
                            }, "Withdraw.Amount-Timeout",
                            "Withdraw.Amount");
                }, currency -> {
                    Tradeable tradeable = BlobEconomyAPI.getInstance().getTradeable(currency.getKey());
                    if (tradeable == null)
                        throw new NullPointerException("Tradeable is null");
                    TranslatableItem translatableItem = TranslatableItem.by("BlobEconomy.Tradeable-To")
                            .localize(player);
                    ItemStack clone = translatableItem.getClone();
                    ItemStackModder.mod(clone)
                            .replace("%currency%", currency.getDisplayName(player))
                            .replace("%currentRate%", TradeableOperator.DECIMAL_FORMAT.format(tradeable.operator().getRate()))
                            .replace("%change%", tradeable.operator().displayChange())
                            .replace("%from%", from.getDisplayName(player));
                    return clone;
                }, player1 -> {
                    BlobDepositor depositor = getDepositor(player1);
                    if (depositor == null) {
                        player1.closeInventory();
                        return;
                    }
                    trade(player1);
                },
                player1 -> {
                    BlobDepositor depositor = getDepositor(player1);
                    if (depositor == null)
                        return;
                    depositor.trade(false);
                });
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
