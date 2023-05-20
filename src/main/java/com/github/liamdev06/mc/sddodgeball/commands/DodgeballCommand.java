package com.github.liamdev06.mc.sddodgeball.commands;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.commands.subcommands.JoinSubcommand;
import com.github.liamdev06.mc.sddodgeball.utility.PermissionHelper;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegister;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegistry;
import com.github.liamdev06.mc.sddodgeball.commands.core.AbstractPlayerCommand;
import com.github.liamdev06.mc.sddodgeball.commands.core.TabCompleteData;
import com.github.liamdev06.mc.sddodgeball.commands.core.TabOption;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MessageHelper;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MsgReplace;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * /dodgeball command. The general player public command.
 */
@AutoRegister(type = AutoRegistry.Type.COMMAND)
public class DodgeballCommand extends AbstractPlayerCommand {

    public DodgeballCommand(@NonNull DodgeballPlugin plugin) {
        super("dodgeball", new TabCompleteData(
                        new TabOption(1,
                                "join")
                ),
                new JoinSubcommand(plugin)
                );
    }

    @Override
    public void onPlayerCommand(@NonNull Player player, @NonNull String[] args) {
        // Check if the player has the base permission to use the command
        if (!PermissionHelper.hasCommandPermission(player, "use")) {
            MessageHelper.sendMessage(player, "command.dodgeball.no-permission", new MsgReplace("permission", "dodgeball.command.use"));
            return;
        }

        // Check if the player did not provide any subcommand
        if (args.length == 0) {
            MessageHelper.sendMessage(player, "command.dodgeball.invalid-argument");
            return;
        }

        // Check if the argument is the join
        if (args[0].equalsIgnoreCase("join")) {
            this.callSubcommand(player, "dodgeball_join", args);
        } else {
            MessageHelper.sendMessage(player, "command.dodgeball.invalid-argument");
        }
    }
}