package net.minecord.gamesys.arena

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.GameStatus
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent

class ArenaListener(val plugin: Gamesys): Listener {
    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        checkAndCancel(e.player, e)
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        checkAndCancel(e.player, e)
    }

    @EventHandler
    fun onPlayerArmorStandManipulateEvent(e: PlayerArmorStandManipulateEvent) {
        checkAndCancel(e.player, e)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onArmorStandBreak(e: EntityDamageByEntityEvent) {
        if (e.entity is ArmorStand && e.damager is Player) {
            checkAndCancel(e.damager as Player, e)
        }
    }

    private fun checkAndCancel(player: Player, e: Cancellable) {
        plugin.gamePlayerManager.get(player).game?.let {
            when (it.status) {
                GameStatus.WAITING, GameStatus.STARTING -> {
                    if (plugin.system.isLobbyProtected()) {
                        e.isCancelled = true
                    }
                }
                GameStatus.RUNNING, GameStatus.ENDING, GameStatus.ENDED -> {
                    if (plugin.system.isLobbyProtected()) {
                        e.isCancelled = true
                    }
                }
                else -> {}
            }
        }
    }
}
