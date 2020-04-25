package net.minecord.gamesys.config

import net.minecord.gamesys.Gamesys
import org.bukkit.Bukkit
import org.bukkit.Location

class ConfigReader(val plugin: Gamesys) {
    private fun getString(path: String): String? {
        return this.plugin.config.getString(path)
    }

    fun getLocation(path: String): Location? {
        val worldName: String? = getString("$path.world")
        val locationString: String? = getString("$path.location")
        val parseLocations = locationString?.split(" ")?.toTypedArray() ?: return null

        return if (parseLocations.size >= 3) {
            val x = parseLocations[0].toFloat()
            val y = parseLocations[1].toFloat()
            val z = parseLocations[2].toFloat()
            var yaw = 0.0f
            var pitch = 0.0f
            if (parseLocations.size == 5) {
                yaw = parseLocations[3].toFloat()
                pitch = parseLocations[4].toFloat()
            }
            Location(
                Bukkit.getWorld(worldName ?: "world"),
                x.toDouble(),
                y.toDouble(),
                z.toDouble(),
                yaw,
                pitch
            )
        } else {
            Location(Bukkit.getWorld("world"), 0.0, 75.0, 0.0)
        }
    }
}