package us.mytheria.blobeconomy.entities.tradeable;

public record StaticTradeableOperator(double getRate)
        implements TradeableOperator {
    @Override
    public void update() {
    }

    @Override
    public double getChangePercentage() {
        return 0;
    }
}
