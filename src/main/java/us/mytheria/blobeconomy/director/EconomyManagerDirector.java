package us.mytheria.blobeconomy.director;

import us.mytheria.blobeconomy.BlobEconomy;
import us.mytheria.blobeconomy.director.manager.ConfigManager;
import us.mytheria.blobeconomy.director.manager.ListenerManager;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.blobeconomy.events.DepositorLoadEvent;
import us.mytheria.blobeconomy.events.DepositorUnloadEvent;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.entities.currency.WalletOwnerManager;

public class EconomyManagerDirector extends GenericManagerDirector<BlobEconomy> {

    public EconomyManagerDirector(BlobEconomy plugin) {
        super(plugin);
        getRealFileManager().unpackYamlFile("/currency", "default");
        addManager("ConfigManager", new ConfigManager(this));
        addManager("ListenerManager", new ListenerManager(this));
        addCurrencyDirector("Currency");
        addWalletOwnerManager("DepositorManager",
                x -> x,
                BlobDepositor::new,
                "BlobDepositor",
                true,
                DepositorLoadEvent::new,
                DepositorUnloadEvent::new);
        getCurrencyDirector().whenObjectManagerFilesLoad(manager -> {
            getDepositorManager().registerEconomy(manager.getObject("default"),
                    this.getCurrencyDirector());
            getDepositorManager().registerDefaultEconomyCommand(getCurrencyDirector());
            getDepositorManager().registerPlaceholderAPIExpansion();
        });
    }

    /**
     * From top to bottom, follow the order.
     */
    @Override
    public void reload() {
        //reload directors
    }

    @Override
    public void unload() {
    }

    @Override
    public void postWorld() {
    }

    public final ObjectDirector<Currency> getCurrencyDirector() {
        return super.getCurrencyDirector("Currency");
    }

    public final WalletOwnerManager<BlobDepositor> getDepositorManager() {
        return getWalletOwnerManager("DepositorManager", BlobDepositor.class);
    }

    public final ConfigManager getConfigManager() {
        return getManager("ConfigManager", ConfigManager.class);
    }

    public final ListenerManager getListenerManager() {
        return getManager("ListenerManager", ListenerManager.class);
    }
}