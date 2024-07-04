package us.mytheria.blobeconomy.director.manager;

import org.bukkit.Bukkit;
import us.mytheria.blobeconomy.blobtycoon.BlobTycoonTransferFunds;
import us.mytheria.blobeconomy.director.EconomyManager;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;

public class ListenerManager extends EconomyManager {

    public ListenerManager(EconomyManagerDirector managerDirector) {
        super(managerDirector);
        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("BlobTycoon")) {
                new BlobTycoonTransferFunds(this);
            }
        });
    }
}