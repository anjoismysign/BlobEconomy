package us.mytheria.blobeconomy.director.commands;

import me.anjoismysign.anjo.entities.Result;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobChildCommand;
import us.mytheria.bloblib.entities.BlobExecutor;
import us.mytheria.bloblib.entities.ExecutorData;

import java.util.ArrayList;
import java.util.List;

public class TraderCmd {

    public static boolean command(ExecutorData data, EconomyManagerDirector director) {
        String[] args = data.args();
        BlobExecutor executor = data.executor();
        CommandSender sender = data.sender();
        Result<BlobChildCommand> result = executor
                .isChildCommand("trader", args);
        if (!result.isValid())
            return false;
        Player player;
        if (args.length >= 2) {
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
        depositor.chooseAndTradeCurrency();
        return true;
    }

    public static List<String> tabCompleter(ExecutorData data, EconomyManagerDirector director) {
        String[] args = data.args();
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("trader");
        }
        if (args.length == 2) {
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList());
        }
        return suggestions;
    }
}
