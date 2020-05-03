package net.minecord.gamesys.config

import net.minecord.gamesys.Gamesys
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager(val plugin: Gamesys) {
    val messages: YamlConfiguration = YamlConfiguration()
    val config: YamlConfiguration = YamlConfiguration()

    init {
        loadMessageConfig()
    }

    private fun loadConfig() {
        val configFile = File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false)
        }

        config.load(configFile)
        plugin.reloadConfig()
    }

    private fun loadMessageConfig() {
        val messagesFile = File(plugin.dataFolder, "messages.yml")
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false)
        }

        messages.load(messagesFile)
    }
}
