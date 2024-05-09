package us.mytheria.blobeconomy.director.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobeconomy.BlobEconomyAPI;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.blobeconomy.entities.tradeable.Tradeable;
import us.mytheria.blobeconomy.entities.tradeable.TradeableOperator;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.api.BlobLibListenerAPI;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.itemstack.ItemStackBuilder;

import java.math.BigDecimal;
import java.util.List;

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

    public void from(Player player, List<Currency> list) {
        BlobLibInventoryAPI.getInstance().customSelector("Trade",
                player, "Currencies", "Currency",
                () -> list,
                currency -> {
                    List<Currency> currencies;
                    if (BlobEconomyAPI.getInstance().isFreeTraderCurrencyMarket())
                        currencies = BlobEconomyAPI.getInstance().getTradeableCurrencies();
                    else
                        currencies = list.stream()
                                .filter(c -> !c.equals(currency))
                                .toList();
                    to(player, currencies, currency);
                }, currency -> {
                    Tradeable tradeable = BlobEconomyAPI.getInstance().getTradeable(currency.getKey());
                    if (tradeable == null)
                        throw new NullPointerException("Tradeable is null");
                    return ItemStackBuilder.build(Material.PAPER)
                            .displayName("&f" + currency.getDisplayName(player))
                            .lore("&7" + TradeableOperator.DECIMAL_FORMAT.format(tradeable.operator().getRate()),
                                    tradeable.operator().displayChange(),
                                    " ",
                                    "&7Click to trade with")
                            .build();
                });
    }

    public void to(Player player,
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
                    return ItemStackBuilder.build(Material.PAPER)
                            .displayName("&f" + currency.getDisplayName(player))
                            .lore("&7" + TradeableOperator.DECIMAL_FORMAT.format(tradeable.operator().getRate()),
                                    tradeable.operator().displayChange(),
                                    " ",
                                    "&7Trading &f" + from.getDisplayName(player),
                                    "&7to &f" + currency.getDisplayName(player))
                            .build();
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
