package us.mytheria.blobeconomy.entities;

import org.bukkit.entity.Player;
import us.mytheria.bloblib.entities.BlobCrudable;
import us.mytheria.bloblib.entities.currency.Wallet;
import us.mytheria.bloblib.entities.currency.WalletOwner;

public class BlobDepositor implements WalletOwner {
    private final BlobCrudable crudable;
    private final Wallet wallet;

    public BlobDepositor(BlobCrudable crudable) {
        this.crudable = crudable;
        wallet = deserializeWallet();
    }

    @Override
    public BlobCrudable serializeAllAttributes() {
        serializeWallet();
        return crudable;
    }

    @Override
    public String getPlayerName() {
        Player player = getPlayer();
        if (player == null || !player.isOnline())
            throw new IllegalStateException("Player is null");
        return player.getName();
    }

    @Override
    public String getPlayerUniqueId() {
        Player player = getPlayer();
        if (player == null || !player.isOnline())
            throw new IllegalStateException("Player is null");
        return player.getUniqueId().toString();
    }

    @Override
    public BlobCrudable blobCrudable() {
        return crudable;
    }

    @Override
    public Wallet getWallet() {
        return wallet;
    }
}
