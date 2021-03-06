package net.minecord.gamesys.system

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.sidebar.GameSidebar
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.gamesys.game.portal.GamePortal
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File

interface System {
    fun createArena(name: String, file: File, locations: HashMap<String, ArrayList<Vector>>): Arena

    fun createGame(plugin: Gamesys, arena: Arena): Game

    fun createGamePlayer(plugin: Gamesys, player: Player): GamePlayer

    fun createGameSidebar(plugin: Gamesys, game: Game): GameSidebar

    fun createGamePortal(plugin: Gamesys, location: Location): GamePortal

    fun getAllowedCommands(): MutableList<String>

    fun getBlockedCommands(): MutableList<String>

    fun getArenaBlockMapping(): HashMap<String, Material>

    fun getSpawnLocation(): Location

    fun getChatPrefix(): String

    fun getMinimumPreparedGamesCount(): Int

    fun isArenaProtected(): Boolean

    fun isLobbyProtected(): Boolean

    fun getProperty(property: SystemProperty): Boolean
}
