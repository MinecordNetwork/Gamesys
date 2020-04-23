package net.minecord.gamesys.system

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.player.GamePlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File

open class BaseSystem : System {
    override fun createArena(name: String, file: File, locations: HashMap<String, ArrayList<Vector>>, minPlayers: Int?, maxPlayers: Int?): Arena {
        return Arena(name, file, locations, minPlayers, maxPlayers)
    }

    override fun createGame(plugin: Gamesys, arena: Arena): Game {
        return Game(plugin, arena)
    }

    override fun createGamePlayer(plugin: Gamesys, player: Player): GamePlayer {
        return GamePlayer(plugin, player)
    }

    override fun getArenaBlockMapping(): HashMap<String, Material> {
        val map = hashMapOf<String, Material>()

        map["spawns"] = Material.WHITE_GLAZED_TERRACOTTA
        map["chests"] = Material.CHEST

        return map
    }

    override fun getSpawnLocation(): Location {
        return Location(Bukkit.getWorld("EventWorld"), 683.toDouble(), 186.toDouble(), 131.toDouble())
    }

    override fun isItemThrowingAllowed(): Boolean {
        return true
    }

    override fun isHungerBarDisabled(): Boolean {
        return false
    }

    override fun dropItemsAfterDeath(): Boolean {
        return true
    }
}
