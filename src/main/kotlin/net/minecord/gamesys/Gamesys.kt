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
import net.minecord.gamesys.system.BaseSystem
import net.minecord.gamesys.system.System
import net.minecord.gamesys.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

open class Gamesys: JavaPlugin() {
    val arenaManager: ArenaManager by lazy { ArenaManager(this) }
    val gameManager: GameManager by lazy { GameManager(this) }
    val gamePlayerManager: GamePlayerManager by lazy { GamePlayerManager(this) }
    val gamePortalManager: GamePortalManager by lazy { GamePortalManager(this) }
    val worldManager: WorldManager by lazy { WorldManager(this) }
    val logger: Logger by lazy { Logger(this) }
    val configReader: ConfigReader by lazy { ConfigReader(this) }
    open val system: System by lazy { BaseSystem(this) }

    override fun onEnable() {
        gamePlayerManager.enable()
        arenaManager.enable()
        worldManager.enable()
        gamePortalManager.enable()

        getCommand("join")!!.setExecutor(JoinCommand(this))
        getCommand("leave")!!.setExecutor(LeaveCommand(this))
        getCommand("start")!!.setExecutor(StartCommand(this))

        Bukkit.getPluginManager().registerEvents(GamePlayerListener(this), this)
    }

    override fun onDisable() {
        gamePortalManager.disable()
        gameManager.disable()
        gamePlayerManager.disable()
    }
}
