package io.github.anjoismysign.blobeconomy.entities;

import io.github.anjoismysign.anjo.entities.Result;
import io.github.anjoismysign.blobeconomy.BlobEconomyAPI;
import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.director.ui.WithdrawerUI;
import io.github.anjoismysign.blobeconomy.entities.tradeable.Tradeable;
import io.github.anjoismysign.blobeconomy.events.DepositorTradeFailEvent;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.BlobCrudable;
import io.github.anjoismysign.bloblib.entities.ObjectManager;
import io.github.anjoismysign.bloblib.entities.currency.BankWalletOwner;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.bloblib.entities.currency.Wallet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlobDepositor implements BankWalletOwner {
    private final BlobCrudable crudable;
    private final Wallet wallet;
    private final Wallet bankWallet;
    private final EconomyManagerDirector director;
    private final String playerName;

    public BlobDepositor(BlobCrudable crudable, EconomyManagerDirector director) {
        this.crudable = crudable;
        this.director = director;
        wallet = deserializeWallet("Wallet");
        bankWallet = deserializeWallet("BankWallet");
        Player player = getPlayer();
        playerName = player.getName();
    }

    @Override
    public BlobCrudable serializeAllAttributes() {
        serializeWallet(wallet,"Wallet");
        serializeWallet(bankWallet,"BankWallet");
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

    public Wallet getWallet() {
        return wallet;
    }

    public Wallet getBankWallet(){
        return bankWallet;
    }

    public void trade(@NotNull BigDecimal bigDecimal,
                      @NotNull Currency from,
                      @NotNull Currency to) {
        Player player = getPlayer();
        double amount = bigDecimal.doubleValue();
        if (!has(from.getKey(), amount)) {
            double remaining = amount - getBalance(from.getKey());
            DepositorTradeFailEvent event = new DepositorTradeFailEvent(this, from, remaining);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isFixed()) {
                trade(from, to, amount, player);
                return;
            }
            BlobLibMessageAPI.getInstance()
                    .getMessage("Withdraw.Insufficient-Balance", player)
                    .handle(player);
            return;
        }
        trade(from, to, amount, player);
    }

    private void trade(@NotNull Currency from,
                       @NotNull Currency to,
                       double amount,
                       @NotNull Player player) {
        Tradeable fromTradeable = BlobEconomyAPI.getInstance().getTradeable(from.getKey());
        Tradeable toTradeable = BlobEconomyAPI.getInstance().getTradeable(to.getKey());
        if (fromTradeable == null)
            throw new NullPointerException("'fromTradeable' cannot be null!");
        if (toTradeable == null)
            throw new NullPointerException("'toTradeable' cannot be null!");
        double total = fromTradeable.trade(toTradeable, amount);
        withdraw(from.getKey(), amount);
        deposit(to.getKey(), total);
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
        if (!has(currency.getKey(), amount)) {
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
        withdraw(currency.getKey(), amount);
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
