package io.github.anjoismysign.blobeconomy.domain;

import io.github.anjoismysign.anjo.entities.Result;
import io.github.anjoismysign.blobeconomy.BlobEconomyAPI;
import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.director.ui.WithdrawerUI;
import io.github.anjoismysign.blobeconomy.domain.tradeable.Tradeable;
import io.github.anjoismysign.blobeconomy.events.DepositorLoadEvent;
import io.github.anjoismysign.blobeconomy.events.DepositorTradeEvent;
import io.github.anjoismysign.blobeconomy.events.DepositorTradeFailEvent;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.currency.Currency;
import io.github.anjoismysign.bloblib.currency.Wallet;
import io.github.anjoismysign.bloblib.currency.WalletHolder;
import io.github.anjoismysign.bloblib.manager.ObjectManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlobDepositor implements WalletHolder {
    private final Wallet wallet = new Wallet();
    private final Wallet bankWallet = new Wallet();
    private final EconomyManagerDirector director;
    private final String playerName;

    public BlobDepositor(String playerName, EconomyManagerDirector director){
        this.playerName = playerName;
        this.director = director;
        DepositorLoadEvent event = new DepositorLoadEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    @NotNull
    public Player getPlayer(){
        return Objects.requireNonNull(Bukkit.getPlayerExact(getPlayerName()), "Player is not online!");
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
        if (amount < 0){
            BlobLibMessageAPI.getInstance().getMessage("Economy.Number-Exception")
                    .handle(player);
            return;
        }
        if (!getWallet().has(from.getKey(), amount)) {
            double remaining = amount - getWallet().balance(from.getKey());
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
        getWallet().subtract(from.getKey(), amount);
        getWallet().add(to.getKey(), total);
        DepositorTradeEvent event = new DepositorTradeEvent(this, amount, total, from.getKey(), to.getKey());
        Bukkit.getPluginManager().callEvent(event);
        BlobLibMessageAPI.getInstance()
                .getMessage("Withdraw.Successful", player)
                .modder()
                .replace("%display%", from.display(amount))
                .get()
                .handle(player); //this did run successfully
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
        if (!getWallet().has(currency.getKey(), amount)) {
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
        getWallet().subtract(currency.getKey(), amount);
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
        director.getTradeableDirector().trade(player, isTrading);
    }

}
