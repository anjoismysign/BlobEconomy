package io.github.anjoismysign.blobeconomy.events;

import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before loading depositor's into database.
 * Synchronous event.
 * Vault's Economy operations are available ONLY AND ONLY IF
 * NOT SCHEDULING A TASK.
 */
public class DepositorUnloadEvent extends DepositorEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public DepositorUnloadEvent(BlobDepositor depositor) {
        super(depositor, false);
    }
}
