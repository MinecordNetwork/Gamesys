package net.minecord.gamesys.game

import net.minecord.gamesys.Gamesys
import org.bukkit.scheduler.BukkitRunnable

class GameManager(val plugin: Gamesys) {
    val games = mutableListOf<Game>()

    fun enable() {
        object : BukkitRunnable() {
            override fun run() {
                if (getAvailableGames().size < plugin.system.getMinumumPreparedGamesCount()) {
                    addGame()
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)
    }

    fun disable() {
        for (game in games) {
            game.onGameEnd()
        }
    }

    fun addGame() {
        plugin.logger.logInfo("Creating new game")
        games.add(plugin.system.createGame(plugin, plugin.arenaManager.arenas.random()))
    }

    fun removeGame(game: Game) {
        games.remove(game)
    }

    fun getSuitableGame(): Game? {
        return getReadyGames().sortedByDescending { it.players.size }.getOrNull(0)
    }

    fun getReadyGames(): List<Game> {
        return getAvailableGames().filter { it.status != GameStatus.PREPARING }
    }

    fun getAvailableGames(): List<Game> {
        return games.filter { it.status == GameStatus.WAITING || it.status == GameStatus.STARTING || it.status == GameStatus.PREPARING }
    }
}