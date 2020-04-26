package net.minecord.gamesys.command

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.utils.colored
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class JoinCommand(private val plugin: Gamesys) : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, strings: Array<String>): Boolean {
        val game = plugin.gameManager.getSuitableGame()

        if (game != null) {
            game.onPlayerJoined(plugin.gamePlayerManager.get(commandSender as Player))
        } else {
            commandSender.sendMessage("${plugin.system.getChatPrefix()} &cThere is no available game right now".colored())
        }

        return true
    }
}
