package net.minecord.gamesys.game

import net.minecord.gamesys.Gamesys
import org.bukkit.scheduler.BukkitRunnable

class GameManager(val plugin: Gamesys) {
    private val games = mutableListOf<Game>()

    fun enable() {
        object : BukkitRunnable() {
            override fun run() {
                if (getAvailableGames().filter { it.status != GameStatus.STARTING }.size < plugin.system.getMinimumPreparedGamesCount()) {
                    addGame()
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)
    }

    fun disable() {
        var count = games.size
        while (count > 0) {
            count--
            games[count].onGameEnd()
        }
    }

    fun addGame() {
        plugin.logger.logInfo("Creating new game")
        val game = plugin.system.createGame(plugin, plugin.arenaManager.arenas.random())
        games.add(game)
        plugin.worldManager.loadGame(game)
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
        return games.filter { (it.status == GameStatus.WAITING || it.status == GameStatus.STARTING || it.status == GameStatus.PREPARING) && !it.isFull() }
    }
}