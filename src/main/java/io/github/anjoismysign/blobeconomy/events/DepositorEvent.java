package io.github.anjoismysign.blobeconomy.events;

import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class DepositorEvent extends Event {
    private final BlobDepositor depositor;

    public DepositorEvent(BlobDepositor depositor, boolean isAsync) {
        super(isAsync);
        this.depositor = depositor;
    }

    /**
     * Will get the main depositor involved in the event.
     *
     * @return The main depositor involved in the event.
     */
    @NotNull
    public BlobDepositor getDepositor() {
        return depositor;
    }
}
