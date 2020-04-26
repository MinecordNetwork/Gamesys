package net.minecord.gamesys.command

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.GameStatus
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class StartCommand(private val plugin: Gamesys) : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, strings: Array<String>): Boolean {
        if (commandSender.hasPermission("gamesys.admin.start") || commandSender.hasPermission("${plugin.name.toLowerCase()}}.admin.start")) {
            val game = plugin.gameManager.getSuitableGame()

            if (game != null) {
                when (game.status) {
                    GameStatus.STARTING -> {
                        game.startCountdownCounter = 10
                    }
                    GameStatus.WAITING -> {
                        game.onGameStart()
                    }
                    else -> {
                        commandSender.sendMessage("Game is already running")
                    }
                }
            }
        }

        return true
    }
}
