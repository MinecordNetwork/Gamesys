package net.minecord.gamesys.game

import com.sk89q.worldedit.EditSession
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.gamesys.game.player.GamePlayerStatus
import net.minecord.gamesys.utils.colored
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.craftbukkit.v1_15_R1.boss.CraftBossBar
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.atan2 as atan21

open class Game(val plugin: Gamesys, val arena: Arena) {
    var status: GameStatus = GameStatus.PREPARING
    val locations = hashMapOf<String, MutableList<Location>>()
    val players = mutableListOf<GamePlayer>()
    val gameBar = CraftBossBar("&f&lWaiting for more players".colored(), BarColor.WHITE, BarStyle.SEGMENTED_12)

    open fun onArenaLoaded(editSession: EditSession, origin: Location) {
        for ((string, vectors) in arena.locations) {
            locations[string] = mutableListOf()
            vectors.forEach {
                it.add(origin.toVector())

                val location = Location(origin.world, it.x, it.y, it.z).add(0.5, 0.toDouble(), 0.5)

                location.yaw = (atan21(
                    y = -(origin.x - location.x),
                    x = origin.z - location.z
                ) * (180.0 / Math.PI)).toFloat()
                location.pitch = 0f

                locations[string]?.add(location)
            }
        }
        status = GameStatus.WAITING
    }

    open fun onPlayerJoined(player: GamePlayer) {
        players.add(player)
        gameBar.addPlayer(player.player)
        player.game = this
        player.status = GamePlayerStatus.PLAYING
        player.setGameMode(getLobbyMode(player))
        player.teleport(getLobbyLocation(player))
        if (status == GameStatus.WAITING && players.size >= getMinimumRequiredPlayers()) {
            onCountdownStarted()
        }
    }

    open fun onPlayerLeft(player: GamePlayer) {
        players.remove(player)
        player.game = null
        player.status = GamePlayerStatus.NONE
        player.setGameMode(getLobbyMode(player))
        player.teleport(plugin.system.getSpawnLocation())
    }

    open fun onPlayerDeath(player: GamePlayer, cause: EntityDamageEvent.DamageCause? = null, killer: GamePlayer? = null) {
        player.deaths++

        if (killer != null) {
            killer.kills++
        }

        onDeathMessageSent(player, cause, killer)
        onPlayerStartsToRespawn(player)
    }

    open fun onDeathMessageSent(player: GamePlayer, cause: EntityDamageEvent.DamageCause? = null, killer: GamePlayer? = null) {
        if (killer != null) sendMessage("&c${player.player.name} &7was killed by &a${killer.player.name}")
        else sendMessage("&7player &c${player.player.name} &7died")
    }

    open fun onPlayerStartsToRespawn(player: GamePlayer) {
        player.player.setItemOnCursor(null)
        player.player.inventory.clear()
        player.setGameMode(GameMode.SPECTATOR)
        player.status = GamePlayerStatus.RESPAWNING

        object : BukkitRunnable() {
            var counter = getRespawnCooldown()
            override fun run() {
                if (counter <= 0) {
                    onPlayerSpawn(player)
                    cancel()
                    return
                }
                player.player.sendTitle("&e&l$counter".colored(), "&f&lRespawning".colored(), 0, 25, 0)
                counter--
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)
    }

    open fun onPlayerSpawn(player: GamePlayer) {
        player.player.health = player.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value!!
        player.player.foodLevel = 20
        player.setGameMode(getGameMode(player))
        player.teleport(getRespawnLocation(player))
        player.player.inventory.clear()
        for ((slot, item) in getGameItems(player)) {
            player.player.inventory.setItem(slot, item)
        }
    }

    open fun onStartWaiting() {
        status = GameStatus.WAITING
        gameBar.setTitle("&f&lWaiting for more players".colored())
    }

    open fun onCountdownStarted() {
        status = GameStatus.STARTING
        var countdown = getCountdownSeconds()
        object : BukkitRunnable() {
            override fun run() {
                if (players.size < getMinimumRequiredPlayers()) {
                    onStartWaiting()
                    cancel()
                    return
                }
                when {
                    countdown <= 0 -> {
                        onGameStart()
                        cancel()
                        return
                    }
                    countdown <= 10 || countdown % 10 == 0 -> {
                        players.forEach {
                            it.player.playSound(it.player.location, Sound.UI_BUTTON_CLICK, 10f, 1f)
                            it.player.sendTitle("&e&l$countdown".colored(), "&f&lThe Game is Starting".colored(), 0, 60, 20)
                        }
                    }
                }
                gameBar.setTitle("&f&lGame starts in &e&l$countdown &f&lseconds".colored())
                gameBar.progress = (countdown / countdown).toDouble()
                countdown--
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)
    }

    open fun onGameStart() {
        status = GameStatus.RUNNING
        gameBar.isVisible = false
        players.forEach {
            onPlayerSpawn(it)
            it.player.playSound(it.player.location, Sound.BLOCK_ANVIL_USE, 10.toFloat(), 1.toFloat())
            it.player.sendTitle("", "&e&lThe Game has Begun".colored(), 0, 60, 20)
        }
    }

    open fun onGameEnd(winner: GamePlayer) {
        status = GameStatus.ENDING
        sendMessage("&fPlayer ${winner.player.name} is the winner!")
        players.forEach {
            onPlayerLeft(it)
        }
    }

    open fun sendMessage(message: String) {
        players.forEach {
            it.player.sendMessage(message.colored())
        }
    }

    open fun getChestLocations(): MutableList<Location> {
        return locations["chests"]!!
    }

    open fun getSpawnLocations(): MutableList<Location> {
        return locations["spawns"]!!
    }

    open fun getRespawnLocation(gamePlayer: GamePlayer): Location {
        return getSpawnLocations().random()
    }

    open fun getLobbyLocation(gamePlayer: GamePlayer): Location {
        return getSpawnLocations().random()
    }

    open fun getGameItems(gamePlayer: GamePlayer): HashMap<Int, ItemStack> {
        return hashMapOf()
    }

    open fun getLobbyItems(gamePlayer: GamePlayer): HashMap<Int, ItemStack> {
        return hashMapOf()
    }

    open fun getGameMode(gamePlayer: GamePlayer): GameMode {
        return GameMode.SURVIVAL
    }

    open fun getLobbyMode(gamePlayer: GamePlayer): GameMode {
        return GameMode.ADVENTURE
    }

    open fun getMinimumRequiredPlayers(): Int {
        return 1
    }

    open fun getRespawnCooldown(): Int {
        return 3
    }

    open fun getCountdownSeconds(): Int {
        return 25
    }
}
