package us.mytheria.blobeconomy.entities;

import me.anjoismysign.anjo.entities.Result;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.BlobEconomyAPI;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.director.ui.WithdrawerUI;
import us.mytheria.blobeconomy.entities.tradeable.Tradeable;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobCrudable;
import us.mytheria.bloblib.entities.ObjectManager;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.entities.currency.Wallet;
import us.mytheria.bloblib.entities.currency.WalletOwner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlobDepositor implements WalletOwner {
    private final BlobCrudable crudable;
    private final Wallet wallet;
    private final EconomyManagerDirector director;
    private final String playerName;

    public BlobDepositor(BlobCrudable crudable, EconomyManagerDirector director) {
        this.crudable = crudable;
        this.director = director;
        wallet = deserializeWallet();
        Player player = getPlayer();
        playerName = player.getName();
    }

    @Override
    public BlobCrudable serializeAllAttributes() {
        serializeWallet();
        return crudable;
    }

    @Override
    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    @Override
    @NotNull
    public String getPlayerUniqueId() {
        Player player = getPlayer();
        if (player == null || !player.isOnline())
            throw new NullPointerException("Player is null");
        return player.getUniqueId().toString();
    }

    @Override
    public BlobCrudable blobCrudable() {
        return crudable;
    }

    @Override
    public Wallet getWallet() {
        return wallet;
    }

    public void trade(@NotNull BigDecimal bigDecimal,
                      @NotNull Currency from,
                      @NotNull Currency to) {
        Player player = getPlayer();
        double amount = bigDecimal.doubleValue();
        if (!has(from, amount)) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Withdraw.Insufficient-Balance", player)
                    .handle(player);
            return;
        }
        Tradeable fromTradeable = BlobEconomyAPI.getInstance().getTradeable(from.getKey());
        Tradeable toTradeable = BlobEconomyAPI.getInstance().getTradeable(to.getKey());
        if (fromTradeable == null)
            throw new NullPointerException("'fromTradeable' cannot be null!");
        if (toTradeable == null)
            throw new NullPointerException("'toTradeable' cannot be null!");
        double total = fromTradeable.trade(toTradeable, amount);
        withdraw(from, amount);
        deposit(to, total);
        BlobLibMessageAPI.getInstance()
                .getMessage("Withdraw.Successful", player)
                .modder()
                .replace("%display%", from.display(amount))
                .get()
                .handle(player);
    }

    /**
     * Will withdraw the amount of currency from the wallet.
     *
     * @param bigDecimal the amount to withdraw
     * @param currency   the currency to withdraw
     */
    public void withdrawTargetCurrency(@NotNull BigDecimal bigDecimal,
                                       @NotNull Currency currency) {
        double amount = bigDecimal.doubleValue();
        Player player = getPlayer();
        if (!has(currency, amount)) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Withdraw.Insufficient-Balance", player)
                    .handle(player);
            return;
        }
        Currency.TangibleShapeOperation operation = currency.getTangibleShape(amount);
        if (!operation.isValid()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Withdraw.Amount-Too-Small", player)
                    .handle(player);
            return;
        }
        if (operation.hasReminder()) {
            bigDecimal = bigDecimal.subtract(operation.reminder());
            amount = bigDecimal.doubleValue();
        }
        withdraw(currency, amount);
        operation.shape().forEach(itemStack -> player.getInventory().addItem(itemStack));
        BlobLibMessageAPI.getInstance()
                .getMessage("Withdraw.Successful", player)
                .modder()
                .replace("%display%", currency.display(amount))
                .get()
                .handle(player);
    }

    /**
     * Will make the player choose a currency to withdraw.
     */
    public void chooseAndWithdrawCurrency() {
        ObjectManager<Currency> objectManager = director.getCurrencyDirector()
                .getObjectManager();
        List<Currency> list = getWallet().keySet().stream()
                .map(objectManager::searchObject)
                .map(Result::toOptional)
                .flatMap(Optional::stream)
                .filter(Currency::isTangible)
                .collect(Collectors.toList());
        WithdrawerUI.getInstance().withdraw(getPlayer(), list);
    }

    public void trade(boolean isTrading) {
        Player player = getPlayer();
        if (player == null)
            return;
        director.getTradeableDirector().trade(player, isTrading);
    }
}
