package io.github.anjoismysign.blobeconomy;

import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.blobeconomy.entities.tradeable.Tradeable;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.bloblib.entities.currency.Wallet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BlobEconomyAPI {
    private static BlobEconomyAPI instance;

    protected static BlobEconomyAPI getInstance(@NotNull EconomyManagerDirector director) {
        if (instance == null) {
            instance = new BlobEconomyAPI(director);
        }
        return instance;
    }

    public static BlobEconomyAPI getInstance() {
        return getInstance(null);
    }

    private BlobEconomyAPI(@NotNull EconomyManagerDirector director) {
        this.director = director;
    }

    private final EconomyManagerDirector director;

    @NotNull
    public Currency getDefaultCurrency(){
        return Objects.requireNonNull(director.getDepositorManager().getDefaultCurrency(), "There are no currencies");
    }

    @NotNull
    public Wallet getBankWallet(@NotNull Player player){
        BlobDepositor depositor = Objects.requireNonNull(director.getDepositorManager().isWalletOwner(player).orElse(null), "Player not in plugin cache!");
        return depositor.getBankWallet();
    }

    @NotNull
    public Collection<Currency> getAllCurrencies(){
        return director.getCurrencyDirector().getObjectManager().values();
    }

    @Nullable
    public Currency getCurrency(@NotNull String currency) {
        return director.getCurrencyDirector().getObjectManager()
                .getObject(currency);
    }

    @Nullable
    public Tradeable getTradeable(@NotNull String currency) {
        return director.getTradeableDirector().getObjectManager()
                .getObject(currency);
    }

    @NotNull
    public List<Currency> getTradeableCurrencies() {
        return director.getCurrencyDirector().getObjectManager()
                .values().stream()
                .filter(currency -> getTradeable(currency.getKey()) != null)
                .collect(Collectors.toList());
    }

    public boolean isFreeTraderCurrencyMarket() {
        return director.getConfigManager().isFreeTraderCurrencyMarket();
    }

}
