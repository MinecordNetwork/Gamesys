package net.minecord.gamesys.game

import com.sk89q.worldedit.EditSession
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.gamesys.game.player.GamePlayerStatus
import net.minecord.gamesys.game.player.event.DeathMessageSentEvent
import net.minecord.gamesys.utils.colored
import net.minecord.gamesys.utils.instantFirework
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.craftbukkit.v1_15_R1.boss.CraftBossBar
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random
import kotlin.math.atan2 as atan21

open class Game(val plugin: Gamesys, val arena: Arena) {
    var status: GameStatus = GameStatus.PREPARING
    val locations = hashMapOf<String, ArrayList<Location>>()
    lateinit var lobbyLocation: Location
    val players = arrayListOf<GamePlayer>()
    val bar = CraftBossBar("&f&lWaiting for more players".colored(), BarColor.WHITE, BarStyle.SEGMENTED_12)
    val board = plugin.system.createGameBoard(plugin, this)

    open fun onArenaLoaded(editSession: EditSession, origin: Location, lobbyLocation: Location) {
        for ((string, vectors) in arena.locations) {
            locations[string] = arrayListOf()
            vectors.forEach {
                val vector = origin.toVector().add(it)
                val location = Location(origin.world, vector.x, vector.y, vector.z).add(0.5, 0.toDouble(), 0.5)

                location.yaw = (atan21(
                    y = -(origin.x - location.x),
                    x = origin.z - location.z
                ) * (180.0 / Math.PI)).toFloat()
                location.pitch = 0f

                locations[string]?.add(location)
            }
        }
        this.lobbyLocation = lobbyLocation
        status = GameStatus.WAITING
    }

    open fun onPlayerJoined(player: GamePlayer) {
        players.add(player)
        bar.addPlayer(player.player)
        board.addPlayer(player)
        player.game = this
        player.status = GamePlayerStatus.PLAYING
        player.player.gameMode = player.getLobbyGameMode()
        player.player.teleport(getLobbyLocation(player))
        player.storeAndClearInventory()
        if (status == GameStatus.WAITING && players.size >= getMinimumRequiredPlayers()) {
            onStartCountdownStart()
        }
    }

    open fun onPlayerLeft(player: GamePlayer) {
        players.remove(player)
        bar.removePlayer(player.player)
        board.removePlayer(player)
        player.kills = 0
        player.deaths = 0
        player.game = null
        player.status = GamePlayerStatus.NONE
        player.player.gameMode = player.getLobbyGameMode()
        player.player.teleport(plugin.system.getSpawnLocation())
        player.restoreInventory()
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
        val message: String = if (killer != null) {
            "&c${player.player.name} &7was killed by &a${killer.player.name}"
        } else {
            "&7player &c${player.player.name} &7died"
        }

        val event = DeathMessageSentEvent(player, cause, killer, message)

        Bukkit.getPluginManager().callEvent(event)

        sendMessage(event.deathMessage)
    }

    open fun onPlayerStartsToRespawn(player: GamePlayer) {
        player.player.setItemOnCursor(null)
        player.player.inventory.clear()
        player.player.gameMode = GameMode.SPECTATOR
        player.status = GamePlayerStatus.RESPAWNING

        object : BukkitRunnable() {
            var counter = getRespawnCooldown()
            override fun run() {
                if (counter <= 0) {
                    object : BukkitRunnable() {
                        override fun run() {
                            onPlayerSpawn(player)
                        }
                    }.runTask(plugin)
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
        player.player.gameMode = player.getDefaultGameMode()
        player.player.teleport(getRespawnLocation(player))
        player.player.inventory.clear()
        for ((slot, item) in player.getGameItems()) {
            player.player.inventory.setItem(slot, item)
        }
    }

    open fun onStartWaiting() {
        status = GameStatus.WAITING
        bar.setTitle("&f&lWaiting for more players".colored())
    }

    open fun onStartCountdownStart() {
        status = GameStatus.STARTING
        var countdown = getStartCountdown()
        object : BukkitRunnable() {
            override fun run() {
                if (players.size < getMinimumRequiredPlayers()) {
                    onStartWaiting()
                    cancel()
                    return
                }
                when {
                    countdown <= 0 -> {
                        object : BukkitRunnable() {
                            override fun run() {
                                onGameStart()
                            }
                        }.runTask(plugin)
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
                bar.setTitle("&f&lGame starts in &e&l$countdown &f&lseconds".colored())
                bar.progress = (countdown / countdown).toDouble()
                countdown--
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)
    }

    open fun onEndCountdownStart(winner: GamePlayer? = null) {
        status = GameStatus.ENDING
        var countdown = getEndCountdown()
        object : BukkitRunnable() {
            override fun run() {
                when {
                    countdown <= 0 -> {
                        object : BukkitRunnable() {
                            override fun run() {
                                onGameEnd(winner)
                            }
                        }.runTask(plugin)
                        cancel()
                        return
                    }
                    else -> {
                        players.forEach {
                            it.player.playSound(it.player.location, Sound.UI_BUTTON_CLICK, 3f, 1f)
                            if (winner != null) {
                                it.player.sendTitle("&b&l${winner.player.name}".colored(), "&f&lIs the Winner".colored(), 0, 60, 20)
                            } else {
                                it.player.sendTitle("&c&l${countdown}".colored(), "&f&lGame has ended".colored(), 0, 60, 20)
                            }
                        }
                    }
                }
                bar.setTitle("&f&lTeleport to lobby in in &c&l$countdown &f&lseconds".colored())
                bar.progress = (countdown / countdown).toDouble()
                countdown--
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)

        object : BukkitRunnable() {
            override fun run() {
                when (status) {
                    GameStatus.ENDED -> {
                        cancel()
                        return
                    }
                    else -> {
                        instantFirework(FireworkEffect.builder().withColor(Color.fromRGB(
                            Random.nextInt(0, 255),
                            Random.nextInt(0, 255),
                            Random.nextInt(0, 255)
                        )).with(FireworkEffect.Type.BALL_LARGE).build(), getSpawnLocations().random().add(0.0, 5.0, 0.0))
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 5)
    }

    open fun onGameStart() {
        status = GameStatus.RUNNING
        bar.isVisible = false
        players.forEach {
            onPlayerSpawn(it)
            it.player.playSound(it.player.location, Sound.BLOCK_ANVIL_USE, 10.toFloat(), 1.toFloat())
            it.player.sendTitle("", "&e&lThe Game has Begun".colored(), 0, 60, 20)
        }
    }

    open fun onGameEnd(winner: GamePlayer? = null) {
        status = GameStatus.ENDED
        if (winner != null) {
            sendMessage("&fPlayer ${winner.player.name} is the winner!")
        }

        var playersCount = players.size
        while (playersCount > 0) {
            playersCount--
            onPlayerLeft(players[playersCount])
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
        return getSpawnLocations().random().clone().add(0.0, 0.01, 0.0)
    }

    open fun getLobbyLocation(gamePlayer: GamePlayer): Location {
        return lobbyLocation.clone().add(0.0, 0.01, 0.0)
    }

    open fun getMinimumRequiredPlayers(): Int {
        return 1
    }

    open fun getRespawnCooldown(): Int {
        return 3
    }

    open fun getStartCountdown(): Int {
        return 25
    }

    open fun getEndCountdown(): Int {
        return 15
    }
}
