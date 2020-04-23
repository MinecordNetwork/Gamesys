package net.minecord.gamesys.game.player.event

import net.minecord.gamesys.game.player.GamePlayer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageEvent
import org.jetbrains.annotations.NotNull

class DeathMessageSentEvent(val player: GamePlayer, val cause: EntityDamageEvent.DamageCause?, val killer: GamePlayer?, var deathMessage: String) : Event() {
    private val handlers = HandlerList()

    override fun getHandlers(): @NotNull HandlerList {
        return handlers
    }

    fun getHandlerList(): HandlerList? {
        return handlers
    }
}
