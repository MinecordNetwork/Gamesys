package net.minecord.gamesys.game

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena

class GameManager(val plugin: Gamesys) {
    lateinit var testGame: Game

    fun createTestGame(arena: Arena): Game {
        testGame = plugin.system.createGame(plugin, arena)
        plugin.worldManager.loadGame(testGame)
        return testGame
    }
}