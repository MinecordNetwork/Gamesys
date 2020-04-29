package net.minecord.gamesys.system

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.sidebar.GameSidebar
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.gamesys.game.portal.GamePortal
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File

open class BaseSystem(val plugin: Gamesys) : System {
    override fun createArena(name: String, file: File, locations: HashMap<String, ArrayList<Vector>>): Arena {
        return Arena(name, file, locations)
    }

    override fun createGame(plugin: Gamesys, arena: Arena): Game {
        return Game(plugin, arena)
    }

    override fun createGamePlayer(plugin: Gamesys, player: Player): GamePlayer {
        return GamePlayer(plugin, player)
    }

    override fun createGameSidebar(plugin: Gamesys, game: Game): GameSidebar {
        return GameSidebar(plugin, game)
    }

    override fun createGamePortal(plugin: Gamesys, location: Location): GamePortal {
        return GamePortal(plugin, location)
    }

    override fun getAllowedCommands(): MutableList<String> {
        return mutableListOf("join", "leave", "server", "hub", "lobby", "start", "msg", "r", "find")
    }

    override fun getBlockedCommands(): MutableList<String> {
        return mutableListOf()
    }

    override fun getArenaBlockMapping(): HashMap<String, Material> {
        val map = hashMapOf<String, Material>()

        map["spawns"] = Material.WHITE_GLAZED_TERRACOTTA
        map["chests"] = Material.CHEST

        return map
    }

    override fun getSpawnLocation(): Location {
        val world = plugin.gamePortalManager.portal.location.world
        return world?.spawnLocation ?: Location(Bukkit.getWorld("world"), 0.0, 60.0, 0.0)
    }

    override fun getChatPrefix(): String {
        return "&e&lMinigame &f&l●&7"
    }

    override fun getMinimumPreparedGamesCount(): Int {
        return 3
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
