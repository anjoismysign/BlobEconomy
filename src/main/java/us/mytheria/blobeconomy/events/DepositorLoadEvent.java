package us.mytheria.blobeconomy.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.entities.BlobDepositor;

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
