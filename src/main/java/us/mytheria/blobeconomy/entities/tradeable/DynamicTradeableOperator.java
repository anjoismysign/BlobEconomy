package us.mytheria.blobeconomy.entities.tradeable;

import me.anjoismysign.anjo.entities.Uber;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.BlobEconomyAPI;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public record DynamicTradeableOperator(@NotNull Uber<Double> rate,
                                       @NotNull String currency,
                                       double min,
                                       double max,
                                       int maxTries,
                                       double changeAmount,
                                       @NotNull Uber<Double> changePercentage)
        implements TradeableOperator {
    @NotNull
    public static DynamicTradeableOperator READ(@NotNull ConfigurationSection section,
                                                @NotNull String currency) {
        Objects.requireNonNull(section, "section");
        if (!section.isDouble("Min"))
            throw new ConfigurationFieldException("'Min' is not valid or set");
        if (!section.isDouble("Max"))
            throw new ConfigurationFieldException("'Max' is not valid or set");
        if (!section.isInt("MaxTries"))
            throw new ConfigurationFieldException("'MaxTries' is not valid or set");
        if (!section.isDouble("ChangeAmount"))
            throw new ConfigurationFieldException("'ChangeAmount' is not valid or set");
        double min = section.getDouble("Min");
        double max = section.getDouble("Max");
        double rate = (ThreadLocalRandom.current().nextDouble() * (max - min)) + min;
        int maxTries = section.getInt("MaxTries");
        double changeAmount = section.getDouble("ChangeAmount");
        double changePercentage = 0;
        return new DynamicTradeableOperator(new Uber<>(rate), currency, min, max,
                maxTries, changeAmount, new Uber<>(changePercentage));
    }

    @Override
    public double getRate() {
        return rate.thanks();
    }

    @Override
    public void update() {
        Currency currency = BlobEconomyAPI.getInstance().getCurrency(this.currency);
        if (currency == null)
            return;
        double value = getRate();
        int tries = new Random().nextInt(maxTries) + 1;

        double change = changeAmount * tries;

        int nxt = new Random().nextInt(2);
        if (nxt == 0) {
            double end = value + change;
            rate.talk(Math.min(end, max));
        } else {
            double end = value - change;
            rate.talk(Math.max(end, min));
        }

        double test = rate.thanks() / value * 100;
        if (test > 100) {
            changePercentage.talk(test - 100);
        }
        if (test == 100) {
            changePercentage.talk(0D);
        }
        if (test < 100) {
            changePercentage.talk(100 - test);
        }
    }

    @Override
    public double getChangePercentage() {
        return changePercentage.thanks();
    }
}
