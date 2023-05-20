package com.github.liamdev06.mc.sddodgeball.commands.core;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple way to create and work with commands.
 */
public abstract class AbstractPlayerCommand extends Command {

    private final @NonNull List<AbstractPlayerSubcommand> subcommands;
    private final @NonNull TabCompleteData tabData;

    public AbstractPlayerCommand(@NonNull String command, @NonNull TabCompleteData tabData) {
        super(command);
        this.tabData = tabData;
        this.subcommands = new ArrayList<>();
        this.init();
    }

    public AbstractPlayerCommand(@NonNull String command, @NonNull TabCompleteData tabData, AbstractPlayerSubcommand... subcommands) {
        super(command);
        this.tabData = tabData;
        this.subcommands = Arrays.asList(subcommands);
        this.init();
    }

    /**
     * Register the command to the command map.
     * This makes it possible to register commands without having to put them in the plugin.yml.
     */
    private void init() {
        try {
            Server server = Bukkit.getServer();

            // Get the command map field
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            // Get the command map instance and register the command
            CommandMap commandMap = (CommandMap) commandMapField.get(server);
            commandMap.register("command", this);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Call a sub-command by a specific id. This is used to organize the code more and keep it
     * more clean by being able to move sub commands to new classes.
     *
     * @param subcommandId The ID of the sub-command to call.
     */
    public void callSubcommand(@NonNull Player player, @NonNull String subcommandId, @NonNull String[] args) {
        for (AbstractPlayerSubcommand subcommand : this.subcommands) {
            if (subcommand.getSubcommandId().equals(subcommandId)) {
                subcommand.onPlayerSubcommand(player, args);
                break;
            }
        }
    }

    public abstract void onPlayerCommand(@NonNull Player player, @NonNull String[] args);

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (sender instanceof Player) {
            this.onPlayerCommand((Player) sender, args);
        }

        return false;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<TabOption> options = this.tabData.getOptions();

        if (!(sender instanceof Player) || options.isEmpty()) {
            return Collections.emptyList();
        }

        // Iterate over all possible options in the tab data
        for (TabOption option : this.tabData.getOptions()) {
            if (option.getPositionOnArgument() == args.length) {
                int argPos = args.length;
                if (args.length == 1) {
                    argPos = 0;
                }

                if (args.length >= 2) {
                    argPos-= 1;
                }

                // Add all options that match to list and send them
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[argPos], option.getOptions(), completions);
                Collections.sort(completions);
                return completions;
            }
        }

        return Collections.emptyList();
    }
}