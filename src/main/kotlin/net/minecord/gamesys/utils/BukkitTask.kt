package net.minecord.gamesys.utils

import org.bukkit.Bukkit.getScheduler
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

inline fun <reified T: JavaPlugin> T.runTask(crossinline body: (T) -> Unit): BukkitTask {
    val func = { body(this) }
    return getScheduler().runTask(this, func)
}

inline fun <reified T: JavaPlugin> T.runTaskTimer(crossinline body: (T) -> Unit, delay: Long, period: Long): BukkitTask {
    val func = { body(this) }
    return getScheduler().runTaskTimer(this, func, delay, period)
}

inline fun <reified T: JavaPlugin> T.runTaskLater(crossinline body: (T) -> Unit, delay: Long): BukkitTask {
    val func = { body(this) }
    return getScheduler().runTaskLater(this, func, delay)
}

inline fun <reified T: JavaPlugin> T.runTaskAsynchronously(crossinline body: (T) -> Unit): BukkitTask {
    val func = { body(this) }
    return getScheduler().runTaskAsynchronously(this, func)
}

inline fun <reified T: JavaPlugin> T.runTaskLaterAsynchronously(crossinline body: (T) -> Unit, delay: Long): BukkitTask {
    val func = { body(this) }
    return getScheduler().runTaskLaterAsynchronously(this, func, delay)
}

inline fun <reified T: JavaPlugin> T.runTaskTimerAsynchronously(crossinline body: (T) -> Unit, delay: Long, period: Long): BukkitTask {
    val func = { body(this) }
    return getScheduler().runTaskTimerAsynchronously(this, func, delay, period)
}
