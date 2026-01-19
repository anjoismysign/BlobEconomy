package io.github.anjoismysign.blobeconomy.director.ui;

import io.github.anjoismysign.anjo.entities.Result;
import io.github.anjoismysign.anjo.entities.Tuple3;
import io.github.anjoismysign.bloblib.middleman.itemstack.ItemStackModder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.blobeconomy.BlobEconomyAPI;
import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.blobeconomy.entities.LockedTrading;
import io.github.anjoismysign.blobeconomy.entities.tradeable.Tradeable;
import io.github.anjoismysign.blobeconomy.entities.tradeable.TradeableOperator;
import io.github.anjoismysign.blobeconomy.events.DepositorPreTradeEvent;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibListenerAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.ObjectManager;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventory;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TraderUI {
    private static TraderUI instance;
    private final EconomyManagerDirector director;
    private final Map<UUID, Tuple3<Currency, Currency, Double>> trading;

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
        this.trading = new HashMap<>();
    }

    public void trade(Player player) {
        BlobDepositor blobDepositor = getDepositor(player);
        if (blobDepositor == null)
            return;
        ObjectManager<Currency> objectManager = director.getCurrencyDirector()
                .getObjectManager();
        LockedTrading lockedTrading = director.getConfigManager().getLockedTrading();
        boolean isLocked = lockedTrading.isEnabled();
        Set<String> allowedCurrencies = lockedTrading.getAllowedCurrencies();
        List<Currency> list = blobDepositor.getWallet().keySet().stream()
                .map(objectManager::searchObject)
                .map(Result::toOptional)
                .flatMap(Optional::stream)
                .filter(currency -> BlobEconomyAPI.getInstance().getTradeable(currency.getKey()) != null)
                .filter(currency -> {
                    if (!isLocked || allowedCurrencies.size() != 1)
                        return true;
                    return !allowedCurrencies.contains(currency.getKey());
                })
                .collect(Collectors.toList());
        Bukkit.getScheduler().runTask(director.getPlugin(), () -> {
            if (!player.isConnected()){
                return;
            }
            blobDepositor.trade(true);
        });
        BlobLibInventoryAPI.getInstance().customSelector("Trade",
                player, "Currencies", "Currency",
                () -> list,
                currency -> {
                    trade(player);
                    Set<Currency> currencies;
                    if (BlobEconomyAPI.getInstance().isFreeTraderCurrencyMarket())
                        currencies = new HashSet<>(BlobEconomyAPI.getInstance().getTradeableCurrencies());
                    else {
                        currencies = list.stream()
                                .filter(c -> !c.equals(currency))
                                .collect(Collectors.toSet());
                        Set<Currency> finalCurrencies = currencies;
                        director.getConfigManager().getAlwaysTradableCurrencies()
                                .forEach(key -> {
                                    Currency found = objectManager.getObject(key);
                                    if (found == null)
                                        return;
                                    finalCurrencies.add(found);
                                });

                    }
                    if (isLocked) {
                        currencies = currencies.stream()
                                .filter(c -> allowedCurrencies.contains(c.getKey()))
                                .collect(Collectors.toSet());
                    }
                    to(player, currencies.stream().toList(), currency);
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
                },
                null);
    }

    private void to(Player player,
                    List<Currency> list,
                    Currency from) {
        BlobLibInventoryAPI.getInstance().customSelector("Trade",
                player, "Currencies", "Currency",
                () -> list,
                to -> {
                    BlobInventory blobInventory = BlobLibInventoryAPI.getInstance().trackInventory(player, "Trade-Amount")
                            .getInventory();
                    BlobDepositor depositor = getDepositor(player);
                    depositor.trade(false);
                    double balance = depositor.getBalance(from.getKey());
                    DepositorPreTradeEvent event = new DepositorPreTradeEvent(depositor, from, balance);
                    Bukkit.getPluginManager().callEvent(event);
                    balance = event.getBalance();
                    double finalBalance = balance;
                    Tuple3<Currency, Currency, Double> tuple = new Tuple3<>(from, to, finalBalance);
                    trading.put(player.getUniqueId(), tuple);
                    blobInventory.modder("All", itemStackModder -> {
                        itemStackModder.replace("%balance%", from.display(finalBalance))
                                .replace("%amount%", from.display(finalBalance));
                    });
                    blobInventory.modder("Half", itemStackModder -> {
                        itemStackModder.replace("%balance%", from.display(finalBalance))
                                .replace("%amount%", from.display(finalBalance * 0.5));
                    });
                    blobInventory.modder("One-Fifth", itemStackModder -> {
                        itemStackModder.replace("%balance%", from.display(finalBalance))
                                .replace("%amount%", from.display(finalBalance * 0.2));
                    });
                    blobInventory.open(player);
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
                },
                null);
    }

    public void trade(@NotNull Player player,
                      double multiplier) {
        Tuple3<Currency, Currency, Double> tuple = trading.get(player.getUniqueId());
        player.closeInventory();
        if (tuple == null)
            return;
        Currency from = tuple.first();
        Currency to = tuple.second();
        BlobDepositor depositor = getDepositor(player);
        if (depositor == null)
            return;
        double amount = tuple.third() * multiplier;
        BigDecimal bigDecimal = new BigDecimal(amount);
        depositor.trade(false);
        depositor.trade(bigDecimal, from, to);
    }

    public void tradeCustomAmount(@NotNull Player player) {
        Tuple3<Currency, Currency, Double> tuple = trading.get(player.getUniqueId());
        player.closeInventory();
        if (tuple == null)
            return;
        Currency from = tuple.first();
        Currency to = tuple.second();
        BlobLibListenerAPI.getInstance().addChatListener(player, 300, input -> {
                    BlobDepositor depositor = getDepositor(player);
                    if (depositor == null)
                        return;
                    try {
                        double amount = Double.parseDouble(input);
                        BigDecimal bigDecimal = new BigDecimal(amount);
                        depositor.trade(bigDecimal, from, to);
                    } catch (NumberFormatException ignored) {
                        Set<String> allKeywords = director.getConfigManager().getWithdrawAllKeywords();
                        Set<String> halfKeywords = director.getConfigManager().getWithdrawHalfKeywords();
                        if (allKeywords.contains(input)) {
                            double amount = depositor.getBalance(from.getKey());
                            BigDecimal bigDecimal = new BigDecimal(amount);
                            depositor.trade(false);
                            depositor.trade(bigDecimal, from, to);
                        } else if (halfKeywords.contains(input)) {
                            double amount = depositor.getBalance(from.getKey()) / 2;
                            BigDecimal bigDecimal = new BigDecimal(amount);
                            depositor.trade(false);
                            depositor.trade(bigDecimal, from, to);
                        } else {
                            BlobLibMessageAPI.getInstance()
                                    .getMessage("Builder.Number-Exception", player)
                                    .handle(player);
                        }
                    }
                }, "Withdraw.Amount-Timeout",
                "Withdraw.Amount");
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
