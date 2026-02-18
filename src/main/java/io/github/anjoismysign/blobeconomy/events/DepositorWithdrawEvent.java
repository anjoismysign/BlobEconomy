package io.github.anjoismysign.blobeconomy.events;

import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when depositor uses the WithdrawerUI (/currency withdrawer %playerName%)
 */
public class DepositorWithdrawEvent extends DepositorEvent implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private final String amountInput;
    private double amount;
    private final String currency;
    private boolean cancelled;
    private final boolean withdrawingAll;
    private final boolean withdrawingHalf;

    public DepositorWithdrawEvent(BlobDepositor depositor,
                                  String amountInput,
                                  double amount,
                                  String currency,
                                  boolean withdrawingAll,
                                  boolean withdrawingHalf) {
        super(depositor, false);
        this.amountInput = amountInput;
        this.amount = amount;
        this.currency = currency;
        this.cancelled = false;
        this.withdrawingAll = withdrawingAll;
        this.withdrawingHalf = withdrawingHalf;
    }

    /**
     * @return What the player input in chat as the amount to withdraw.
     */
    public String getAmountInput() {
        return amountInput;
    }

    /**
     * @return The amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param amount The custom amount that would be withdrawn
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @return The currency that's being withdrawn
     */
    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @return Whether BlobEconomy did detect the initial amountInput as to withdraw the whole balance.
     */
    public boolean isWithdrawingAll() {
        return withdrawingAll;
    }

    /**
     * @return Whether BlobEconomy did detect the initial amountInput as to withdraw half the balance.
     */
    public boolean isWithdrawingHalf() {
        return withdrawingHalf;
    }
}
