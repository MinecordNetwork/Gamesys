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
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.Game
import org.bukkit.*
import java.io.File


class WorldManager(private val api: Gamesys) {
    private val worldName = "world_arenas"
    private lateinit var bukkitWorld: World
    private lateinit var worldEditWorld: com.sk89q.worldedit.world.World
    private val pasteHeight = 93
    private val arenaBorder = 400
    private val latestPasteX = 0
    var biggestArenaSize = 0

    fun insertArena(game: Game) {
        TaskManager.IMP.async {
            try {
                val pasteTo = BlockVector3.at(latestPasteX + (biggestArenaSize/2) + arenaBorder, pasteHeight, 0)

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
                editSession.close()
                Bukkit.broadcastMessage("Arena ${game.arena.name} pasted (" + (System.currentTimeMillis() - now) + "ms).")

                game.onArenaLoaded(Location(bukkitWorld, pasteTo.x + 0.5, pasteTo.y.toDouble(), pasteTo.z + 0.5))

            } catch (e: Exception) {
                Bukkit.broadcastMessage("Unable to paste arena.")
            }
        }
    }

    fun loadWorld(): World {
        deleteWorld()

        val mV: MultiverseCore = api.server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore
        if (mV.mvWorldManager.loadWorld(worldName)) {
            return mV.mvWorldManager.getMVWorld(worldName).cbWorld
        }

        val ret: Boolean = mV.mvWorldManager.addWorld(worldName, World.Environment.NORMAL, null, WorldType.NORMAL, false, "VoidGenerator", false)

        var world: World? = null

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
            worldCreator.generator("SkyClean")
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

        worldEditWorld = FaweAPI.getWorld(worldName)
        bukkitWorld = world!!

        return bukkitWorld
    }

    private fun deleteWorld() {
        val world: World? = api.server.getWorld(worldName)

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
                result = api.server.unloadWorld(world, true)
                if (result) api.logger.logInfo("World $worldName was unloaded from memory") else api.logger.logWarning("World $worldName could not be unloaded")
            }

            result = FileUtils.deleteFolder(File(worldName))

            if (result) api.logger.logInfo("World $worldName was deleted") else api.logger.logInfo("World $worldName was not deleted")
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
