package us.mytheria.blobeconomy.director.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.api.BlobLibListenerAPI;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.itemstack.ItemStackBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WithdrawerUI {
    private static WithdrawerUI instance;
    private final EconomyManagerDirector director;
    private Map<String, ItemStack> currencyDisplay;

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
        instance = this;
        this.director = director;
        reload();
    }

    public void withdraw(Player player, List<Currency> list) {
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
                                    depositor.withdrawTargetCurrency(bigDecimal, currency);
                                } catch (NumberFormatException ignored) {
                                    Set<String> allKeywords = director.getConfigManager().getWithdrawAllKeywords();
                                    Set<String> halfKeywords = director.getConfigManager().getWithdrawHalfKeywords();
                                    if (allKeywords.contains(input)) {
                                        double amount = depositor.getBalance(currency.getKey());
                                        BigDecimal bigDecimal = new BigDecimal(amount);
                                        depositor.withdrawTargetCurrency(bigDecimal, currency);
                                    } else if (halfKeywords.contains(input)) {
                                        double amount = depositor.getBalance(currency.getKey()) / 2;
                                        BigDecimal bigDecimal = new BigDecimal(amount);
                                        depositor.withdrawTargetCurrency(bigDecimal, currency);
                                    } else {
                                        BlobLibMessageAPI.getInstance()
                                                .getMessage("Builder.Number-Exception", player)
                                                .handle(player);
                                    }
                                }
                            }, "Withdraw.Amount-Timeout",
                            "Withdraw.Amount");
                }, currency -> {
                    ItemStack result = currencyDisplay.get(currency.getKey());
                    if (result == null) {
                        throw new NullPointerException("Currency display is not expected to be null!");
                    }
                    return result;
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

    public void reload() {
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
}
