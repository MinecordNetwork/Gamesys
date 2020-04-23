package net.minecord.gamesys.system

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.player.GamePlayer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File

interface System {
    fun createArena(name: String, file: File, locations: HashMap<String, MutableList<Vector>>, minPlayers: Int? = null, maxPlayers: Int? = null): Arena

    fun createGame(plugin: Gamesys, arena: Arena): Game

    fun createGamePlayer(plugin: Gamesys, player: Player): GamePlayer

    fun getArenaBlockMapping(): HashMap<String, Material>

    fun getSpawnLocation(): Location

    fun isItemThrowingAllowed(): Boolean

    fun isHungerBarDisabled(): Boolean

    fun dropItemsAfterDeath(): Boolean
}
