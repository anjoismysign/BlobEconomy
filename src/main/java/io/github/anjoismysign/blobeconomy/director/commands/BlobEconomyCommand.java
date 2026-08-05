package io.github.anjoismysign.blobeconomy.director.commands;

import io.github.anjoismysign.blobeconomy.BlobEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.currency.Currency;
import io.github.anjoismysign.bloblib.utility.TextColor;
import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandBuilder;
import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.BukkitCommandTarget;
import io.github.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum BlobEconomyCommand {
    INSTANCE;

    public void load(){
        BlobEconomyAPI api = BlobEconomyAPI.getInstance();

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

        Command blobeconomy = CommandBuilder.of("blobeconomy").build();
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

        Command eco = CommandBuilder.of("eco").permission("blobeconomy.admin").build();

        Command give = eco.child("give");
        give.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), amountTarget, currencyTarget);
        give.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            BlobEconomyCommandContext context = BlobEconomyCommandContext.WITH_AMOUNT(args, sender);
            if (context == null){
                return;
            }
            context.walletOwner().deposit(context.currency().getKey(), context.amount());
            BlobLibMessageAPI.getInstance()
                    .getMessage("Economy.Deposit", sender)
                    .modify(s -> s.replace("%display%", context.currency().display(context.amount()))
                            .replace("%currency%", context.currency().getDisplayName(context.player()))
                            .replace("%player%", context.player().getName()))
                    .toCommandSender(sender);
        });

        Command take = eco.child("take");
        take.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), amountTarget, currencyTarget);
        take.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            BlobEconomyCommandContext context = BlobEconomyCommandContext.WITH_AMOUNT(args, sender);
            if (context == null){
                return;
            }
            if (!context.walletOwner().has(context.currency().getKey(), context.amount())) {
                double missing = context.amount() - context.walletOwner().getBalance(context.currency().getKey());
                BlobLibMessageAPI.getInstance()
                        .getMessage("Economy.Cannot-Bankrupt-Others", sender)
                        .modify(s -> s.replace("%display%", context.currency().display(missing))
                                .replace("%currency%", context.currency().getDisplayName(context.player()))
                                .replace("%player%", context.player().getName()))
                        .toCommandSender(sender);
                return;
            }
            context.walletOwner().withdraw(context.currency().getKey(), context.amount());
            BlobLibMessageAPI.getInstance()
                    .getMessage("Economy.Withdraw", sender)
                    .modify(s -> s.replace("%display%", context.currency().display(context.amount()))
                            .replace("%currency%", context.currency().getDisplayName(context.player()))
                            .replace("%player%", context.player().getName()))
                    .toCommandSender(sender);
        });

        Command set = eco.child("set");
        set.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), amountTarget, currencyTarget);
        set.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            BlobEconomyCommandContext context = BlobEconomyCommandContext.WITH_AMOUNT(args, sender);
            if (context == null){
                return;
            }
            context.walletOwner().setBalance(context.currency().getKey(), context.amount());
            BlobLibMessageAPI.getInstance()
                    .getMessage("Economy.Set", sender)
                    .modify(s -> s.replace("%display%", context.currency().display(context.amount()))
                            .replace("%currency%", context.currency().getDisplayName(context.player()))
                            .replace("%player%", context.player().getName()))
                    .toCommandSender(sender);
        });

        Command reset = eco.child("reset");
        reset.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), currencyTarget);
        reset.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            BlobEconomyCommandContext context = BlobEconomyCommandContext.WITHOUT_AMOUNT(args, sender);
            if (context == null){
                return;
            }
            context.walletOwner().reset(context.currency());
            BlobLibMessageAPI.getInstance()
                    .getMessage("Economy.Reset", sender)
                    .modify(s -> s.replace("%currency%", context.currency().getDisplayName(context.player()))
                            .replace("%player%", context.player().getName()))
                    .toCommandSender(sender);
        });
    }
}
