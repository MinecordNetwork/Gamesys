package net.minecord.gamesys.world

import com.fastasyncworldedit.core.FaweAPI
import com.fastasyncworldedit.core.util.TaskManager
import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MultiverseWorld
import com.onarandombox.MultiverseCore.utils.FileUtils
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.EditSessionBuilder
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockTypes
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.Game
import net.minecord.gamesys.game.GameStatus
import net.minecord.gamesys.utils.runTaskAsynchronously
import net.minecord.gamesys.utils.runTaskLaterAsynchronously
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.io.File
import kotlin.math.atan2

class WorldManager(private val plugin: Gamesys) {
    private val worldName = "world_arenas"
    lateinit var bukkitWorld: World
    private lateinit var worldEditWorld: com.sk89q.worldedit.world.World
    private val lobbyFile = File("./plugins/${plugin.name}", "Lobby.schem")
    private lateinit var lobbySpawnLocation: Vector
    private val pasteHeight = 93
    private val border = 400
    private var lastX = 0
    private var lastZ = 0
    var biggestArenaSize = 0

    fun enable() {
        loadWorld()
        analyzeLobby()

        object : BukkitRunnable() {
            override fun run() {
                for (player: Player in Bukkit.getOnlinePlayers()) {
                    if (player.location.blockY <= 0) {
                        object : BukkitRunnable() {
                            override fun run() {
                                val gamePlayer = plugin.gamePlayerManager.get(player)
                                val game = gamePlayer.game
                                if (game != null) {
                                    when (game.status) {
                                        GameStatus.RUNNING -> {
                                            game.onPlayerDeath(gamePlayer, EntityDamageEvent.DamageCause.VOID)
                                        }
                                        GameStatus.WAITING, GameStatus.STARTING -> {
                                            player.teleport(game.getLobbyLocation(gamePlayer))
                                        }
                                        else -> {
                                            player.teleport(game.getRespawnLocation(gamePlayer))
                                        }
                                    }
                                } else {
                                    gamePlayer.player.teleport(plugin.system.getSpawnLocation())
                                }
                            }
                        }.runTask(plugin)
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 10)
    }

    private fun analyzeLobby() {
        plugin.runTaskAsynchronously {
            plugin.logger.logInfo("Analyzing lobby schematic")

            if (!lobbyFile.exists()) {
                plugin.logger.logError("Lobby schematic does not exist")
                return@runTaskAsynchronously
            }

            val now = System.currentTimeMillis()
            val clipboard =
                ClipboardFormats.findByFile(lobbyFile)?.getReader(lobbyFile.inputStream())?.read() ?: return@runTaskAsynchronously

            for (y in clipboard.minimumPoint.blockY..clipboard.maximumPoint.blockY) {
                for (x in clipboard.minimumPoint.blockX..clipboard.maximumPoint.blockX) {
                    for (z in clipboard.minimumPoint.blockZ..clipboard.maximumPoint.blockZ) {
                        val blockState = clipboard.getBlock(BlockVector3.at(x, y, z))
                        if (blockState.blockType == BlockTypes.WHITE_GLAZED_TERRACOTTA) {
                            lobbySpawnLocation = Vector(x, y, z).subtract(
                                Vector(
                                    clipboard.origin.x,
                                    clipboard.origin.y,
                                    clipboard.origin.z
                                )
                            )
                            plugin.logger.logInfo("Lobby schematic analyzed in ${(System.currentTimeMillis() - now)}ms")
                            return@runTaskAsynchronously
                        }
                    }
                }
            }

            plugin.logger.logError("Lobby schematic does not have a spawn! (WHITE_GLAZED_TERRACOTTA)")
        }
    }

    fun loadGame(game: Game) {
        val arenaOrigin = BlockVector3.at(lastX + (biggestArenaSize/2) + border, pasteHeight, 0)
        lastX = arenaOrigin.blockX

        val lobbyOrigin = BlockVector3.at(0, pasteHeight, lastZ + border)
        lastZ = lobbyOrigin.blockZ

        TaskManager.taskManager().async {
            try {
                plugin.logger.logInfo("Pasting lobby for arena ${game.arena.name}")

                var now = System.currentTimeMillis()
                pasteSchematic(lobbyFile, lobbyOrigin)

                val lobbySpawn = lobbyOrigin.add(lobbySpawnLocation.blockX, lobbySpawnLocation.blockY, lobbySpawnLocation.blockZ)
                val lobbyLocation = Location(bukkitWorld, lobbySpawn.x.toDouble(), lobbySpawn.y.toDouble(), lobbySpawn.z.toDouble())
                lobbyLocation.yaw = (atan2(
                    y = -(lobbyOrigin.x - lobbyLocation.x),
                    x = lobbyOrigin.z - lobbyLocation.z
                ) * (180.0 / Math.PI)).toFloat()
                lobbyLocation.pitch = 0f

                worldEditWorld.setBlock(lobbySpawn.blockX, lobbySpawn.blockY, lobbySpawn.blockZ, BlockTypes.AIR?.defaultState)

                plugin.logger.logInfo("Lobby for arena ${game.arena.name} pasted at ${lobbyOrigin.blockX} ${lobbyOrigin.blockY} ${lobbyOrigin.blockZ} (${(System.currentTimeMillis() - now)}ms)")
                plugin.logger.logInfo("Pasting arena ${game.arena.name}")

                now = System.currentTimeMillis()
                pasteSchematic(game.arena.file, arenaOrigin)

                game.onArenaLoaded(
                    worldEditWorld,
                    Location(bukkitWorld, arenaOrigin.x.toDouble(), arenaOrigin.y.toDouble(), arenaOrigin.z.toDouble()),
                    lobbyLocation
                )

                game.getSpawnLocations().forEach {
                    worldEditWorld.setBlock(it.blockX, it.blockY, it.blockZ, BlockTypes.AIR?.defaultState)
                }

                object : BukkitRunnable() {
                    override fun run() {
                        for (chunk in bukkitWorld.loadedChunks) chunk.unload()
                    }
                }.runTask(plugin)

                plugin.logger.logInfo("Arena ${game.arena.name} pasted at ${arenaOrigin.blockX} ${arenaOrigin.blockY} ${arenaOrigin.blockZ} (${(System.currentTimeMillis() - now)}ms)")

            } catch (e: Exception) {
                Bukkit.broadcastMessage("Unable to paste arena.")
            }
        }
    }

    private fun pasteSchematic(file: File, location: BlockVector3) {
        val clipboard = ClipboardFormats.findByFile(file)?.getReader(file.inputStream())?.read()
        val clipboardHolder = ClipboardHolder(clipboard)
        val operation: Operation = clipboardHolder
            .createPaste(worldEditWorld)
            .to(location)
            .ignoreAirBlocks(true)
            .build()

        Operations.complete(operation)
    }

    fun fixRespawnScreen() {
        bukkitWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false)
        bukkitWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
    }

    private fun loadWorld(): World {
        plugin.logger.logInfo("Loading world")

        deleteWorld()

        val now = System.currentTimeMillis()
        var world: World? = plugin.server.getWorld(worldName)
        val mV: MultiverseCore = plugin.server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore

        val region = File("./${worldName}/region")
        val backupRegion = File("./${worldName}/region_backup")

        if (world != null) {
            plugin.server.unloadWorld(world, true)
            mV.mvWorldManager.unloadWorld(worldName)
            region.deleteRecursively()
            if (backupRegion.exists()) {
                backupRegion.copyRecursively(region)
            }
            mV.mvWorldManager.loadWorld(worldName)

        } else {
            val ret: Boolean = mV.mvWorldManager.addWorld(worldName, World.Environment.NORMAL, null, WorldType.NORMAL, false, "VoidGen", false)

            if (ret) {
                val mvWorld: MultiverseWorld = mV.mvWorldManager.getMVWorld(worldName)
                world = mvWorld.cbWorld
                mvWorld.setPVPMode(true)
                mvWorld.setEnableWeather(false)
                mvWorld.setKeepSpawnInMemory(false)
                mvWorld.setAllowAnimalSpawn(false)
                mvWorld.setAllowMonsterSpawn(false)
                mvWorld.gameMode = GameMode.ADVENTURE
            }

            if (world == null) {
                val worldCreator = WorldCreator(worldName)
                worldCreator.environment(World.Environment.NORMAL)
                worldCreator.generateStructures(false)
                worldCreator.generator("VoidGen")
                world = worldCreator.createWorld()
                if (world != null) {
                    world.difficulty = Difficulty.NORMAL
                    world.setSpawnFlags(false, false)
                    world.pvp = true
                    world.setStorm(false)
                    world.isThundering = false
                    world.weatherDuration = Int.MAX_VALUE
                    world.keepSpawnInMemory = false
                    world.setTicksPerAnimalSpawns(0)
                    world.setTicksPerMonsterSpawns(0)
                }
            }

            if (world != null) {
                world.isAutoSave = false
                world.time = 6000
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
                world.setGameRule(GameRule.DO_FIRE_TICK, false)
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
                world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            }
        }

        plugin.runTaskLaterAsynchronously({
            if (!backupRegion.exists()) {
                region.copyRecursively(backupRegion)
            }
        }, 60)

        plugin.logger.logInfo("World $worldName loaded (${(System.currentTimeMillis() - now)}ms)")

        worldEditWorld = FaweAPI.getWorld(worldName)
        bukkitWorld = world!!

        return bukkitWorld
    }

    private fun deleteWorld() {
        val world: World? = plugin.server.getWorld(worldName)

        var result = false
        if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) {
            val multiVerse: MultiverseCore? = Bukkit.getPluginManager().getPlugin("Multiverse-Core") as MultiverseCore?
            if (multiVerse != null) {
                result = if (world != null) {
                    try {
                        multiVerse.mvWorldManager.deleteWorld(world.name)
                    } catch (ignored: IllegalArgumentException) {
                        false
                    }
                } else multiVerse.mvWorldManager.removeWorldFromConfig(worldName)
            }
        }

        if (!result) {
            if (world != null) {
                result = plugin.server.unloadWorld(world, true)
                if (result) plugin.logger.logInfo("World $worldName was unloaded from memory") else plugin.logger.logWarning("World $worldName could not be unloaded")
            }

            result = FileUtils.deleteFolder(File(worldName))

            if (result) plugin.logger.logInfo("World $worldName was deleted") else plugin.logger.logInfo("World $worldName was not deleted")
        }

        val workingDirectory = File("./plugins/WorldGuard/worlds/")
        val contents = workingDirectory.listFiles()
        if (contents != null) {
            for (file in contents) {
                if (!file.isDirectory || file.name != worldName) {
                    continue
                }
                FileUtils.deleteFolder(file)
            }
        }
    }
}
