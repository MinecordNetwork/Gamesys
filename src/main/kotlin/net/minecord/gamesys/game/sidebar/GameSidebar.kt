package net.minecord.gamesys.game.sidebar

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.xoreboardutil.bukkit.XoreBoardUtil

open class GameSidebar(open val plugin: Gamesys, open val game: Game) {
    private val board = XoreBoardUtil.getNextXoreBoard()

    open fun getTitle(player: GamePlayer): String {
        return "The Title"
    }

    open fun getLines(player: GamePlayer): HashMap<String, Int> {
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
        var playersCount = game.players.size
        while (playersCount > 0) {
            playersCount--
            val player = game.players[playersCount]
            val private = board.getPlayer(player.player).privateSidebar
            private.displayName = getTitle(player)
            private.rewriteLines(getLines(player))
            private.showSidebar()
        }
    }
}
