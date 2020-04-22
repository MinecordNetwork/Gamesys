package net.minecord.gamesys.arena

import org.bukkit.util.Vector
import java.io.File

open class Arena(val name: String, val file: File, val locations: HashMap<String, MutableList<Vector>>, val minPlayers: Int? = null, val maxPlayers: Int? = null) {
    fun getLocations(name: String): MutableList<Vector> {
        return locations[name]!!
    }
}
