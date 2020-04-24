package net.minecord.gamesys.game.board

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.xoreboardutil.bukkit.XoreBoardUtil
import org.bukkit.Bukkit

open class GameBoard(val plugin: Gamesys, val game: Game) {
    private val board = XoreBoardUtil.getNextXoreBoard()

    open fun getTitle(): String {
        return "The Title"
    }

    open fun getLines(): HashMap<String, Int> {
        return hashMapOf()
    }

    open fun addPlayer(player: GamePlayer) {
        board.addPlayer(player.player)
        update()
    }

    open fun removePlayer(player: GamePlayer) {
        board.removePlayer(player.player)
        update()
    }

    open fun update() {
        //TODO: Limit to 15 lines
        for (player in game.players) {
            Bukkit.broadcastMessage("Updating sidebar for ${player.player.name}")
            val private = board.getPlayer(player.player).privateSidebar
            private.displayName = getTitle()
            private.rewriteLines(getLines())
            private.showSidebar()
        }
    }
}
