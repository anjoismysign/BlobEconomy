package io.github.anjoismysign.blobeconomy.economy;

import io.github.anjoismysign.blobeconomy.director.manager.BlobDepositorManager;
import io.github.anjoismysign.blobeconomy.domain.BlobDepositor;
import io.github.anjoismysign.blobeconomy.domain.NotEnoughBalance;
import io.github.anjoismysign.bloblib.currency.Currency;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * A transient {@link IdentityEconomy} implementation for a single {@link Currency}.
 * <p>
 * It only tracks online players' wallets in volatile memory, nothing is ever saved.
 * The bank wallet backs up the wallet: {@link #getBalance(UUID)} and {@link #has}
 * include bank funds, and a {@code withdraw} that exceeds the wallet balance will
 * fall back to the bank wallet (see {@link BlobDepositorManager#setNotEnoughEvent}).
 * The Vault {@code bank*} API methods are still not implemented, the bank can only
 * be manipulated through BlobEconomy's API.
 */
public class CurrencyEconomy implements IdentityEconomy {
    private final Currency currency;
    private final BlobDepositorManager depositorManager;

    public CurrencyEconomy(Currency currency, BlobDepositorManager depositorManager) {
        this.currency = currency;
        this.depositorManager = depositorManager;
    }

    private Optional<BlobDepositor> isOnline(UUID uuid) {
        return depositorManager.isWalletOwner(uuid);
    }

    private boolean ifIsOnline(UUID uuid, java.util.function.Consumer<BlobDepositor> consumer) {
        Optional<BlobDepositor> depositor = isOnline(uuid);
        if (depositor.isPresent()) {
            consumer.accept(depositor.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsAllRecordsOperation() {
        return false;
    }

    @Override
    public boolean supportsAllOnlineOperation() {
        return true;
    }

    @Override
    public boolean supportsOfflineOperations() {
        return false;
    }

    @Override
    public boolean supportsUUIDOperations() {
        return false;
    }

    @Override
    public boolean createAccount(UUID uuid, String name) {
        return true;
    }

    @Override
    public boolean createAccount(UUID uuid, String name, String worldName) {
        return true;
    }

    @Override
    public Map<UUID, String> getAllRecords() {
        throw new UnsupportedOperationException("getAllRecords() is not supported by CurrencyEconomy");
    }

    @Override
    public Collection<UUID> getAllOnline() {
        return depositorManager.getAll();
    }

    @Override
    public String getAccountName(UUID uuid) {
        Optional<BlobDepositor> depositor = isOnline(uuid);
        if (depositor.isEmpty())
            throw new IllegalArgumentException("Player is not online: " + uuid);
        return depositor.get().getPlayerName();
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return isOnline(uuid).isPresent();
    }

    @Override
    public boolean hasAccount(UUID uuid, String worldName) {
        return hasAccount(uuid);
    }

    @Override
    public boolean renameAccount(UUID uuid, String name) {
        return isOnline(uuid).isPresent();
    }

    @Override
    public double getBalance(UUID uuid) {
        Optional<BlobDepositor> depositor = isOnline(uuid);
        if (depositor.isEmpty()) {
            return 0;
        }
        BlobDepositor blobDepositor = depositor.get();
        return walletBalance(blobDepositor) + blobDepositor.getBankWallet().balance(currency.getKey());
    }

    private double walletBalance(BlobDepositor blobDepositor) {
        return blobDepositor.getBalance(currency.getKey());
    }

    @Override
    public double getBalance(UUID uuid, String world) {
        return getBalance(uuid);
    }

    @Override
    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    @Override
    public boolean has(UUID uuid, String worldName, double amount) {
        return getBalance(uuid) >= amount;
    }

    @Override
    public EconomyResponse withdraw(UUID uuid, double amount) {
        Optional<BlobDepositor> optional = isOnline(uuid);
        if (optional.isEmpty())
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,
                    "Offline operations not implemented");
        BlobDepositor depositor = optional.get();
        double balance = walletBalance(depositor);
        if (balance < amount) {
            Function<NotEnoughBalance, Boolean> function = depositorManager.getNotEnoughEvent();
            if (function == null) {
                return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.FAILURE, null);
            }
            double missing = amount - balance;
            NotEnoughBalance event = new NotEnoughBalance(depositor, currency.getKey(), missing);
            boolean eventResult = function.apply(event);
            if (!eventResult || getBalance(uuid) < amount) {
                return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.FAILURE, null);
            }
        }
        depositor.withdraw(currency.getKey(), amount);
        return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdraw(UUID uuid, String worldName, double amount) {
        return withdraw(uuid, amount);
    }

    @Override
    public EconomyResponse deposit(UUID uuid, double amount) {
        boolean isOnline = ifIsOnline(uuid, depositor ->
                depositor.deposit(currency.getKey(), amount));
        if (isOnline)
            return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, null);
        return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,
                "Offline operations not implemented");
    }

    @Override
    public EconomyResponse deposit(UUID uuid, String worldName, double amount) {
        return deposit(uuid, amount);
    }

    @Override
    public EconomyResponse createBank(String name, UUID uuid) {
        return notImplemented();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, UUID uuid) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, UUID uuid) {
        return notImplemented();
    }

    @Override
    public List<String> getBanks() {
        return new ArrayList<>();
    }

    private EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return currency.getKey();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return currency.getDecimalFormat().getMinimumFractionDigits();
    }

    @Override
    public String format(double amount) {
        return currency.display(amount);
    }

    @Override
    public String currencyNamePlural() {
        return "";
    }

    @Override
    public String currencyNameSingular() {
        return "";
    }

    @Override
    public boolean hasAccount(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        return player != null && hasAccount(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player.getUniqueId() != null && hasAccount(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return 0;
        return getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return false;
        return has(player.getUniqueId(), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return has(player.getUniqueId(), amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Player not online");
        return withdraw(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Player not online");
        return deposit(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return depositPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse createBank(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }
}
