package io.github.anjoismysign.blobeconomy.events;

import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once depositor is in volatile memory as an object,
 * from a persistent/long-term database memory request.
 * Synchronous event.
 * Vault's Economy operations are available at this point.
 */
public class DepositorLoadEvent extends DepositorEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public DepositorLoadEvent(BlobDepositor depositor) {
        super(depositor, false);
    }
}
