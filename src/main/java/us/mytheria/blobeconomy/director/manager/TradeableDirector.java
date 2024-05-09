package us.mytheria.blobeconomy.director.manager;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.tradeable.DynamicTradeableOperator;
import us.mytheria.blobeconomy.entities.tradeable.StaticTradeableOperator;
import us.mytheria.blobeconomy.entities.tradeable.Tradeable;
import us.mytheria.blobeconomy.entities.tradeable.TradeableOperator;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.entities.ObjectDirectorData;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TradeableDirector extends ObjectDirector<Tradeable> {
    private BukkitTask task;
    private final Set<UUID> coinboycott;

    public TradeableDirector(EconomyManagerDirector managerDirector) {
        super(managerDirector, ObjectDirectorData
                        .simple(managerDirector.getRealFileManager(), "Tradeable"),
                file -> {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String currency = FilenameUtils.removeExtension(file.getName());
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
        coinboycott = new HashSet<>();
    }

    private void reloadTask() {
        if (task != null)
            task.cancel();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                getObjectManager().values().stream()
                        .map(Tradeable::operator)
                        .forEach(TradeableOperator::update);
            }
        }.runTaskTimer(getPlugin(), 0, 600);
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
        if (coinboycott.contains(uuid))
            return;
        coinboycott.add(uuid);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> coinboycott.remove(uuid), seconds * 20L);
    }

    @Override
    public void postWorld() {
        reloadTask();
    }
}
