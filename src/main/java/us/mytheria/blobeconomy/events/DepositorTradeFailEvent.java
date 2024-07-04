package us.mytheria.blobeconomy.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.bloblib.entities.currency.Currency;

public class DepositorTradeFailEvent extends DepositorEvent {
    private final Currency currency;
    private final double remaining;
    private boolean fix;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public DepositorTradeFailEvent(@NotNull BlobDepositor depositor,
                                   @NotNull Currency currency,
                                   double remaining) {
        super(depositor, false);
        this.currency = currency;
        this.remaining = remaining;
        this.fix = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    /**
     * Whether the trade was fixed.
     *
     * @return Whether the trade was fixed.
     */
    public boolean isFixed() {
        return fix;
    }

    /**
     * Set whether the trade was fixed.
     *
     * @param fix Whether the trade was fixed.
     */
    public void setFixed(boolean fix) {
        this.fix = fix;
    }

    @NotNull
    public Currency getCurrency() {
        return currency;
    }

    /**
     * The remaining amount of currency that was not traded.
     *
     * @return The remaining amount of currency that was not traded.
     */
    public double getRemaining() {
        return remaining;
    }
}
