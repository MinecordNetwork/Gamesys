package net.minecord.gamesys.utils

import org.bukkit.entity.Player
import java.lang.Exception
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class ProtocolSupport {
    companion object {
        private var protocolVersionMethod: Method? = null
        private var getIdMethod: Method? = null

        init {
            try {
                protocolVersionMethod = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player::class.java)
                getIdMethod = Class.forName("protocolsupport.api.ProtocolVersion").getMethod("getId")
            } catch (e: Exception) {
                // ProtocolSupport not installed.
            }
        }

        fun getProtocolVersion(player: Player?): Int {
            if (protocolVersionMethod == null) return -1

            try {
                val version = protocolVersionMethod!!.invoke(null, player)
                return getIdMethod!!.invoke(version) as Int
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }

            return -1
        }
    }
}
