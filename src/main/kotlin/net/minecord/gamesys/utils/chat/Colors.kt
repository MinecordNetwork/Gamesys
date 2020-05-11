package net.minecord.gamesys.utils.chat

import org.bukkit.ChatColor

fun String.colored(colorChar: Char = '&'): String {
    return ChatColor.translateAlternateColorCodes(colorChar, this)
}
