package us.mytheria.blobeconomy.entities;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record LockedTrading(boolean isEnabled,
                            Set<String> getAllowedCurrencies) {

    public static LockedTrading of(@NotNull ConfigurationSection section) {
        Objects.requireNonNull(section, "'section' cannot be null");
        if (!section.isList("Allowed-Currencies"))
            throw new IllegalArgumentException("'Allowed-Currencies' is not set or valid");
        boolean isEnabled = section.getBoolean("Enabled", false);
        Set<String> allowedCurrencies = section.getStringList("Allowed-Currencies").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return new LockedTrading(isEnabled, allowedCurrencies);
    }
}
