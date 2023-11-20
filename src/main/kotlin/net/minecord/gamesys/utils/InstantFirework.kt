package net.minecord.gamesys.utils

import org.bukkit.Bukkit
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.Firework

class InstantFirework {
    fun explode(fe: FireworkEffect?, loc: Location) {
        val f = loc.world!!.spawn(loc, Firework::class.java)
        val fm = f.fireworkMeta
        fm.addEffect(fe!!)
        f.fireworkMeta = fm
        try {
            val entityFireworkClass = Class.forName("net.minecraft.world.entity.projectile.EntityFireworks")
            val craftFireworkClass = getClass("org.bukkit.craftbukkit.", "entity.CraftFirework")
            val firework = craftFireworkClass.cast(f)
            val handle = firework.javaClass.getMethod("getHandle")
            val entityFirework = handle.invoke(firework)
            val expectedLifespan = entityFireworkClass.getDeclaredField("f")
            val ticksFlown = entityFireworkClass.getDeclaredField("e")
            ticksFlown.isAccessible = true
            ticksFlown.setInt(entityFirework, expectedLifespan.getInt(entityFirework) - 1)
            ticksFlown.isAccessible = false
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @Throws(ClassNotFoundException::class)
    private fun getClass(prefix: String, nmsClassString: String): Class<*> {
        val version =
                Bukkit.getServer().javaClass.getPackage().name.replace(".", ",").split(",").toTypedArray()[3] + "."
        val name = prefix + version + nmsClassString
        return Class.forName(name)
    }
}