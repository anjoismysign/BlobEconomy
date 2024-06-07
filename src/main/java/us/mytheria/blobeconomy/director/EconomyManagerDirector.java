package us.mytheria.blobeconomy.director;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.BlobEconomy;
import us.mytheria.blobeconomy.director.commands.*;
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
        registerBlobMessage(
                "es_es/blobeconomy_lang",
                "de_de/blobeconomy_lang",
                "el_gr/blobeconomy_lang",
                "fr_fr/blobeconomy_lang",
                "nl_nl/blobeconomy_lang",
                "pt_pt/blobeconomy_lang",
                "ru_ru/blobeconomy_lang",
                "zh_cn/blobeconomy_lang"
        );
        registerTranslatableItem(
                "es_es/blobeconomy_translatable_items",
                "de_de/blobeconomy_translatable_items",
                "el_gr/blobeconomy_translatable_items",
                "fr_fr/blobeconomy_translatable_items",
                "nl_nl/blobeconomy_translatable_items",
                "pt_pt/blobeconomy_translatable_items",
                "ru_ru/blobeconomy_translatable_items",
                "zh_cn/blobeconomy_translatable_items");
        registerBlobInventory("Withdraw",
                "es_es/Withdraw",
                "de_de/Withdraw",
                "el_gr/Withdraw",
                "fr_fr/Withdraw",
                "nl_nl/Withdraw",
                "pt_pt/Withdraw",
                "ru_ru/Withdraw",
                "zh_cn/Withdraw");
        registerBlobInventory("Trade",
                "es_es/Trade",
                "de_de/Trade",
                "el_gr/Trade",
                "fr_fr/Trade",
                "nl_nl/Trade",
                "pt_pt/Trade",
                "ru_ru/Trade",
                "zh_cn/Trade");
        addManager("ConfigManager", new ConfigManager(this));
        addCurrencyDirector("Currency");
        addManager("TradeableDirector", new TradeableDirector(this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> Withdraw.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(executorData -> Withdraw.tabCompleter(executorData, this));
        getCurrencyDirector().addNonAdminChildCommand(executorData -> Deposit.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(Deposit::tabCompleter);
        getCurrencyDirector().addNonAdminChildCommand(executorData -> WithdrawerCmd.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(WithdrawerCmd::tabCompleter);
        getCurrencyDirector().addAdminChildCommand(executorData -> BoycottCmd.command(executorData, this));
        getCurrencyDirector().addAdminChildTabCompleter(BoycottCmd::tabCompleter);
        getCurrencyDirector().addNonAdminChildCommand(executorData -> TraderCmd.command(executorData, this));
        getCurrencyDirector().addNonAdminChildTabCompleter(TraderCmd::tabCompleter);
        addWalletOwnerManager("DepositorManager",
                x -> x, crudable ->
                        new BlobDepositor(crudable, this),
                "BlobDepositor",
                true,
                DepositorLoadEvent::new,
                DepositorUnloadEvent::new);
        getCurrencyDirector().whenObjectManagerFilesLoad(manager -> {
            try {
                withdrawerUI = WithdrawerUI.getInstance(this);
                traderUI = TraderUI.getInstance(this);
                Bukkit.getScheduler().runTask(getPlugin(), () -> {
                    getDepositorManager().registerEconomy(manager.getObject("default"),
                            getCurrencyDirector());
                    getDepositorManager().registerDefaultEconomyCommand(getCurrencyDirector());
                    Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), () -> {
                        getDepositorManager().registerPlaceholderAPIExpansion();
                    }, 20L);

                });
            } catch (Exception e) {
                e.printStackTrace();
            }
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