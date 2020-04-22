package net.minecord.gamesys.arena

import com.boydti.fawe.util.TaskManager
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.block.BlockType
import com.sk89q.worldedit.world.block.BlockTypes
import net.minecord.gamesys.Gamesys
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.io.File
import java.io.IOException

class ArenaManager(private val plugin: Gamesys) {
    val arenas = arrayListOf<Arena>()

    fun loadArenas() {
        if (arenas.isNotEmpty())
            arenas.clear()

        val schematicsDirectory = File("./plugins/${plugin.name}", "arenas")
        if (!schematicsDirectory.exists() && !schematicsDirectory.mkdirs()) {
            return
        }

        val schematics: Array<File> = schematicsDirectory.listFiles() ?: return
        val mapping = getBlockMapping()

        for (schematic in schematics) {
            if (!schematic.name.endsWith(".schem")) {
                continue
            }

            if (!schematic.isFile) {
                plugin.logger.logWarning("Could not load schematic " + schematic.name + ": Not a file")
                continue
            }

            try {
                registerArena(schematic, mapping)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun registerArena(file: File, mapping: HashMap<String, BlockType>) {
        object : BukkitRunnable() {
            override fun run() {
                plugin.logger.logInfo("Analyzing schematic ${file.name}")

                val now = System.currentTimeMillis()
                val clipboard = ClipboardFormats.findByFile(file)?.getReader(file.inputStream())?.read() ?: return
                val locations = hashMapOf<String, MutableList<Vector>>()
                for (string in mapping.keys) {
                    locations[string] = mutableListOf()
                }

                for (y in clipboard.minimumPoint.blockY..clipboard.maximumPoint.blockY) {
                    for (x in clipboard.minimumPoint.blockX..clipboard.maximumPoint.blockX) {
                        for (z in clipboard.minimumPoint.blockZ..clipboard.maximumPoint.blockZ) {
                            for ((string, block) in mapping) {
                                if (block == clipboard.getBlock(BlockVector3.at(x, y, z)).blockType) {
                                    locations[string]?.add(Vector(x, y, z))
                                }
                            }
                        }
                    }
                }

                val size = maxOf(clipboard.region.length, clipboard.region.width)
                if (size > plugin.worldManager.biggestArenaSize) {
                    plugin.worldManager.biggestArenaSize = size
                }

                arenas.add(plugin.system.createArena(file.name.replace(".schematic", ""), file, locations))


                plugin.logger.logInfo("Schematic ${file.name} analyzed (${(System.currentTimeMillis() - now)}ms with ${locations["spawns"]?.size} spawns and ${locations["chests"]?.size} chests).")
            }
        }.runTaskAsynchronously(plugin)
    }

    private fun getBlockMapping(): HashMap<String, BlockType> {
        val map = hashMapOf<String, BlockType>()

        for ((string, material) in plugin.system.getArenaBlockMapping()) {
            map[string] = BlockTypes.get(material.toString().toLowerCase())!!
        }

        return map
    }
}