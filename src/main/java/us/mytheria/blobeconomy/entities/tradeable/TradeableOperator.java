package us.mytheria.blobeconomy.entities.tradeable;

import us.mytheria.bloblib.utilities.TextColor;

import java.text.DecimalFormat;

public interface TradeableOperator {
    static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    double getRate();

    void update();

    double getChangePercentage();

    default String displayChange() {
        if (!didChange())
            return TextColor.PARSE("&7(0%)→");
        if (getChangePercentage() > 0.01)
            return TextColor.PARSE("&a(" + DECIMAL_FORMAT.format(getChangePercentage()) + "%)↑");
        return TextColor.PARSE("&c(" + DECIMAL_FORMAT.format(getChangePercentage()) + "%)↓");
    }

    default boolean didChange() {
        return Math.abs(getChangePercentage()) > 0.01;
    }
}
