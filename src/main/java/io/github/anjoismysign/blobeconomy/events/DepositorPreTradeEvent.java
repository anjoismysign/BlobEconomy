package io.github.anjoismysign.blobeconomy.events;

import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DepositorPreTradeEvent extends DepositorEvent {
    private final Currency currency;
    private double balance;
    private boolean hasChanged;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public DepositorPreTradeEvent(@NotNull BlobDepositor depositor,
                                  @NotNull Currency currency,
                                  double balance) {
        super(depositor, false);
        this.currency = currency;
        this.balance = balance;
        this.hasChanged = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @NotNull
    public Currency getCurrency() {
        return currency;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
