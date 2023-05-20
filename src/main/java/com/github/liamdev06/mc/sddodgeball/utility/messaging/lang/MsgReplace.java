package com.github.liamdev06.mc.sddodgeball.utility.messaging.lang;

import org.bukkit.entity.Player;

/**
 * Used with {@link MessageHelper} class to add replacements in a message string.
 *
 * @param replace The string to replace with, without the curly braces. For example 'player' (becoming {player}) or 'title' (becoming {title}).
 * @param replaceWith The object to replace the replacement string with. For example replacing the {player} with {@link Player#getName()}
 */
public record MsgReplace(String replace, Object replaceWith) {

    public MsgReplace(String replace, Object replaceWith) {
        this.replace = "{" + replace + "}";
        this.replaceWith = replaceWith;
    }
}
