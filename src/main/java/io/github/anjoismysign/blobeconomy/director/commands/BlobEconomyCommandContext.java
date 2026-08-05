package io.github.anjoismysign.blobeconomy.director.commands;

import io.github.anjoismysign.blobeconomy.BlobEconomyAPI;
import io.github.anjoismysign.blobeconomy.entities.BlobDepositor;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.currency.Currency;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record BlobEconomyCommandContext(
        Currency currency,
        BlobDepositor walletOwner,
        Player player,
        double amount) {

    @Nullable
    public static BlobEconomyCommandContext WITH_AMOUNT(String[] args, CommandSender sender) {
        if (args.length < 2)
            return null;
        String inputPlayer = args[0];
        Player player = Bukkit.getPlayer(inputPlayer);
        if (player == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Found", sender)
                    .toCommandSender(sender);
            return null;
        }
        BlobDepositor walletOwner = BlobEconomyAPI.getInstance().getDepositor(player);
        if (walletOwner == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                    .toCommandSender(sender);
            return null;
        }
        String input = args[1];
        double amount;
        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Economy.Number-Exception", sender)
                    .toCommandSender(sender);
            return null;
        }
        Currency currency = BlobEconomyAPI.getInstance().getDefaultCurrency();
        if (args.length >= 3) {
            String inputCurrency = args[2];
            currency = BlobEconomyAPI.getInstance().getCurrency(inputCurrency);
            if (currency == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Currency.Not-Found", sender)
                        .toCommandSender(sender);
                return null;
            }
        }
        return new BlobEconomyCommandContext(currency, walletOwner, player, amount);
    }

    @Nullable
    public static BlobEconomyCommandContext WITHOUT_AMOUNT(String[] args, CommandSender sender) {
        if (args.length < 1) {
            return null;
        }
        String inputPlayer = args[0];
        Player player = Bukkit.getPlayer(inputPlayer);
        if (player == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Found", sender)
                    .toCommandSender(sender);
            return null;
        }
        BlobDepositor walletOwner = BlobEconomyAPI.getInstance().getDepositor(player);
        if (walletOwner == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                    .toCommandSender(sender);
            return null;
        }
        Currency currency = BlobEconomyAPI.getInstance().getDefaultCurrency();
        if (args.length >= 2) {
            String inputCurrency = args[1];
            currency = BlobEconomyAPI.getInstance().getCurrency(inputCurrency);
            if (currency == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Currency.Not-Found", sender)
                        .toCommandSender(sender);
                return null;
            }
        }
        return new BlobEconomyCommandContext(currency, walletOwner, player, 0);
    }
}
