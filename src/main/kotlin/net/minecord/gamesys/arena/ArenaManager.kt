package net.minecord.gamesys.arena

import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.block.BlockType
import com.sk89q.worldedit.world.block.BlockTypes
import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.utils.runTaskAsynchronously
import org.bukkit.util.Vector
import java.io.File

class ArenaManager(private val plugin: Gamesys) {
    val arenas = arrayListOf<Arena>()
    var validSchematicCount = 0

    fun enable() {
        loadArenas()
    }

    private fun loadArenas() {
        if (arenas.isNotEmpty())
            arenas.clear()

        val schematicsDirectory = File("./plugins/${plugin.name}", "arenas")
        if (!schematicsDirectory.exists() && !schematicsDirectory.mkdirs()) {
            return
        }

        val schematics: Array<File> = schematicsDirectory.listFiles() ?: return
        val mapping = getBlockMapping()
        val validSchematics = schematics.filter { it.name.endsWith(".schem") && it.isFile }
        validSchematicCount = validSchematics.size

        for (schematic in validSchematics) {
            analyzeArena(schematic, mapping)
        }
    }

    private fun analyzeArena(file: File, mapping: HashMap<String, BlockType>) {
        plugin.runTaskAsynchronously {
            plugin.logger.logInfo("Analyzing schematic ${file.name}")

            val now = System.currentTimeMillis()
            val clipboard = ClipboardFormats.findByFile(file)?.getReader(file.inputStream())?.read() ?: return@runTaskAsynchronously
            val locations = hashMapOf<String, ArrayList<Vector>>()
            for (string in mapping.keys) {
                locations[string] = arrayListOf()
            }

            for (y in clipboard.minimumPoint.blockY..clipboard.maximumPoint.blockY) {
                for (x in clipboard.minimumPoint.blockX..clipboard.maximumPoint.blockX) {
                    for (z in clipboard.minimumPoint.blockZ..clipboard.maximumPoint.blockZ) {
                        for ((string, block) in mapping) {
                            val blockState = clipboard.getBlock(BlockVector3.at(x, y, z))
                            if (blockState.blockType == block) {
                                locations[string]?.add(Vector(x, y, z).subtract(Vector(clipboard.origin.x, clipboard.origin.y, clipboard.origin.z)))
                            }
                        }
                    }
                }
            }

            val size = maxOf(clipboard.region.length, clipboard.region.width)
            if (size > plugin.worldManager.biggestArenaSize) {
                plugin.worldManager.biggestArenaSize = size
            }

            arenas.add(plugin.system.createArena(file.name.replace(".schem", ""), file, locations))

            plugin.logger.logInfo("Schematic ${file.name} analyzed (${(System.currentTimeMillis() - now)}ms with ${locations["spawns"]?.size} spawns and ${locations["chests"]?.size} chests).")

            if (arenas.size == validSchematicCount) {
                plugin.gameManager.enable()
            }
        }
    }

    private fun getBlockMapping(): HashMap<String, BlockType> {
        val map = hashMapOf<String, BlockType>()

        for ((string, material) in plugin.system.getArenaBlockMapping()) {
            plugin.logger.logDebug("Getting material of $material")
            map[string] = BlockTypes.get(material.toString().toLowerCase())!!
        }

        return map
    }
}