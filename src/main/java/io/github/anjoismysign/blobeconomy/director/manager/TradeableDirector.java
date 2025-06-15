package io.github.anjoismysign.blobeconomy.director.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.director.ui.TraderUI;
import io.github.anjoismysign.blobeconomy.entities.tradeable.DynamicTradeableOperator;
import io.github.anjoismysign.blobeconomy.entities.tradeable.StaticTradeableOperator;
import io.github.anjoismysign.blobeconomy.entities.tradeable.Tradeable;
import io.github.anjoismysign.blobeconomy.entities.tradeable.TradeableOperator;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.ObjectDirector;
import io.github.anjoismysign.bloblib.entities.ObjectDirectorData;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TradeableDirector extends ObjectDirector<Tradeable> {
    private final EconomyManagerDirector director;

    private BukkitTask task;
    private final Set<UUID> coinboycott;
    private final Set<UUID> trading;

    public TradeableDirector(EconomyManagerDirector managerDirector) {
        super(managerDirector, ObjectDirectorData
                        .simple(managerDirector.getRealFileManager(), "Tradeable"),
                file -> {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String currency = file.getName().replace(".yml", "");
                    TradeableOperator operator;
                    if (config.isConfigurationSection("Operator")) {
                        ConfigurationSection operatorSection = config.getConfigurationSection("Operator");
                        operator = DynamicTradeableOperator.READ(operatorSection, currency);
                    } else {
                        if (!config.isDouble("Rate"))
                            throw new ConfigurationFieldException("'Rate' is not valid or set");
                        double rate = config.getDouble("Rate");
                        operator = new StaticTradeableOperator(rate);
                    }
                    return new Tradeable(currency, operator);
                }, false);
        director = managerDirector;
        coinboycott = new HashSet<>();
        trading = new HashSet<>();
    }

    private void reloadTask() {
        if (task != null)
            task.cancel();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                getObjectManager().values().stream()
                        .map(Tradeable::operator)
                        .forEach(TradeableOperator::naturalUpdate);
                trading.forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null)
                        trading.remove(uuid);
                    TraderUI.getInstance().trade(player);
                });
            }
        }.runTaskTimer(getPlugin(), 0, 600);
    }

    private void aggressiveUpdate() {
        getObjectManager().values().stream()
                .map(Tradeable::operator)
                .forEach(TradeableOperator::aggressiveUpdate);
    }

    /**
     * Makes a player boycott the Tradeable market
     *
     * @param player  The player to boycott
     * @param seconds The amount of seconds to boycott
     */
    public void boycott(@NotNull Player player,
                        int seconds) {
        UUID uuid = player.getUniqueId();
        if (coinboycott.contains(uuid)) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobEconomy.Currency-Boycott-Cooldown", player)
                    .handle(player);
            return;
        }
        coinboycott.add(uuid);
        reloadTask();
        aggressiveUpdate();
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobEconomy.Currency-Boycott", onlinePlayer)
                    .handle(onlinePlayer);
        });
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> coinboycott.remove(uuid), seconds * 20L);
    }

    @Override
    public void postWorld() {
        reloadTask();
    }

    public void trade(@NotNull Player player,
                      boolean isTrading) {
        Objects.requireNonNull(player, "'player' cannot be null");
        if (isTrading)
            trading.add(player.getUniqueId());
        else
            trading.remove(player.getUniqueId());
    }
}
