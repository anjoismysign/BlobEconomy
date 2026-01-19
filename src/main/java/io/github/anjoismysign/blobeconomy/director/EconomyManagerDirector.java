package io.github.anjoismysign.blobeconomy.director;

import io.github.anjoismysign.blobeconomy.BlobEconomy;
import io.github.anjoismysign.blobeconomy.director.commands.BankCommand;
import io.github.anjoismysign.blobeconomy.director.commands.BoycottCmd;
import io.github.anjoismysign.blobeconomy.director.commands.Deposit;
import io.github.anjoismysign.blobeconomy.director.commands.TraderCmd;
import io.github.anjoismysign.blobeconomy.director.commands.Withdraw;
import io.github.anjoismysign.blobeconomy.director.commands.WithdrawerCmd;
import io.github.anjoismysign.blobeconomy.director.manager.EconomyConfigManager;
import io.github.anjoismysign.blobeconomy.director.manager.TradeableDirector;
import io.github.anjoismysign.blobeconomy.director.ui.BankUI;
import io.github.anjoismysign.blobeconomy.director.ui.TraderUI;
import io.github.anjoismysign.blobeconomy.director.ui.WithdrawerUI;
import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.blobeconomy.events.DepositorLoadEvent;
import io.github.anjoismysign.blobeconomy.events.DepositorUnloadEvent;
import io.github.anjoismysign.bloblib.entities.GenericManagerDirector;
import io.github.anjoismysign.bloblib.entities.ObjectDirector;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.bloblib.entities.currency.Wallet;
import io.github.anjoismysign.bloblib.entities.currency.WalletOwner;
import io.github.anjoismysign.bloblib.entities.currency.WalletOwnerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class EconomyManagerDirector extends GenericManagerDirector<BlobEconomy> {
    private WithdrawerUI withdrawerUI;
    private TraderUI traderUI;
    private BankUI bankUI;

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
        registerBlobInventory("Trade-Amount",
                "es_es/Trade-Amount",
                "de_de/Trade-Amount",
                "el_gr/Trade-Amount",
                "fr_fr/Trade-Amount",
                "nl_nl/Trade-Amount",
                "pt_pt/Trade-Amount",
                "ru_ru/Trade-Amount",
                "zh_cn/Trade-Amount");
        registerBlobInventory("Bank",
                "es_es/Bank",
                "de_de/Bank",
                "el_gr/Bank",
                "fr_fr/Bank",
                "nl_nl/Bank",
                "pt_pt/Bank",
                "ru_ru/Bank",
                "zh_cn/Bank");
        registerBlobInventory("BankCurrency",
                "es_es/BankCurrency",
                "de_de/BankCurrency",
                "el_gr/BankCurrency",
                "fr_fr/BankCurrency",
                "nl_nl/BankCurrency",
                "pt_pt/BankCurrency",
                "ru_ru/BankCurrency",
                "zh_cn/BankCurrency");
        registerBlobInventory("BankDeposit",
                "es_es/BankDeposit",
                "de_de/BankDeposit",
                "el_gr/BankDeposit",
                "fr_fr/BankDeposit",
                "nl_nl/BankDeposit",
                "pt_pt/BankDeposit",
                "ru_ru/BankDeposit",
                "zh_cn/BankDeposit");
        registerBlobInventory("BankWithdraw",
                "es_es/BankWithdraw",
                "de_de/BankWithdraw",
                "el_gr/BankWithdraw",
                "fr_fr/BankWithdraw",
                "nl_nl/BankWithdraw",
                "pt_pt/BankWithdraw",
                "ru_ru/BankWithdraw",
                "zh_cn/BankWithdraw");
        addManager("ConfigManager", new EconomyConfigManager(this));
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
        boolean transientUsers = getConfigManager().isTransientUsers();
        if (transientUsers)
            addTransientWalletOwnerManager("DepositorManager",
                    x -> x, crudable ->
                            new BlobDepositor(crudable, this),
                    "BlobDepositor",
                    true,
                    DepositorLoadEvent::new,
                    DepositorUnloadEvent::new,
                    EventPriority.NORMAL,
                    EventPriority.NORMAL);
        else
            addWalletOwnerManager("DepositorManager",
                    x -> x, crudable ->
                            new BlobDepositor(crudable, this),
                    "BlobDepositor",
                    true,
                    DepositorLoadEvent::new,
                    DepositorUnloadEvent::new);
        getDepositorManager().setNotEnoughEvent(notEnoughBalance -> {
            WalletOwner walletOwner = notEnoughBalance.owner();
            BlobDepositor blobDepositor = (BlobDepositor) walletOwner;
            String currency = notEnoughBalance.currency();
            double missing = notEnoughBalance.missing();
            Wallet bankWallet = blobDepositor.getBankWallet();
            double current = bankWallet.get(currency);
            if (current < missing){
                return false;
            }
            bankWallet.put(currency, current - missing);
            return true;
        });
        getCurrencyDirector().whenObjectManagerFilesLoad(manager -> {
            try {
                withdrawerUI = WithdrawerUI.getInstance(this);
                traderUI = TraderUI.getInstance(this);
                bankUI = BankUI.getInstance(this);
                Bukkit.getScheduler().runTask(getPlugin(), () -> {
                    getDepositorManager().registerEconomy(manager.getObject("default"),
                            getCurrencyDirector());
                    getDepositorManager().registerDefaultEconomyCommand(getCurrencyDirector());
                    Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), () -> {
                        getDepositorManager().registerPlaceholderAPIExpansion();
                    }, 20L);
                    BankCommand.INSTANCE.load();
                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void reload() {
        getConfigManager().reload();
        getCurrencyDirector().reload();
        getCurrencyDirector().whenReloaded(() ->
                getTradeableDirector().reload());
        bankUI.reload();
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
    public final EconomyConfigManager getConfigManager() {
        return getManager("ConfigManager", EconomyConfigManager.class);
    }
}