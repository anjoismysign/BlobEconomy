package us.mytheria.blobeconomy.director.ui;

import me.anjoismysign.anjo.entities.Result;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobeconomy.BlobEconomyAPI;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.blobeconomy.entities.LockedTrading;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
                                BlobDepositor depositor = getDepositor(player);
                                if (depositor == null)
                                    return;
                                try {
                                    double amount = Double.parseDouble(input);
                                    BigDecimal bigDecimal = new BigDecimal(amount);
                                    depositor.trade(false);
                                    depositor.trade(bigDecimal, from, currency);
                                } catch (NumberFormatException ignored) {
                                    Set<String> allKeywords = director.getConfigManager().getWithdrawAllKeywords();
                                    Set<String> halfKeywords = director.getConfigManager().getWithdrawHalfKeywords();
                                    if (allKeywords.contains(input)) {
                                        double amount = depositor.getBalance(from.getKey());
                                        BigDecimal bigDecimal = new BigDecimal(amount);
                                        depositor.trade(false);
                                        depositor.trade(bigDecimal, from, currency);
                                    } else if (halfKeywords.contains(input)) {
                                        double amount = depositor.getBalance(from.getKey()) / 2;
                                        BigDecimal bigDecimal = new BigDecimal(amount);
                                        depositor.trade(false);
                                        depositor.trade(bigDecimal, from, currency);
                                    } else {
                                        BlobLibMessageAPI.getInstance()
                                                .getMessage("Builder.Number-Exception", player)
                                                .handle(player);
                                    }
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
