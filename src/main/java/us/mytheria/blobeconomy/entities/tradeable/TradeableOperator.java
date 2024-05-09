package us.mytheria.blobeconomy.entities.tradeable;

import java.text.DecimalFormat;

public interface TradeableOperator {
    static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    double getRate();

    void update();

    double getChangePercentage();

    default String displayChange() {
        if (!didChange())
            return "&7(0%)→";
        if (getChangePercentage() > 0.01)
            return "&a(" + DECIMAL_FORMAT.format(getChangePercentage()) + "%)↑";
        return "&c(" + DECIMAL_FORMAT.format(getChangePercentage()) + "%)↓";
    }

    default boolean didChange() {
        return Math.abs(getChangePercentage()) > 0.01;
    }
}
