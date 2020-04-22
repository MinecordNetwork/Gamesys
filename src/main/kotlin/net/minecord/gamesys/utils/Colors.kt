package net.minecord.gamesys.utils

import org.bukkit.ChatColor

fun String.colored(colorChar: Char = '&'): String {
    return ChatColor.translateAlternateColorCodes(colorChar, this)
}
