package us.mytheria.blobeconomy.director.commands;

import me.anjoismysign.anjo.entities.Result;
import me.anjoismysign.anjo.entities.Uber;
import org.bukkit.command.CommandSender;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobChildCommand;
import us.mytheria.bloblib.entities.BlobExecutor;
import us.mytheria.bloblib.entities.ExecutorData;
import us.mytheria.bloblib.entities.currency.Currency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Withdraw {

    public static boolean command(ExecutorData data, EconomyManagerDirector director) {
        String[] args = data.args();
        BlobExecutor executor = data.executor();
        CommandSender sender = data.sender();
        Result<BlobChildCommand> result = executor
                .isChildCommand("withdraw", args);
        if (!result.isValid())
            return false;
        Uber<Boolean> uber = Uber.drive(false);
        executor.ifInstanceOfPlayer(sender, player -> {
            if (args.length < 2) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobEconomy.Withdraw-Usage", sender)
                        .toCommandSender(sender);
                uber.talk(true);
                return;
            }
            String input = args[1];
            BigDecimal amount;
            int operation = 0; // 0 = amount, 1 = all, 2 = half
            try {
                amount = new BigDecimal(input);
            } catch (NumberFormatException e) {
                Set<String> allKeywords = director.getConfigManager().getWithdrawAllKeywords();
                Set<String> halfKeywords = director.getConfigManager().getWithdrawHalfKeywords();
                if (allKeywords.contains(input)) {
                    amount = null;
                    operation = 1;
                } else if (halfKeywords.contains(input)) {
                    amount = null;
                    operation = 2;
                } else {
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobEconomy.Withdraw-Usage", sender)
                            .toCommandSender(sender);
                    uber.talk(true);
                    return;
                }
            }
            String currencyKey = "default";
            if (args.length >= 3)
                currencyKey = args[2];
            BlobDepositor depositor = director.getDepositorManager()
                    .isWalletOwner(player).orElse(null);
            if (depositor == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                uber.talk(true);
                return;
            }
            if (amount == null) {
                if (operation == 1) {
                    double balance = depositor.getBalance(currencyKey);
                    amount = new BigDecimal(balance);
                } else {
                    double balance = depositor.getBalance(currencyKey);
                    amount = new BigDecimal(balance / 2);
                }
            }
            Currency currency = director.getCurrencyDirector()
                    .getObjectManager().getObject(currencyKey);
            if (currency == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Currency.Not-Found", player)
                        .handle(player);
                uber.talk(true);
                return;
            }
            depositor.withdrawTargetCurrency(amount, currency);
            uber.talk(true);
        });
        return uber.thanks();
    }

    public static List<String> tabCompleter(ExecutorData data, EconomyManagerDirector director) {
        String[] args = data.args();
        BlobExecutor executor = data.executor();
        List<String> suggestions = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                suggestions.add("withdraw");
                return suggestions;
            }
            case 3 -> {
                if (executor.isChildCommand("withdraw", args).isValid())
                    suggestions.addAll(director.getCurrencyDirector().getObjectManager().keys());
                return suggestions;
            }
            default -> {
                return suggestions;
            }
        }
    }
}
