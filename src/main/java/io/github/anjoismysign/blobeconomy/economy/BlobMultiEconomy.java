package io.github.anjoismysign.blobeconomy.economy;

import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.bloblib.currency.Currency;
import io.github.anjoismysign.bloblib.manager.ObjectDirector;
import net.milkbowl.vault.economy.IdentityEconomy;
import net.milkbowl.vault.economy.MultiEconomy;
import net.milkbowl.vault.economy.wrappers.MultiEconomyWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A transient {@link MultiEconomy} implementation for BlobEconomy.
 * <p>
 * Every {@link Currency} is exposed as an {@link IdentityEconomy} implementation.
 * All balances are stored in volatile memory and are never saved for any player.
 * The bank wallet backs up the wallet: {@code getBalance} and {@code has} include
 * bank funds and {@code withdraw} falls back to the bank wallet, but the Vault
 * {@code bank*} API methods are not implemented and the bank can only be
 * manipulated through BlobEconomy's API.
 */
public class BlobMultiEconomy implements MultiEconomy {
    private final Map<String, CurrencyEconomy> implementations;
    private final CurrencyEconomy defaultEconomy;

    public static void load(@NotNull EconomyManagerDirector director){
        new MultiEconomyWrapper(new BlobMultiEconomy(director)).registerProviders(true);
    }

    private BlobMultiEconomy(EconomyManagerDirector director) {
        ObjectDirector<Currency> currencyDirector = director.getCurrencyDirector();
        this.implementations = new HashMap<>();
        currencyDirector.getObjectManager().values().forEach(currency ->
                implementations.put(currency.getKey(), new CurrencyEconomy(currency, director.getDepositorManager())));
        CurrencyEconomy defaultImplementation = implementations.get(director.getConfigManager().getDefaultCurrency());
        defaultImplementation = defaultImplementation == null ? implementations.get("default") : defaultImplementation;
        this.defaultEconomy = Objects.requireNonNull(defaultImplementation, "There are no currencies");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "BlobEconomy";
    }

    @Override
    public boolean existsImplementation(String name) {
        return implementations.containsKey(name);
    }

    @Override
    public boolean existsImplementation(String name, String world) {
        return existsImplementation(name);
    }

    @Override
    public IdentityEconomy getImplementation(String name) {
        return implementations.get(name);
    }

    @Override
    public IdentityEconomy getDefault() {
        return defaultEconomy;
    }

    @Override
    public IdentityEconomy getDefault(String world) {
        return getDefault();
    }

    @Override
    public Collection<IdentityEconomy> getAllImplementations() {
        return implementations.values().stream()
                .map(currencyEconomy -> (IdentityEconomy) currencyEconomy)
                .toList();
    }

    @Override
    public Collection<IdentityEconomy> getAllImplementations(String world) {
        return getAllImplementations();
    }
}
