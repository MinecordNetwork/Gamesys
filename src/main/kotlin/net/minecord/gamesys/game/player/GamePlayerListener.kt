package net.minecord.gamesys.game.player

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.GameStatus
import net.minecord.gamesys.utils.ProtocolSupport
import net.minecord.gamesys.utils.colored
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.scheduler.BukkitRunnable

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

    @EventHandler(priority = EventPriority.LOWEST)
    fun onSelfDamage(e: EntityDamageEvent) {
        if (e.entity !is Player) return

        val victim = plugin.gamePlayerManager.get(e.entity as Player)
        val game = victim.game ?: return

        if (game.invinciblePlayers || game.status != GameStatus.RUNNING) {
            victim.player.fireTicks = 0
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun playerRespawnEvent(e: PlayerRespawnEvent) {
        val player = plugin.gamePlayerManager.get(e.player)

        if (player.game?.status == GameStatus.RUNNING) {
            e.respawnLocation = e.player.location
        }
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        e.deathMessage = null

        if (!plugin.system.dropItemsAfterDeath()) {
            e.drops.clear()
        }

        val player = plugin.gamePlayerManager.get(e.entity)
        val version = ProtocolSupport.getProtocolVersion(player.player)

        if (version in 0..47) { //1.8 version respawn fix
            object : BukkitRunnable() {
                override fun run() {
                    player.player.spigot().respawn()
                }
            }.runTaskLater(plugin, 1)

        } else {
            player.player.health = e.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        }

        player.game?.onPlayerDeath(player, null, player.lastAttacker)
    }

    @EventHandler
    fun onChat(e: PlayerCommandPreprocessEvent) {
        val player = plugin.gamePlayerManager.get(e.player)
        val game = player.game

        if (game != null) {
            val cmd = e.message.split(" ")[0].replace("/", "")
            val allowed = plugin.system.getAllowedCommands()
            val blocked = plugin.system.getBlockedCommands()

            if ((blocked.isNotEmpty() && blocked.contains(cmd)) || !allowed.contains(cmd)) {
                if (e.player.hasPermission("gamesys.admin.allow-commands") || e.player.hasPermission("${plugin.name.toLowerCase()}}.admin.allow-commands")) {
                    return
                }

                e.player.sendMessage("${plugin.system.getChatPrefix()} &7This command is disabled, if you want to leave, type &c/leave".colored())
                e.isCancelled = true
            }
        }
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
