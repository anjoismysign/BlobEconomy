package us.mytheria.blobeconomy.director.manager;

import org.bukkit.configuration.ConfigurationSection;
import us.mytheria.blobeconomy.director.EconomyManager;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.bloblib.entities.ConfigDecorator;

public class ConfigManager extends EconomyManager {
    private boolean freeTraderCurrencyMarket;

    public ConfigManager(EconomyManagerDirector managerDirector) {
        super(managerDirector);
        reload();
    }

    public void reload() {
        ConfigDecorator configDecorator = getPlugin().getConfigDecorator();
        ConfigurationSection settingsSection = configDecorator.reloadAndGetSection("Settings");
        freeTraderCurrencyMarket = settingsSection.getBoolean("Free-Trader-Currency-Market");
    }

    public boolean isFreeTraderCurrencyMarket() {
        return freeTraderCurrencyMarket;
    }
}