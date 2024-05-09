package us.mytheria.blobeconomy.director;

import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.BlobEconomy;
import us.mytheria.blobeconomy.director.commands.Deposit;
import us.mytheria.blobeconomy.director.commands.TraderCmd;
import us.mytheria.blobeconomy.director.commands.Withdraw;
import us.mytheria.blobeconomy.director.commands.WithdrawerCmd;
import us.mytheria.blobeconomy.director.manager.ConfigManager;
import us.mytheria.blobeconomy.director.manager.TradeableDirector;
import us.mytheria.blobeconomy.director.ui.TraderUI;
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
    private TraderUI traderUI;

    public EconomyManagerDirector(BlobEconomy plugin) {
        super(plugin);
        getRealFileManager().unpackYamlFile("/currency", "default");
        registerBlobMessage("es_es/blobeconomy_lang");
        registerBlobInventory("Withdraw", "es_es/Withdraw");
        registerBlobInventory("Trade", "es_es/Trade");
        addManager("ConfigManager", new ConfigManager(this));
        addCurrencyDirector("Currency");
        addManager("TradeableDirector", new TradeableDirector(this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> Withdraw.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(executorData -> Withdraw.tabCompleter(executorData, this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> Deposit.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(Deposit::tabCompleter);
        getCurrencyDirector().addNonAdminChildTabCompleter(executorData -> WithdrawerCmd.tabCompleter(executorData, this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> WithdrawerCmd.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(executorData -> TraderCmd.tabCompleter(executorData, this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> TraderCmd.command(executorData, this));
        addWalletOwnerManager("DepositorManager",
                x -> x, crudable ->
                        new BlobDepositor(crudable, this),
                "BlobDepositor",
                true,
                DepositorLoadEvent::new,
                DepositorUnloadEvent::new);
        getCurrencyDirector().whenObjectManagerFilesLoad(manager -> {
            withdrawerUI = WithdrawerUI.getInstance(this);
            traderUI = TraderUI.getInstance(this);
            getDepositorManager().registerEconomy(manager.getObject("default"),
                    getCurrencyDirector());
            getDepositorManager().registerDefaultEconomyCommand(getCurrencyDirector());
            getDepositorManager().registerPlaceholderAPIExpansion();
        });
    }

    @Override
    public void reload() {
        getConfigManager().reload();
        getCurrencyDirector().reload();
        getCurrencyDirector().whenReloaded(() ->
                getTradeableDirector().reload());
    }

    @Override
    public void unload() {
        getDepositorManager().unload();
    }

    @Override
    public void postWorld() {
        getTradeableDirector().postWorld();
    }

    @NotNull
    public final ObjectDirector<Currency> getCurrencyDirector() {
        return getCurrencyDirector("Currency");
    }

    @NotNull
    public final TradeableDirector getTradeableDirector() {
        return getManager("TradeableDirector", TradeableDirector.class);
    }

    @NotNull
    public final WalletOwnerManager<BlobDepositor> getDepositorManager() {
        return getWalletOwnerManager("DepositorManager", BlobDepositor.class);
    }

    @NotNull
    public final ConfigManager getConfigManager() {
        return getManager("ConfigManager", ConfigManager.class);
    }
}