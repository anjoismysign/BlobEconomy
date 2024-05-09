package us.mytheria.blobeconomy.entities.tradeable;

import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.BlobObject;

import java.io.File;
import java.util.Objects;

/**
 * Represents a tradeable currency.
 *
 * @param currency The currency.
 * @param operator The default currency operator.
 */
public record Tradeable(@NotNull String currency,
                        @NotNull TradeableOperator operator)
        implements BlobObject {
    @Override
    public String getKey() {
        return currency;
    }

    @Override
    public File saveToFile(File file) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Trades the currency of this Tradeable to another currency for a given amount.
     *
     * @param to     The Tradeable to trade to.
     * @param amount The amount to trade.
     * @return The amount of the traded currency.
     */
    public double trade(@NotNull Tradeable to,
                        double amount) {
        Objects.requireNonNull(to, "'to' cannot be null.");
        double fromRate = operator.getRate();
        double toRate = to.operator().getRate();
        return (amount * fromRate) / toRate;
    }
}
