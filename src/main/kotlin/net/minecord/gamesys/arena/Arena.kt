package net.minecord.gamesys.arena

import org.bukkit.util.Vector
import java.io.File

open class Arena(val name: String, val file: File, val locations: HashMap<String, ArrayList<Vector>>, val minPlayers: Int? = null, val maxPlayers: Int? = null) {
    fun getLocations(name: String): List<Vector> {
        return locations[name]!!
    }
}
