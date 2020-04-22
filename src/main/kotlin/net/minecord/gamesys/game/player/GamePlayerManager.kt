package net.minecord.gamesys.game.player

import net.minecord.gamesys.Gamesys
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

open class GamePlayerManager(private val plugin: Gamesys) {
    val players = hashMapOf<UUID, GamePlayer>()

    fun loadPlayers() {
        for (player: Player in Bukkit.getOnlinePlayers()) {
            addPlayer(player)
        }
    }

    fun unloadPlayers() {
        for (player: Player in Bukkit.getOnlinePlayers()) {
            removePlayer(player)
        }
    }

    fun get(player: Player): GamePlayer {
        return players[player.uniqueId]!!
    }

    fun addPlayer(player: Player) {
        players[player.uniqueId] = plugin.system.createGamePlayer(player)
    }

    fun removePlayer(player: Player) {
        val gamePlayer = get(player)

        get(player).game?.onPlayerLeft(gamePlayer)

        players.remove(player.uniqueId)
    }
}
