package us.mytheria.blobeconomy.blobtycoon;

import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.mytheria.blobeconomy.director.manager.ListenerManager;
import us.mytheria.blobeconomy.events.DepositorPreTradeEvent;
import us.mytheria.blobeconomy.events.DepositorTradeFailEvent;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.TycoonPlayer;

import java.util.Optional;

public class BlobTycoonTransferFunds implements Listener {
    public BlobTycoonTransferFunds(ListenerManager listenerManager) {
        Bukkit.getPluginManager().registerEvents(this, listenerManager.getPlugin());
    }

    @EventHandler
    public void onPreTrade(DepositorPreTradeEvent event) {
        Currency currency = event.getCurrency();
        double current = event.getBalance();
        Player player = event.getDepositor().getPlayer();
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
            return;
        PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
        double valuableAmount = profile.getValuable(currency.getKey());
        double total = current + valuableAmount;
        event.setBalance(total);
    }


    @EventHandler
    public void onFail(DepositorTradeFailEvent event) {
        if (event.isFixed())
            return;
        Currency currency = event.getCurrency();
        double amount = event.getRemaining();
        Player player = event.getDepositor().getPlayer();
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
            return;
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        if (!plotProfile.withdrawValuable(currency.getKey(), amount))
            return;
        IdentityEconomy economy = BlobLibEconomyAPI.getInstance().getElasticEconomy().map(Optional.ofNullable(currency.getKey()));
        economy.depositPlayer(player, amount);
        event.setFixed(true);
    }
}
