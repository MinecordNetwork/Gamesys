package net.minecord.gamesys.game.portal

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.GameStatus
import net.minecord.gamesys.utils.colored
import net.minecord.gamesys.utils.instantFirework
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.random.Random

open class GamePortal(val plugin: Gamesys, val location: Location) {
    private val hologram: Hologram = HologramsAPI.createHologram(plugin, location)

    init {
        object : BukkitRunnable() {
            override fun run() {
                val world = location.world ?: return
                for (player in world.players) {
                    val block: Block = player.location.block
                    if (block.type ==  Material.END_GATEWAY || block.getRelative(BlockFace.DOWN).type === Material.END_GATEWAY) {
                        object : BukkitRunnable() {
                            override fun run() {
                                val game = plugin.gameManager.getSuitableGame()
                                if (game == null) {
                                    val randomDir = if (Random.nextInt(2) == 0) -1 else 1
                                    player.playSound(player.location, Sound.ENTITY_CREEPER_HURT, 10f, 1f)
                                    player.velocity = Vector(randomDir * 12 * 0.05, 12 * 0.1, randomDir * 12 * 0.05)
                                    player.sendTitle("", "&c&lNo games available".colored(), 5, 40, 10)
                                } else {
                                    instantFirework(FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BURST).build(), player.location)
                                    game.onPlayerJoined(plugin.gamePlayerManager.get(player))
                                }
                            }
                        }.runTask(plugin)
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 7)
    }

    open fun updateHologram() {
        if (plugin.isEnabled) {
            object : BukkitRunnable() {
                override fun run() {
                    hologram.clearLines()

                    val game = plugin.gameManager.getSuitableGame()

                    if (game == null) {
                        hologram.insertTextLine(0, "&c&lNo game available".colored())
                    } else {
                        hologram.insertTextLine(0, "&b&l${game.arena.name}".colored())
                        hologram.insertTextLine(1, "&f&l${game.players.size} / ${game.getMaximumPlayers()}".colored())
                        if (game.status == GameStatus.WAITING) {
                            hologram.insertTextLine(2, "&e&lWaiting for players".colored())
                        } else {
                            hologram.insertTextLine(2, "&f&lStarting in &a&l${game.startCountdownCounter}".colored())
                        }
                    }
                }
            }.runTask(plugin)
        }
    }

    fun removeHologram() {
        hologram.delete()
    }
}