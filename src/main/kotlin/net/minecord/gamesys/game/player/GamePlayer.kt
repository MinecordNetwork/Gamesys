package net.minecord.gamesys.game.player

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.Game
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.scheduler.BukkitRunnable

open class GamePlayer(val plugin: Gamesys, val player: Player) {
    var status = GamePlayerStatus.NONE
    var game: Game? = null
    var lastAttacker: GamePlayer? = null
    var lastDamageCause: EntityDamageEvent.DamageCause? = null
    var kills = 0
    var deaths = 0

    fun onAttack(lastAttacker: GamePlayer, damageCause: EntityDamageEvent.DamageCause) {
        this.lastAttacker = lastAttacker
        this.lastDamageCause = damageCause
        object : BukkitRunnable() {
            override fun run() {
                if (this@GamePlayer.lastAttacker != null && this@GamePlayer.lastAttacker == lastAttacker) this@GamePlayer.lastAttacker = null
                if (this@GamePlayer.lastDamageCause != null && this@GamePlayer.lastDamageCause == damageCause) this@GamePlayer.lastDamageCause = null
            }
        }.runTaskLaterAsynchronously(plugin, 100)
    }

    fun teleport(location: Location) {
        object : BukkitRunnable() {
            override fun run() {
                player.teleport(location)
            }
        }.runTask(plugin)
    }

    fun setGameMode(gameMode: GameMode) {
        object : BukkitRunnable() {
            override fun run() {
                player.gameMode = gameMode
            }
        }.runTask(plugin)
    }
}
