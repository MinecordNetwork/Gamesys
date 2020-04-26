package net.minecord.gamesys.command

import net.minecord.gamesys.Gamesys
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class StartCommand(private val plugin: Gamesys) : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, strings: Array<String>): Boolean {
        if (commandSender.hasPermission("oneshot.start")) {
            val game = plugin.gameManager.getSuitableGame()

            if (game != null) {
                game.startCountdownCounter = 10
            }
        }

        return true
    }
}
