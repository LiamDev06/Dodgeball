package com.github.liamdev06.mc.sddodgeball.utility.messaging.lang;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibActionBar;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibColor;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibTitle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * Utility and helper class to send messages such as regular messages, titles and action bars
 * from the messages.yml file.
 */
public class MessageHelper {

    private static final @NonNull DodgeballPlugin PLUGIN = DodgeballPlugin.getInstance();
    private static final @NonNull FileConfiguration MSG_CONFIG = PLUGIN.getMessagesConfig();

    /**
     * Send a message to the player from the messages.yml file.
     *
     * @param player An online player to send the message to.
     * @param configKey The key in the {@link MessageHelper#MSG_CONFIG} configuration.
     * @param replacements Array of replacements to replace in the final formatted message.
     */
    public static void sendMessage(@NonNull Player player, @NonNull String configKey, MsgReplace... replacements) {
        // Check if the key exists
        if (isKeyMissing(configKey)) {
            return;
        }

        // Get the message from the configuration
        String message = MSG_CONFIG.getString(configKey);

        // Apply replacements and format
        message = formatWithReplacements(message, replacements);
        message = applyDefaultFormat(message);

        // Send the message to the player
        player.sendMessage(message);
    }

    /**
     * Send an action bar to the player from the messages.yml file.
     *
     * @param player An online player to send the action bar to.
     * @param configKey The key in the {@link MessageHelper#MSG_CONFIG} configuration.
     * @param replacements Array of replacements to replace in the final formatted action bar message.
     */
    public static void sendActionBar(@NonNull Player player, @NonNull String configKey, MsgReplace... replacements) {
        // Check if the key exists
        if (isKeyMissing(configKey)) {
            return;
        }

        // Get the message from the configuration
        String message = MSG_CONFIG.getString(configKey);

        // Apply replacements and format
        message = formatWithReplacements(message, replacements);
        message = applyDefaultFormat(message);

        // Send the action bar to the player
        LibActionBar.sendActionBar(player, message);
    }

    /**
     * Send a title to the player from the messages.yml file.
     *
     * @param player An online player to send the message to.
     * @param titleConfigKey The key for the title "title" in the {@link MessageHelper#MSG_CONFIG} configuration.
     * @param subtitleKey The key for the title subtitle in the {@link MessageHelper#MSG_CONFIG} configuration.
     * @param replacements Array of replacements to replace in the final formatted message.
     */
    public static void sendTitle(@NonNull Player player, @NonNull String titleConfigKey, @NonNull String subtitleKey, MsgReplace... replacements) {
        // Check if the title or subtitle key exists
        if (isKeyMissing(titleConfigKey) || isKeyMissing(subtitleKey)) {
            return;
        }

        // Get the title message from the configuration, and format it
        String title = MSG_CONFIG.getString(titleConfigKey);
        title = formatWithReplacements(title, replacements);
        title = applyDefaultFormat(title);

        // Get the subtitle message from the configuration, and format it
        String subtitle = MSG_CONFIG.getString(subtitleKey);
        subtitle = formatWithReplacements(subtitle, replacements);
        subtitle = applyDefaultFormat(subtitle);

        LibTitle.sendTitle(player, 15, 70, 20, title, subtitle);
    }

    /**
     * Get a message from the messages.yml file with the correct replacements and formatting.
     *
     * @param configKey The key in the {@link MessageHelper#MSG_CONFIG} configuration.
     * @param replacements Array of replacements to replace in the final formatted message.
     * @return The message from the {@link MessageHelper#MSG_CONFIG} configuration.
     */
    public static String getMessage(@NonNull String configKey, MsgReplace... replacements) {
        // Check if the key exists
        if (isKeyMissing(configKey)) {
            return "";
        }

        // Get the message from the configuration
        String message = MSG_CONFIG.getString(configKey);

        // Apply replacements and format
        message = formatWithReplacements(message, replacements);
        message = applyDefaultFormat(message);

        return message;
    }

    /**
     * Get a List of messages from the messages.yml file with the correct replacements and formatting.
     *
     * @param configKey The key in the {@link MessageHelper#MSG_CONFIG} file.
     * @param replacements Array of replacements to replace in the final formatted message.
     * @return A list of String messages from the {@link MessageHelper#MSG_CONFIG} configuration.
     */
    public static List<String> getMessages(@NonNull String configKey, MsgReplace... replacements) {
        // Check if the key exists
        if (isKeyMissing(configKey)) {
            return Collections.emptyList();
        }

        List<String> list = MSG_CONFIG.getStringList(configKey);
        for (int i = 0; i < list.size(); i++) {
            String message = list.get(i);

            // Apply replacements and format
            message = formatWithReplacements(message, replacements);
            message = applyDefaultFormat(message);

            // Replace the message in the list to update with correct format
            list.set(i, message);
        }

        return list;
    }

    /**
     * Applies default format to the input string by colorizing it,
     * as well as swapping {newline} to an actual new line.
     *
     * @param input The message to format.
     * @return The formatted version of the message.
     */
    private static String applyDefaultFormat(@NonNull String input) {
        input = input.replace("{newline}", "\n");
        return LibColor.colorMessage(input);
    }

    /**
     * Swaps out all message replacements ({@link MsgReplace}) with their actual replacement.
     *
     * @param message The message to replace replacements in.
     * @param replacements All {@link MsgReplace} objects to find the {@link MsgReplace#replace()} and {@link MsgReplace#replaceWith()} in
     * @return The formatted version with replacements of the message.
     */
    private static String formatWithReplacements(@NonNull String message, MsgReplace... replacements) {
        if (replacements.length > 0) {
            for (MsgReplace replace : replacements) {
                message = message.replace(replace.replace(), String.valueOf(replace.replaceWith()));
            }
        }

        return applyDefaultFormat(message);
    }

    /**
     * Checks if a key is missing from the messages configuration ({@link MessageHelper#MSG_CONFIG}) and if so, logs
     * a warning message to the console notify which key in which file.
     *
     * @param configKey The key in the config to check existence for.
     * @return True if the {@param configKey} is missing from the {@link MessageHelper#MSG_CONFIG}, otherwise returning false if it exists.
     */
    private static boolean isKeyMissing(@NonNull String configKey) {
        if (!MSG_CONFIG.contains(configKey)) {
            PLUGIN.getLogger().severe("The " + MSG_CONFIG.getName() + " has an error! The file is missing the " + configKey + " message." + "\n" +
                    "Handle this ASAP since players will not receive the missing messages until fixed.");
            return true;
        }

        return false;
    }
}