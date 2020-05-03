package net.minecord.gamesys.game

import com.sk89q.worldedit.EditSession
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.arena.Arena
import net.minecord.gamesys.game.player.GamePlayer
import net.minecord.gamesys.game.player.GamePlayerStatus
import net.minecord.gamesys.game.player.event.DeathMessageSentEvent
import net.minecord.gamesys.utils.colored
import net.minecord.gamesys.utils.instantFirework
import net.minecord.gamesys.utils.runTask
import net.minecord.gamesys.utils.runTaskLaterAsynchronously
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.craftbukkit.v1_15_R1.boss.CraftBossBar
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random
import kotlin.math.atan2 as atan21

open class Game(open val plugin: Gamesys, open val arena: Arena) {
    val players = mutableListOf<GamePlayer>()
    var status: GameStatus = GameStatus.PREPARING
    var startCountdownCounter = 0
    var invinciblePlayers = true
    protected val locations = hashMapOf<String, MutableList<Location>>()
    protected val bar: CraftBossBar = CraftBossBar("&f&lWaiting for more players".colored(), BarColor.WHITE, BarStyle.SEGMENTED_12)
    protected val sidebar = plugin.system.createGameSidebar(plugin, this)
    lateinit var lobbyLocation: Location

    init {
        plugin.worldManager.loadGame(this)
    }

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
        onStartWaiting()
    }

    open fun onPlayerJoined(player: GamePlayer) {
        players.add(player)
        bar.addPlayer(player.player)
        sidebar.addPlayer(player)
        player.apply {
            game = this@Game
            status = GamePlayerStatus.PLAYING
            storeAndClearInventory()
            this.player.allowFlight = false
            this.player.gameMode = player.getLobbyGameMode()
            this.player.teleport(getLobbyLocation(player))
            this.player.location.world?.playSound(this.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
        }
        if (status == GameStatus.WAITING && (players.size >= getMinimumPlayersToStartCountdown() || (players.size == Bukkit.getOnlinePlayers().size && players.size >= getMinimumPlayers()))) {
            onStartCountdownStart()
        }
        plugin.worldManager.fixRespawnScreen()
        plugin.gamePortalManager.update()
        sendMessage("${plugin.system.getChatPrefix()} &7Player &e${player.player.name} &7has &ajoined &7the game &f(${players.size}/${getMaximumPlayers()})")
        if (status == GameStatus.WAITING) {
            sendMessage("${plugin.system.getChatPrefix()} &7The game needs &f${getMinimumPlayers() - players.size} &7more players to start")
        }
    }

    open fun onPlayerLeft(player: GamePlayer) {
        players.remove(player)
        bar.removePlayer(player.player)
        sidebar.removePlayer(player)
        player.apply {
            kills = 0
            deaths = 0
            game = null
            status = GamePlayerStatus.NONE
            this.player.health = player.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
            this.player.gameMode = player.getLobbyGameMode()
            this.player.teleport(plugin.system.getSpawnLocation())
            this.restoreInventory()
        }
        plugin.gamePortalManager.update()
        if (status != GameStatus.ENDING && status != GameStatus.ENDED) {
            sendMessage("${plugin.system.getChatPrefix()} &7Player &e${player.player.name} &7has &cleft &7the game &f(${players.size}/${getMaximumPlayers()})")
        }
    }

    open fun onPlayerDeath(player: GamePlayer, cause: EntityDamageEvent.DamageCause? = null, killer: GamePlayer? = null) {
        if (player.status == GamePlayerStatus.SPECTATING || player.status == GamePlayerStatus.RESPAWNING) {
            return
        }

        player.deaths++

        if (killer != null) {
            killer.kills++
        }

        onDeathMessageSent(player, cause, killer)
        onPlayerStartsToRespawn(player, cause, killer)
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

    open fun onPlayerStartsToRespawn(player: GamePlayer, cause: EntityDamageEvent.DamageCause? = null, killer: GamePlayer? = null) {
        player.player.setItemOnCursor(null)
        player.player.inventory.clear()
        player.player.gameMode = GameMode.SPECTATOR
        player.status = GamePlayerStatus.RESPAWNING

        object : BukkitRunnable() {
            var counter = getRespawnCooldown()
            override fun run() {
                if (counter <= 0) {
                    plugin.runTask {
                        onPlayerSpawn(player)
                        Bukkit.getPluginManager().callEvent(PlayerRespawnEvent(player.player, player.player.location, false))
                    }
                    cancel()
                    return
                }
                counter--
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)
    }

    open fun onPlayerSpawn(player: GamePlayer) {
        player.status = GamePlayerStatus.PLAYING
        player.player.health = player.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value!!
        player.player.foodLevel = 20
        player.player.gameMode = player.getDefaultGameMode()
        player.player.teleport(getRespawnLocation(player))
        player.player.location.world?.playSound(player.player.location, Sound.ITEM_ARMOR_EQUIP_TURTLE, 2f, 1.25f)
        player.player.inventory.clear()
        for ((slot, item) in player.getGameItems()) {
            player.player.inventory.setItem(slot, item)
        }
    }

    open fun onStartWaiting() {
        status = GameStatus.WAITING
        bar.setTitle("&f&lWaiting for more players".colored())
        plugin.gamePortalManager.update()
    }

    open fun onStartCountdownStart() {
        status = GameStatus.STARTING
        startCountdownCounter = getStartCountdown()
        object : BukkitRunnable() {
            override fun run() {
                if (players.size < getMinimumPlayers()) {
                    onStartWaiting()
                    cancel()
                    return
                }
                when {
                    startCountdownCounter <= 0 -> {
                        plugin.runTask { onGameStart() }
                        cancel()
                        return
                    }
                    startCountdownCounter <= 10 || startCountdownCounter % 10 == 0 -> {
                        players.forEach {
                            it.player.playSound(it.player.location, Sound.UI_BUTTON_CLICK, 10f, 1f)
                            it.player.sendTitle("&e&l$startCountdownCounter".colored(), "&f&lThe Game is Starting".colored(), 0, 60, 20)
                        }
                    }
                }
                bar.setTitle("&f&lGame starts in &e&l$startCountdownCounter &f&lseconds".colored())
                bar.progress = startCountdownCounter.toDouble() / getStartCountdown().toDouble()
                plugin.gamePortalManager.update()

                val cutCountdownFrom = 30
                if (startCountdownCounter > cutCountdownFrom && players.size == Bukkit.getOnlinePlayers().size) {
                    startCountdownCounter = cutCountdownFrom
                    sendMessage("${plugin.system.getChatPrefix()} &7All players joined, countdown was set to &e$cutCountdownFrom")
                } else {
                    startCountdownCounter--
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)
    }

    open fun onEndCountdownStart(winner: GamePlayer? = null) {
        status = GameStatus.ENDING
        var countdown = getEndCountdown()

        if (winner != null) {
            Bukkit.broadcastMessage("${plugin.system.getChatPrefix()} &7Player &e${winner.player.name} &7won the game in arena &b${arena.name}".colored())
        }

        object : BukkitRunnable() {
            override fun run() {
                when {
                    countdown <= 0 -> {
                        plugin.runTask { onGameEnd(winner) }
                        cancel()
                        return
                    }
                    else -> {
                        players.forEach {
                            it.player.playSound(it.player.location, Sound.UI_BUTTON_CLICK, 3f, 1f)
                        }
                    }
                }
                bar.isVisible = true
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
            it.player.playSound(it.player.location, Sound.BLOCK_ANVIL_USE, 10f, 1f)
        }

        plugin.runTaskLaterAsynchronously({
            invinciblePlayers = false
        }, 40)

        plugin.gamePortalManager.update()
    }

    open fun onGameEnd(winner: GamePlayer? = null) {
        status = GameStatus.ENDED

        var playersCount = players.size
        while (playersCount > 0) {
            playersCount--
            onPlayerLeft(players[playersCount])
        }

        plugin.gameManager.removeGame(this)
    }

    open fun sendMessage(message: String) {
        players.forEach { it.player.sendMessage(message.colored()) }
    }

    open fun isFull(): Boolean {
        return players.size >= getMaximumPlayers()
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
        return lobbyLocation
    }

    open fun getMinimumPlayersToStartCountdown(): Int {
        return 4
    }

    open fun getMinimumPlayers(): Int {
        return 2
    }

    open fun getMaximumPlayers(): Int {
        return 20
    }

    open fun getRespawnCooldown(): Int {
        return 3
    }

    open fun getStartCountdown(): Int {
        return 60
    }

    open fun getEndCountdown(): Int {
        return 15
    }
}
