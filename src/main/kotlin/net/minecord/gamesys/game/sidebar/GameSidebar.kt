package net.minecord.gamesys.game.sidebar

import fr.mrmicky.fastboard.adventure.FastBoard
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.player.GamePlayer
import java.util.*

open class GameSidebar(open val plugin: Gamesys, open val game: Game) {
    private val boards = hashMapOf<UUID, FastBoard>()

    open fun getTitle(player: GamePlayer): String {
        return "The Title"
    }

    open fun getLines(player: GamePlayer): ArrayList<String> {
        return arrayListOf()
    }

    open fun addPlayer(player: GamePlayer) {
        boards[player.player.uniqueId] = FastBoard(player.player)
        update()
    }

    open fun removePlayer(player: GamePlayer) {
        boards[player.player.uniqueId]?.delete()
        boards.remove(player.player.uniqueId)
    }

    open fun update() {
        var playersCount = game.players.size
        while (playersCount > 0) {
            playersCount--
            val player = game.players[playersCount]
            val board = boards[player.player.uniqueId]!!

            board.updateTitle(MiniMessage.miniMessage().deserialize(getTitle(player)))
            board.updateLines(*getLines(player).map { MiniMessage.miniMessage().deserialize(it) }.toTypedArray())
        }
    }

    open fun destroy() {
        boards.forEach { it.value.delete()}
    }
}
