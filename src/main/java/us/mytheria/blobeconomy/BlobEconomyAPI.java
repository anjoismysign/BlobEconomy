package us.mytheria.blobeconomy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.tradeable.Tradeable;
import us.mytheria.bloblib.entities.currency.Currency;

import java.util.List;
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
