package io.github.anjoismysign.blobeconomy.director.commands;

import io.github.anjoismysign.anjo.entities.Result;
import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.BlobChildCommand;
import io.github.anjoismysign.bloblib.entities.BlobExecutor;
import io.github.anjoismysign.bloblib.entities.ExecutorData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WithdrawerCmd {

    public static boolean command(ExecutorData data, EconomyManagerDirector director) {
        String[] args = data.args();
        BlobExecutor executor = data.executor();
        CommandSender sender = data.sender();
        Result<BlobChildCommand> result = executor
                .isChildCommand("withdrawer", args);
        if (!result.isValid())
            return false;
        Player player;
        if (args.length >= 2) {
            if (!sender.hasPermission("blobeconomy.admin")) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.No-Permission", sender)
                        .toCommandSender(sender);
                return true;
            }
            String input = args[1];
            Player target = Bukkit.getPlayer(input);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return true;
            }
            player = target;
        } else {
            if (!(sender instanceof Player)) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.Console-Not-Allowed-Command", sender)
                        .toCommandSender(sender);
                return true;
            }
            player = (Player) sender;
        }
        BlobDepositor depositor = director.getDepositorManager()
                .isWalletOwner(player).orElse(null);
        if (depositor == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                    .toCommandSender(sender);
            return true;
        }
        depositor.chooseAndWithdrawCurrency();
        return true;
    }

    public static List<String> tabCompleter(ExecutorData data) {
        String[] args = data.args();
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("withdrawer");
        }
        if (args.length == 2) {
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList());
        }
        return suggestions;
    }
}
