package net.minecord.gamesys.game.player

import net.minecord.gamesys.game.Game
import org.bukkit.entity.Player

open class GamePlayer(val player: Player) {
    var status = GamePlayerStatus.NONE
    var game: Game? = null
}
