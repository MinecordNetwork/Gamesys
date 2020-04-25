package net.minecord.gamesys

import net.minecord.gamesys.arena.ArenaManager
import net.minecord.gamesys.game.GameManager
import net.minecord.gamesys.game.player.GamePlayerListener
import net.minecord.gamesys.game.player.GamePlayerManager
import net.minecord.gamesys.logging.Logger
import net.minecord.gamesys.system.System
import net.minecord.gamesys.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

open class Gamesys: JavaPlugin() {
    val arenaManager: ArenaManager = ArenaManager(this)
    val gameManager: GameManager = GameManager(this)
    val worldManager: WorldManager = WorldManager(this)
    val gamePlayerManager: GamePlayerManager = GamePlayerManager(this)
    val logger: Logger = Logger(this)
    lateinit var system: System

    fun run(factory: System) {
        system = factory

        gamePlayerManager.enable()
        arenaManager.enable()
        worldManager.enable()

        Bukkit.getPluginManager().registerEvents(GamePlayerListener(this), this)
    }

    fun stop() {
        gameManager.disable()
        gamePlayerManager.disable()
    }
}
