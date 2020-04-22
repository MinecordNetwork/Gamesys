package net.minecord.gamesys.game

import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.gamesys.game.player.GamePlayerStatus
import org.bukkit.Location

open class Game(val arena: Arena) {
    var status = GameStatus.PREPARING
    lateinit var locations: HashMap<String, MutableList<Location>>
    lateinit var players: MutableList<GamePlayer>

    fun onArenaLoaded(center: Location) {
        for ((string, vectors) in arena.locations) {
            locations[string] = mutableListOf()
            vectors.forEach {
                locations[string]?.add(Location(center.world, center.x + it.x, center.y + it.y, center.z + it.z))
            }
        }
        status = GameStatus.WAITING
    }

    fun onPlayerJoined(player: GamePlayer) {
        players.add(player)
        player.game = this
        player.status = GamePlayerStatus.PLAYING
    }

    fun onPlayerLeft(player: GamePlayer) {
        players.remove(player)
        player.game = null
        player.status = GamePlayerStatus.NONE
    }

    fun getChestLocations(): MutableList<Location> {
        return locations["chests"]!!
    }

    fun getSpawnLocations(): MutableList<Location> {
        return locations["spawns"]!!
    }
}
