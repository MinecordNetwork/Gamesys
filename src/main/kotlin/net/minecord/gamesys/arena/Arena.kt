package net.minecord.gamesys.arena

import org.bukkit.util.Vector
import java.io.File

open class Arena(val name: String, val file: File, val locations: HashMap<String, ArrayList<Vector>>) {
    fun getLocations(name: String): List<Vector> {
        return locations[name]!!
    }
}
