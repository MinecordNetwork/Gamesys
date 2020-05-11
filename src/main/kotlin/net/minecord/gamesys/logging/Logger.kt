package net.minecord.gamesys.logging

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.utils.chat.colored
import org.bukkit.Bukkit

class Logger(val plugin: Gamesys) {
    fun logInfo(message: String) {
        Bukkit.getConsoleSender().sendMessage(("&e[${plugin.name}] &b[INFO] &f$message".colored()))
    }

    fun logError(message: String) {
        Bukkit.getConsoleSender().sendMessage(("&e[${plugin.name}] &c[ERROR] &f$message".colored()))
    }

    fun logWarning(message: String) {
        Bukkit.getConsoleSender().sendMessage(("&e[${plugin.name}] &6[WARNING] &f$message".colored()))
    }

    fun logDebug(message: String) {
        Bukkit.getConsoleSender().sendMessage(("&e[${plugin.name}] &d[DEBUG] &f$message".colored()))
    }
}
