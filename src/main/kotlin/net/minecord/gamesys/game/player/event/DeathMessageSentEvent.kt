package net.minecord.gamesys.game.player.event

import net.minecord.gamesys.game.player.GamePlayer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageEvent

class DeathMessageSentEvent(val player: GamePlayer, val cause: EntityDamageEvent.DamageCause?, val killer: GamePlayer?, var deathMessage: String) : Event() {
    private val handlers = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlers
    }

    fun getHandlerList(): HandlerList? {
        return handlers
    }
}
