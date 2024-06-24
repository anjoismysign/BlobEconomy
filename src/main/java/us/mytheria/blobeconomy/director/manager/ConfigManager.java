package us.mytheria.blobeconomy.director.manager;

import org.bukkit.configuration.ConfigurationSection;
import us.mytheria.blobeconomy.director.EconomyManager;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.bloblib.entities.ConfigDecorator;

import java.util.Set;
import java.util.stream.Collectors;

public class ConfigManager extends EconomyManager {
    private boolean freeTraderCurrencyMarket;
    private boolean transientUsers;
    private Set<String> alwaysTradableCurrencies;
    private Set<String> withdrawAllKeywords;
    private Set<String> withdrawHalfKeywords;

    public ConfigManager(EconomyManagerDirector managerDirector) {
        super(managerDirector);
        reload();
    }

    public void reload() {
        ConfigDecorator configDecorator = getPlugin().getConfigDecorator();
        ConfigurationSection settingsSection = configDecorator.reloadAndGetSection("Settings");
        freeTraderCurrencyMarket = settingsSection.getBoolean("Free-Trader-Currency-Market");
        transientUsers = settingsSection.getBoolean("Transient-Users");
        alwaysTradableCurrencies = settingsSection.getStringList("Always-Tradable-Currencies")
                .stream().map(String::toLowerCase).collect(Collectors.toSet());
        withdrawAllKeywords = settingsSection.getStringList("Withdraw-All-Keywords")
                .stream().map(String::toLowerCase).collect(Collectors.toSet());
        withdrawHalfKeywords = settingsSection.getStringList("Withdraw-Half-Keywords")
                .stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    public boolean isFreeTraderCurrencyMarket() {
        return freeTraderCurrencyMarket;
    }

    public boolean isTransientUsers() {
        return transientUsers;
    }

    public Set<String> getAlwaysTradableCurrencies() {
        return alwaysTradableCurrencies;
    }

    public Set<String> getWithdrawAllKeywords() {
        return withdrawAllKeywords;
    }

    public Set<String> getWithdrawHalfKeywords() {
        return withdrawHalfKeywords;
    }
}