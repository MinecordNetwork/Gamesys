package net.minecord.gamesys.utils

import net.minecord.gamesys.Gamesys

fun Gamesys.getMsgString(message: String): String {
    return this.configManager.messages.getString(message)!!.colored()
}

fun Gamesys.getMsgInt(message: String): Int {
    return this.configManager.messages.getInt(message)
}

fun Gamesys.getMsgBoolean(message: String): Boolean {
    return this.configManager.messages.getBoolean(message)
}

fun Gamesys.getMsgDouble(message: String): Double {
    return this.configManager.messages.getDouble(message)
}

fun Gamesys.getCfgString(message: String): String {
    return this.configManager.config.getString(message)!!.colored()
}

fun Gamesys.getCfgStringList(message: String): MutableList<String> {
    val list = mutableListOf<String>()

    this.configManager.config.getStringList(message).forEach {
        list.add(it.colored())
    }

    return list
}

fun Gamesys.getCfgInt(message: String): Int {
    return this.configManager.config.getInt(message)
}

fun Gamesys.getCfgBoolean(message: String): Boolean {
    return this.configManager.config.getBoolean(message)
}

fun Gamesys.getCfgDouble(message: String): Double {
    return this.configManager.config.getDouble(message)
}

fun Gamesys.getMsgStringList(message: String): MutableList<String> {
    val list = mutableListOf<String>()

    this.configManager.messages.getStringList(message).forEach {
        list.add(it.colored())
    }

    return list
}
