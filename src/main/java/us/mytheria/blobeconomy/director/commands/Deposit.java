package us.mytheria.blobeconomy.director.commands;

import me.anjoismysign.anjo.entities.Result;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.blobeconomy.entities.BlobDepositor;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobChildCommand;
import us.mytheria.bloblib.entities.BlobExecutor;
import us.mytheria.bloblib.entities.ExecutorData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Deposit {

    public static boolean command(ExecutorData data, EconomyManagerDirector director) {
        String[] args = data.args();
        BlobExecutor executor = data.executor();
        CommandSender sender = data.sender();
        Result<BlobChildCommand> result = executor
                .isChildCommand("deposit", args);
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
        BlobDepositor depositor = director.getDepositorManager().isWalletOwner(player).orElseThrow();
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) continue;
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            if (!container.has(director.getNamespacedKey("tangibleCurrencyKey"),
                    PersistentDataType.STRING))
                continue;
            String x = container.get(director.getNamespacedKey("tangibleCurrencyKey"),
                    PersistentDataType.STRING);
            if (!container.has(director.getNamespacedKey("tangibleCurrencyDenomination"),
                    PersistentDataType.STRING))
                continue;
            String y = container.get(director.getNamespacedKey("tangibleCurrencyDenomination"),
                    PersistentDataType.STRING);
            int itemAmount = itemStack.getAmount();
            BigDecimal amount = new BigDecimal(y).multiply(new BigDecimal(itemAmount));
            depositor.deposit(x, amount.doubleValue());
            itemStack.setAmount(0);
        }
        return true;
    }

    public static List<String> tabCompleter(ExecutorData data) {
        String[] args = data.args();
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1)
            suggestions.add("deposit");
        if (args.length == 2) {
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList());
        }
        return suggestions;
    }
}
