package io.github.anjoismysign.blobeconomy.director.manager;

import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.bloblib.entities.currency.NotEnoughBalance;
import io.github.anjoismysign.bloblib.entities.currency.WalletOwnerManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class BlobDepositorManager {
    private final EconomyManagerDirector director;
    private final Map<Player, BlobDepositor> accounts = new WeakHashMap<>();
    private @Nullable Function<NotEnoughBalance, Boolean> notEnoughEvent;

    public BlobDepositorManager(EconomyManagerDirector director){
        this.director = director;
    }

    @Nullable
    public BlobDepositor get(@NotNull Player player){
        return accounts.get(player);
    }

    public void add(@NotNull Player player){
        accounts.put(player, new BlobDepositor(player.getName(), director));
    }

    public @Nullable Function<NotEnoughBalance, Boolean> getNotEnoughEvent() {
        return this.notEnoughEvent;
    }

    public void setNotEnoughEvent(@Nullable Function<NotEnoughBalance, Boolean> notEnoughEvent) {
        this.notEnoughEvent = notEnoughEvent;
    }
}
