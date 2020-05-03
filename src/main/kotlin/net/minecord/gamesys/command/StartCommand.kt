package net.minecord.gamesys.command

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.GameStatus
import net.minecord.gamesys.utils.colored
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
                        if (game.startCountdownCounter > 10) {
                            game.startCountdownCounter = 10
                        }
                    }
                    GameStatus.WAITING -> {
                        game.onGameStart()
                    }
                    else -> {
                        commandSender.sendMessage("${plugin.system.getChatPrefix()} &cYour game is already running".colored())
                    }
                }
            }
        }

        return true
    }
}
