package net.minecord.gamesys.game.player

import net.minecord.gamesys.Gamesys
import org.bukkit.entity.Player
import java.util.*

open class GamePlayerManager(private val plugin: Gamesys) {
    val players = hashMapOf<UUID, GamePlayer>()

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
