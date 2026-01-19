package io.github.anjoismysign.blobeconomy.director.commands;

import io.github.anjoismysign.blobeconomy.BlobEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.bloblib.entities.currency.Wallet;
import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandBuilder;
import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.BukkitCommandTarget;
import io.github.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum BankCommand {
    INSTANCE;

    public void load(){
        BlobEconomyAPI api = BlobEconomyAPI.getInstance();

        Command bank = CommandBuilder.of("bank").build();
        CommandTarget<Double> amountTarget = new CommandTarget<>() {
            @Override
            public List<String> get() {
                return List.of("Type the amount here");
            }

            @Override
            public @Nullable Double parse(String amountString) {
                try {
                    double amount = Double.parseDouble(amountString);
                    return amount > 0 ? amount : null;
                } catch (NumberFormatException var3) {
                    return null;
                }
            }
        };

        Command give = bank.child("give");
        give.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), amountTarget);
        give.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            Wallet bankWallet = BlobEconomyAPI.getInstance().getBankWallet(target);
            Currency currency = api.getDefaultCurrency();
            String key = currency.getKey();
            double current = bankWallet.getOrDefault(key, 0.0);
            @Nullable Double parsed = amountTarget.parse(args[1]);
            if (parsed == null){
                return;
            }
            bankWallet.put(currency.getKey(), current+parsed);
        });

        Command take = bank.child("take");
        take.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), amountTarget);
        take.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            Wallet bankWallet = BlobEconomyAPI.getInstance().getBankWallet(target);
            Currency currency = api.getDefaultCurrency();
            String key = currency.getKey();
            double current = bankWallet.getOrDefault(key, 0.0);
            @Nullable Double parsed = amountTarget.parse(args[1]);
            if (parsed == null){
                return;
            }
            double total = current - parsed;
            if (total <= 0.0){
                total = 0.0;
            }
            bankWallet.put(currency.getKey(), total);
        });

        CommandTarget<Currency> currencyTarget = new CommandTarget<>() {
            @Override
            public List<String> get() {
                return api.getAllCurrencies().stream().map(Currency::getKey).toList();
            }

            @Override
            public @Nullable Currency parse(String currencyKey) {
                return api.getAllCurrencies().stream().filter(currency -> currency.getKey().equals(currencyKey)).findFirst().orElse(null);
            }
        };

        Command giveCurrency = bank.child("giveCurrency");
        giveCurrency.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), amountTarget, currencyTarget);
        giveCurrency.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            Wallet bankWallet = BlobEconomyAPI.getInstance().getBankWallet(target);
            @Nullable Currency currency = currencyTarget.parse(args[2]);
            if (currency == null){
                return;
            }
            String key = currency.getKey();
            double current = bankWallet.getOrDefault(key, 0.0);
            @Nullable Double parsed = amountTarget.parse(args[1]);
            if (parsed == null){
                return;
            }
            double total = current + parsed;
            bankWallet.put(currency.getKey(), total);
        });

        Command takeCurrency = bank.child("takeCurrency");
        takeCurrency.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), amountTarget, currencyTarget);
        takeCurrency.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            Wallet bankWallet = BlobEconomyAPI.getInstance().getBankWallet(target);
            @Nullable Currency currency = currencyTarget.parse(args[2]);
            if (currency == null){
                return;
            }
            String key = currency.getKey();
            double current = bankWallet.getOrDefault(key, 0.0);
            @Nullable Double parsed = amountTarget.parse(args[1]);
            if (parsed == null){
                return;
            }
            double total = current - parsed;
            if (total <= 0.0){
                total = 0.0;
            }
            bankWallet.put(currency.getKey(), total);
        });
    }
}
