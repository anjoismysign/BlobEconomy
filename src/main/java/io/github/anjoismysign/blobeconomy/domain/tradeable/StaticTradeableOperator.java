package io.github.anjoismysign.blobeconomy.domain.tradeable;

public record StaticTradeableOperator(double getRate)
        implements TradeableOperator {
    @Override
    public void naturalUpdate() {
    }

    @Override
    public void aggressiveUpdate() {
    }

    @Override
    public double getChangePercentage() {
        return 0;
    }
}
