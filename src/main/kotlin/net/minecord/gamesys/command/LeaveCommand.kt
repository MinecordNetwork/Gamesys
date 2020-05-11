package net.minecord.gamesys.command

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.utils.chat.colored
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LeaveCommand(private val plugin: Gamesys) : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, strings: Array<String>): Boolean {
        val player = plugin.gamePlayerManager.get(commandSender as Player);
        val game = player.game

        if (game != null) {
            game.onPlayerLeft(player)
        } else {
            commandSender.sendMessage("${plugin.system.getChatPrefix()} &cYou are not currently in any game".colored())
        }

        return true
    }
}
