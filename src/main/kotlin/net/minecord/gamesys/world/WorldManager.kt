package net.minecord.gamesys.world

import com.boydti.fawe.FaweAPI
import com.boydti.fawe.util.EditSessionBuilder
import com.boydti.fawe.util.TaskManager
import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MultiverseWorld
import com.onarandombox.MultiverseCore.utils.FileUtils
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockTypes
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.Game
import org.bukkit.*
import org.bukkit.scheduler.BukkitRunnable
import java.io.File

class WorldManager(private val plugin: Gamesys) {
    private val worldName = "world_arenas"
    private lateinit var bukkitWorld: World
    private lateinit var worldEditWorld: com.sk89q.worldedit.world.World
    private val pasteHeight = 93
    private val arenaBorder = 400
    private var latestPasteX = 0
    var biggestArenaSize = 0

    fun insertArena(game: Game) {
        val pasteTo = BlockVector3.at(latestPasteX + (biggestArenaSize/2) + arenaBorder, pasteHeight, 0)
        latestPasteX = pasteTo.blockX

        TaskManager.IMP.async {
            try {
                plugin.logger.logInfo("Pasting arena ${game.arena.name}")

                val now = System.currentTimeMillis()
                val clipboard = ClipboardFormats.findByFile(game.arena.file)?.getReader(game.arena.file.inputStream())?.read()
                val clipboardHolder = ClipboardHolder(clipboard)
                val editSession = EditSessionBuilder(BukkitWorld(bukkitWorld)).fastmode(true).build()
                val operation: Operation = clipboardHolder
                    .createPaste(editSession)
                    .to(pasteTo)
                    .ignoreAirBlocks(true)
                    .build()

                Operations.complete(operation)

                game.onArenaLoaded(editSession, Location(bukkitWorld, pasteTo.x.toDouble(), pasteTo.y.toDouble(), pasteTo.z.toDouble()))

                game.getSpawnLocations().forEach {
                    editSession.setBlock(BlockVector3.at(it.blockX, it.blockY, it.blockZ), BlockTypes.AIR?.defaultState)
                }

                editSession.close()

                plugin.logger.logInfo("Arena ${game.arena.name} pasted at ${pasteTo.blockX} ${pasteTo.blockY} ${pasteTo.blockZ} (${(System.currentTimeMillis() - now)}ms)")

            } catch (e: Exception) {
                Bukkit.broadcastMessage("Unable to paste arena.")
            }
        }
    }

    fun loadWorld(): World {
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
            val ret: Boolean = mV.mvWorldManager.addWorld(worldName, World.Environment.NORMAL, null, WorldType.NORMAL, false, "VoidGenerator", false)

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
                worldCreator.generator("VoidGenerator")
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
            }
        }

        object : BukkitRunnable() {
            override fun run() {
                if (!backupRegion.exists()) {
                    region.copyRecursively(backupRegion)
                }
            }
        }.runTaskLaterAsynchronously(plugin, 60)

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
