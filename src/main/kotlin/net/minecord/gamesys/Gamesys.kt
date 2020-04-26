package net.minecord.gamesys

import net.minecord.gamesys.arena.ArenaManager
import net.minecord.gamesys.command.JoinCommand
import net.minecord.gamesys.command.LeaveCommand
import net.minecord.gamesys.command.StartCommand
import net.minecord.gamesys.config.ConfigReader
import net.minecord.gamesys.game.GameManager
import net.minecord.gamesys.game.player.GamePlayerListener
import net.minecord.gamesys.game.player.GamePlayerManager
import net.minecord.gamesys.game.portal.GamePortalManager
import net.minecord.gamesys.logging.Logger
import net.minecord.gamesys.system.System
import net.minecord.gamesys.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

open class Gamesys: JavaPlugin() {
    val arenaManager: ArenaManager = ArenaManager(this)
    val gameManager: GameManager = GameManager(this)
    val gamePlayerManager: GamePlayerManager = GamePlayerManager(this)
    val gamePortalManager: GamePortalManager = GamePortalManager(this)
    val worldManager: WorldManager = WorldManager(this)
    val logger: Logger = Logger(this)
    val configReader: ConfigReader = ConfigReader(this)
    lateinit var system: System

    fun run(factory: System) {
        system = factory

        gamePlayerManager.enable()
        arenaManager.enable()
        worldManager.enable()
        gamePortalManager.enable()

        getCommand("join")!!.setExecutor(JoinCommand(this))
        getCommand("leave")!!.setExecutor(LeaveCommand(this))
        getCommand("start")!!.setExecutor(StartCommand(this))

        Bukkit.getPluginManager().registerEvents(GamePlayerListener(this), this)
    }

    fun stop() {
        gamePortalManager.disable()
        gameManager.disable()
        gamePlayerManager.disable()
    }
}
