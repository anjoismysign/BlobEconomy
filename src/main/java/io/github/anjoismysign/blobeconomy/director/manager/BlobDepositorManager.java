package io.github.anjoismysign.blobeconomy.director.manager;

import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.blobeconomy.entities.NotEnoughBalance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlobDepositorManager implements Listener {
    private final EconomyManagerDirector director;
    private final Map<Player, BlobDepositor> accounts = new WeakHashMap<>();
    private @Nullable Function<NotEnoughBalance, Boolean> notEnoughEvent;

    public BlobDepositorManager(EconomyManagerDirector director){
        this.director = director;
        Bukkit.getPluginManager().registerEvents(this, director.getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        add(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        accounts.remove(event.getPlayer());
    }

    @Nullable
    public BlobDepositor get(@NotNull Player player){
        return accounts.get(player);
    }

    public void add(@NotNull Player player){
        accounts.put(player, new BlobDepositor(player.getName(), director));
    }

    @NotNull
    public Optional<BlobDepositor> isWalletOwner(@NotNull Player player) {
        return Optional.ofNullable(accounts.get(player));
    }

    @NotNull
    public Optional<BlobDepositor> isWalletOwner(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return Optional.empty();
        return isWalletOwner(player);
    }

    /**
     * Retrieves the UUIDs of every player that currently has a transient depositor.
     *
     * @return a collection of every online player's UUID
     */
    @NotNull
    public Set<UUID> getAll() {
        return accounts.keySet().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());
    }

    public void unload() {
        accounts.clear();
    }

    /**
     * Retrieves the currently registered "not enough balance" handler, or {@code null} if none.
     *
     * @return the registered {@link Function}, or {@code null}
     */
    public @Nullable Function<NotEnoughBalance, Boolean> getNotEnoughEvent() {
        return this.notEnoughEvent;
    }

    /**
     * Registers the handler that is invoked when a Vault {@code withdraw} exceeds the player's
     * wallet balance for a given currency.
     * <p>
     * The {@link Function} receives a {@link NotEnoughBalance} describing the owner
     * {@link BlobDepositor}, the {@code currency} key and the {@code missing} amount. It must
     * make the player able to cover the {@code missing} amount (for example by transferring it
     * from the bank wallet into the wallet) and return {@code true}, or return {@code false} to
     * abort the withdrawal. The default handler transfers the missing amount from the bank wallet
     * to the wallet, so {@code withdraw} falls back to bank funds automatically.
     * <p>
     * Usage:
     * <pre>{@code
     * depositorManager.setNotEnoughEvent(notEnoughBalance -> {
     *     BlobDepositor depositor = notEnoughBalance.owner();
     *     double missing = notEnoughBalance.missing();
     *     Wallet bankWallet = depositor.getBankWallet();
     *     double bank = bankWallet.get(notEnoughBalance.currency());
     *     if (bank < missing) {
     *         return false; // bank can't cover it, abort the withdrawal
     *     }
     *     depositor.deposit(notEnoughBalance.currency(), missing); // wallet += missing
     *     bankWallet.put(notEnoughBalance.currency(), bank - missing); // bank -= missing
     *     return true;
     * });
     * }</pre>
     * <p>
     * Because {@code getBalance} and {@code has} include bank funds, a withdrawal that checks
     * {@code economy.has(player, price)} (or {@code economy.getBalance(player) >= price}) first
     * and then calls {@code economy.withdrawPlayer(player, price)} succeeds whenever the combined
     * wallet and bank balance covers the price.
     *
     * @param notEnoughEvent the handler, or {@code null} to unregister it
     */
    public void setNotEnoughEvent(@Nullable Function<NotEnoughBalance, Boolean> notEnoughEvent) {
        this.notEnoughEvent = notEnoughEvent;
    }
}
