package io.github.anjoismysign.blobeconomy.director.commands;

import io.github.anjoismysign.blobeconomy.BlobEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.currency.Currency;
import io.github.anjoismysign.bloblib.utility.TextColor;
import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandBuilder;
import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.github.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum BlobEconomyCommand {
    INSTANCE;

    public void load(){
        BlobEconomyAPI api = BlobEconomyAPI.getInstance();

        Command blobeconomy = CommandBuilder.of("blobeconomy").build();
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

        Command display = blobeconomy.child("display");
        display.setParameters(currencyTarget, amountTarget);
        display.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            if (args.length < 2){
                sender.sendMessage(TextColor.PARSE("&c/blobeconomy display <currency> <amount>"));
                return;
            }
            @Nullable Currency currency = currencyTarget.parse(args[0]);
            if (currency == null){
                return;
            }
            String key = currency.getKey();
            @Nullable Double amount = amountTarget.parse(args[1]);
            if (amount == null){
                return;
            }
            sender.sendMessage(BlobLibEconomyAPI.getInstance().getElasticEconomy().getImplementation(key).format(amount));
        });
    }
}
