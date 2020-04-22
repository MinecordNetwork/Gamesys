package net.minecord.gamesys.game

import com.sk89q.worldedit.EditSession
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.gamesys.game.player.GamePlayerStatus
import org.bukkit.Location
import kotlin.math.atan2 as atan21

open class Game(val plugin: Gamesys, val arena: Arena) {
    var status = GameStatus.PREPARING
    val locations = hashMapOf<String, MutableList<Location>>()
    val players = mutableListOf<GamePlayer>()

    fun onArenaLoaded(editSession: EditSession, origin: Location) {
        for ((string, vectors) in arena.locations) {
            locations[string] = mutableListOf()
            vectors.forEach {
                it.add(origin.toVector())

                val location = Location(origin.world, it.x, it.y, it.z).add(0.5, 0.toDouble(), 0.5)

                location.yaw = (atan21(
                    y = -(origin.x - location.x),
                    x = origin.z - location.z
                ) * (180.0 / Math.PI)).toFloat()
                location.pitch = 0f

                locations[string]?.add(location)
            }
        }
        status = GameStatus.WAITING
    }

    fun onPlayerJoined(player: GamePlayer) {
        players.add(player)
        player.game = this
        player.status = GamePlayerStatus.PLAYING
        player.player.teleport(getLobbbyLocation(player))
    }

    fun onPlayerLeft(player: GamePlayer) {
        players.remove(player)
        player.game = null
        player.status = GamePlayerStatus.NONE
        player.player.teleport(plugin.system.getSpawnLocation())
    }

    fun getChestLocations(): MutableList<Location> {
        return locations["chests"]!!
    }

    fun getSpawnLocations(): MutableList<Location> {
        return locations["spawns"]!!
    }

    fun getRespawnLocation(gamePlayer: GamePlayer): Location {
        return getSpawnLocations().random()
    }

    fun getLobbbyLocation(gamePlayer: GamePlayer): Location {
        return getSpawnLocations().random()
    }
}
