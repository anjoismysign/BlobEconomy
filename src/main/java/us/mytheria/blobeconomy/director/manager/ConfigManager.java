package us.mytheria.blobeconomy.director.manager;

import org.bukkit.configuration.file.FileConfiguration;
import us.mytheria.blobeconomy.BlobEconomy;
import us.mytheria.blobeconomy.director.EconomyManager;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;

public class ConfigManager extends EconomyManager {
//    private SimpleEventListener<Boolean> listenerExample;

    public ConfigManager(EconomyManagerDirector managerDirector) {
        super(managerDirector);
        BlobEconomy main = managerDirector.getPlugin();
        FileConfiguration config = main.getConfig();
        config.options().copyDefaults(true);
//        ConfigurationSection listeners = config.getConfigurationSection("Listeners");
//        listenerExample = SimpleEventListener.BOOLEAN(
//                (listeners.getConfigurationSection("UseUUIDs")), "State");
        main.saveConfig();
    }

//    public SimpleEventListener<Boolean> useUUIDs() {
//        return listenerExample;
//    }
}