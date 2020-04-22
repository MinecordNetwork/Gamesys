package net.minecord.gamesys.system

import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.player.GamePlayer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File

open class BaseSystem : System {
    override fun createArena(name: String, file: File, locations: HashMap<String, MutableList<Vector>>, minPlayers: Int?, maxPlayers: Int?): Arena {
        return Arena(name, file, locations, minPlayers, maxPlayers)
    }

    override fun createGame(arena: Arena): Game {
        return Game(arena)
    }

    override fun createGamePlayer(player: Player): GamePlayer {
        return GamePlayer(player)
    }

    override fun getArenaBlockMapping(): HashMap<String, Material> {
        val map = hashMapOf<String, Material>()

        map["spawns"] = Material.WHITE_GLAZED_TERRACOTTA
        map["chests"] = Material.CHEST

        return map
    }
}
