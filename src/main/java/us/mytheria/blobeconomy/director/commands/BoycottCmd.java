package us.mytheria.blobeconomy.director.commands;

import me.anjoismysign.anjo.entities.Result;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobChildCommand;
import us.mytheria.bloblib.entities.BlobExecutor;
import us.mytheria.bloblib.entities.ExecutorData;

import java.util.ArrayList;
import java.util.List;

public class BoycottCmd {

    public static boolean command(ExecutorData data, EconomyManagerDirector director) {
        String[] args = data.args();
        BlobExecutor executor = data.executor();
        CommandSender sender = data.sender();
        Result<BlobChildCommand> result = executor
                .isChildCommand("boycott", args);
        if (!result.isValid())
            return false;
        Player player;
        int seconds = 600;
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
            if (args.length >= 3) {
                try {
                    seconds = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    BlobLibMessageAPI.getInstance()
                            .getMessage("Economy.Number-Exception", sender)
                            .toCommandSender(sender);
                    return true;
                }
            }
        } else {
            if (!(sender instanceof Player)) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.Console-Not-Allowed-Command", sender)
                        .toCommandSender(sender);
                return true;
            }
            player = (Player) sender;
        }
        director.getTradeableDirector().boycott(player, seconds);
        return true;
    }

    public static List<String> tabCompleter(ExecutorData data) {
        String[] args = data.args();
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("boycott");
        }
        if (args.length == 2) {
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList());
        }
        return suggestions;
    }
}
