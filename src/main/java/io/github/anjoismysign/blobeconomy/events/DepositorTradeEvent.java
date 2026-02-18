package io.github.anjoismysign.blobeconomy.events;

import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when depositor trades (/currency trader %playerName%)
 */
public class DepositorTradeEvent extends DepositorEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private final double fromAmount, toAmount;
    private final String from, to;

    public DepositorTradeEvent(BlobDepositor depositor,
                               double fromAmount,
                               double toAmount,
                               String from,
                               String to) {
        super(depositor, false);
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.from = from;
        this.to = to;
    }

    /**
     * @return The amount of from.
     */
    public double getFromAmount() {
        return fromAmount;
    }

    /**
     * @return The amount of to.
     */
    public double getToAmount() {
        return toAmount;
    }

    /**
     * @return The currency from which the conversion will be done, like where the player actually has the money.
     */
    public String getFrom() {
        return from;
    }

    /**
     * @return The currency to which the conversion will be done, like what the player wants to own after the transaction.
     */
    public String getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "DepositorTradeEvent{" +
                "depositor=" + getDepositor().getPlayer().getName() +
                ", fromAmount=" + fromAmount +
                ", toAmount=" + toAmount +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }

}
