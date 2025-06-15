package io.github.anjoismysign.blobeconomy.director.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import io.github.anjoismysign.blobeconomy.director.EconomyManager;
import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.director.ui.TraderUI;
import io.github.anjoismysign.blobeconomy.entities.LockedTrading;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.entities.ConfigDecorator;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryButton;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryDataRegistry;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class EconomyConfigManager extends EconomyManager {
    private boolean freeTraderCurrencyMarket;
    private boolean transientUsers;
    private Set<String> alwaysTradableCurrencies;
    private Set<String> withdrawAllKeywords;
    private Set<String> withdrawHalfKeywords;
    private LockedTrading lockedTrading;

    public EconomyConfigManager(EconomyManagerDirector managerDirector) {
        super(managerDirector);
        reload();
    }

    public void reload() {
        ConfigDecorator configDecorator = getPlugin().getConfigDecorator();
        ConfigurationSection settingsSection = configDecorator.reloadAndGetSection("Settings");
        lockedTrading = LockedTrading.of(settingsSection.getConfigurationSection("Locked-Trading"));
        freeTraderCurrencyMarket = settingsSection.getBoolean("Free-Trader-Currency-Market");
        transientUsers = settingsSection.getBoolean("Transient-Users");
        alwaysTradableCurrencies = settingsSection.getStringList("Always-Tradable-Currencies")
                .stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        withdrawAllKeywords = settingsSection.getStringList("Withdraw-All-Keywords")
                .stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        withdrawHalfKeywords = settingsSection.getStringList("Withdraw-Half-Keywords")
                .stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        BlobLibInventoryAPI inventoryAPI = BlobLibInventoryAPI.getInstance();
        InventoryDataRegistry<InventoryButton> tradeAmount = inventoryAPI.getInventoryDataRegistry("Trade-Amount");
        tradeAmount.onClick("All", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TraderUI.getInstance().trade(player, 1);
        });
        tradeAmount.onClick("Half", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TraderUI.getInstance().trade(player, 0.5);
        });
        tradeAmount.onClick("One-Fifth", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TraderUI.getInstance().trade(player, 0.2);
        });
        tradeAmount.onClick("Custom", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TraderUI.getInstance().tradeCustomAmount(player);
        });
    }

    public boolean isFreeTraderCurrencyMarket() {
        return freeTraderCurrencyMarket;
    }

    public boolean isTransientUsers() {
        return transientUsers;
    }

    public LockedTrading getLockedTrading() {
        return lockedTrading;
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