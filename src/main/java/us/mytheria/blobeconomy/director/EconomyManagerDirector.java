package us.mytheria.blobeconomy.director;

import us.mytheria.blobeconomy.BlobEconomy;
import us.mytheria.blobeconomy.director.commands.Deposit;
import us.mytheria.blobeconomy.director.commands.Withdraw;
import us.mytheria.blobeconomy.director.commands.WithdrawerCmd;
import us.mytheria.blobeconomy.director.manager.ConfigManager;
import us.mytheria.blobeconomy.director.ui.WithdrawerUI;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.blobeconomy.events.DepositorLoadEvent;
import us.mytheria.blobeconomy.events.DepositorUnloadEvent;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.entities.currency.WalletOwnerManager;

public class EconomyManagerDirector extends GenericManagerDirector<BlobEconomy> {
    private WithdrawerUI withdrawerUI;

    public EconomyManagerDirector(BlobEconomy plugin) {
        super(plugin);
        getRealFileManager().unpackYamlFile("/currency", "default");
        registerMetaBlobInventory("Withdraw");
        addManager("ConfigManager", new ConfigManager(this));
//        addManager("ListenerManager", new ListenerManager(this));
        addCurrencyDirector("Currency");
        getCurrencyDirector().addNonAdminChildCommand(executorData -> Withdraw.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(executorData -> Withdraw.tabCompleter(executorData, this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> Deposit.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(Deposit::tabCompleter);
        getCurrencyDirector().addNonAdminChildTabCompleter(executorData -> WithdrawerCmd.tabCompleter(executorData, this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> WithdrawerCmd.command(executorData, this));
        addWalletOwnerManager("DepositorManager",
                x -> x, crudable ->
                        new BlobDepositor(crudable, this),
                "BlobDepositor",
                true,
                DepositorLoadEvent::new,
                DepositorUnloadEvent::new);
        getCurrencyDirector().whenObjectManagerFilesLoad(manager -> {
            withdrawerUI = WithdrawerUI.getInstance(this);
            getDepositorManager().registerEconomy(manager.getObject("default"),
                    getCurrencyDirector());
            getDepositorManager().registerDefaultEconomyCommand(getCurrencyDirector());
            getDepositorManager().registerPlaceholderAPIExpansion();
        });
    }

    @Override
    public void reload() {
        getCurrencyDirector().reload();
    }

    @Override
    public void unload() {
        getDepositorManager().unload();
    }

    @Override
    public void postWorld() {
    }

    public final ObjectDirector<Currency> getCurrencyDirector() {
        return getCurrencyDirector("Currency");
    }

    public final WalletOwnerManager<BlobDepositor> getDepositorManager() {
        return getWalletOwnerManager("DepositorManager", BlobDepositor.class);
    }

    public final ConfigManager getConfigManager() {
        return getManager("ConfigManager", ConfigManager.class);
    }
}