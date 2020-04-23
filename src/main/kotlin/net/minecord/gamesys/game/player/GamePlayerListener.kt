package net.minecord.gamesys.game.player

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.GameStatus
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

class GamePlayerListener(private val plugin: Gamesys) : Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        plugin.gamePlayerManager.addPlayer(e.player)
        e.joinMessage = null
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onQuit(e: PlayerQuitEvent) {
        plugin.gamePlayerManager.removePlayer(e.player)
        e.quitMessage = null
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDamage(e: EntityDamageByEntityEvent) {
        if (e.entity !is Player) return

        val victim = plugin.gamePlayerManager.get(e.entity as Player)
        if (victim.game == null) return

        if (victim.game!!.status == GameStatus.RUNNING) {
            val attacker: Player
            val damagedBy = e.damager

            attacker = if (damagedBy is Projectile && damagedBy.shooter is Player)
                damagedBy.shooter as Player
            else if (damagedBy is Player) {
                damagedBy
            } else return

            plugin.gamePlayerManager.get(e.entity as Player).onAttack(plugin.gamePlayerManager.get(attacker), e.cause)

        } else {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player: GamePlayer = plugin.gamePlayerManager.get(e.entity)
        player.player.health = player.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        e.deathMessage = null

        if (!plugin.system.dropItemsAfterDeath()) {
            e.drops.clear()
        }

        player.game?.onPlayerDeath(player, null, player.lastAttacker)
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (!plugin.system.isItemThrowingAllowed()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerHunger(event: FoodLevelChangeEvent) {
        if (plugin.system.isHungerBarDisabled()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onSpectatorTeleport(e: PlayerTeleportEvent) {
        if (e.cause == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            e.isCancelled = true
        }
    }
}
