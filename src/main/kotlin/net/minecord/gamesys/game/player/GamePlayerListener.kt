package net.minecord.gamesys.game.player

import net.minecord.gamesys.Gamesys
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class GamePlayerListener(private val api: Gamesys) : Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        api.gamePlayerManager.addPlayer(e.player)
        e.joinMessage = null
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onQuit(e: PlayerQuitEvent) {
        api.gamePlayerManager.removePlayer(e.player)
        e.quitMessage = null
    }
}
