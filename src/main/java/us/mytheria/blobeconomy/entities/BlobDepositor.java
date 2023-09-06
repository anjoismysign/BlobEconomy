package us.mytheria.blobeconomy.entities;

import me.anjoismysign.anjo.entities.Result;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.director.ui.WithdrawerUI;
import us.mytheria.bloblib.BlobLibAssetAPI;
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
    private final String playerName, playerUniqueId;

    public BlobDepositor(BlobCrudable crudable, EconomyManagerDirector director) {
        this.crudable = crudable;
        this.director = director;
        wallet = deserializeWallet();
        Player player = getPlayer();
        playerName = player.getName();
        playerUniqueId = player.getUniqueId().toString();
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

    /**
     * Will withdraw the amount of currency from the wallet.
     *
     * @param bigDecimal the amount to withdraw
     * @param currency   the currency to withdraw
     */
    public void withdrawTargetCurrency(BigDecimal bigDecimal, Currency currency) {
        double amount = bigDecimal.doubleValue();
        Player player = getPlayer();
        if (!has(currency, amount)) {
            BlobLibAssetAPI.getMessage("Withdraw.Insufficient-Balance")
                    .handle(player);
            return;
        }
        Currency.TangibleShapeOperation operation = currency.getTangibleShape(amount);
        if (!operation.isValid()) {
            BlobLibAssetAPI.getMessage("Withdraw.Amount-Too-Small")
                    .handle(player);
            return;
        }
        if (operation.hasReminder()) {
            bigDecimal = bigDecimal.subtract(operation.reminder());
            amount = bigDecimal.doubleValue();
        }
        withdraw(currency, amount);
        operation.shape().forEach(itemStack -> player.getInventory().addItem(itemStack));
        BlobLibAssetAPI.getMessage("Withdraw.Successful")
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
}
