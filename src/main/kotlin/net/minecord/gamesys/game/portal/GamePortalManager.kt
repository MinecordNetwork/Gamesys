package net.minecord.gamesys.game.portal

import net.minecord.gamesys.Gamesys

class GamePortalManager(val plugin: Gamesys) {
    lateinit var portal: GamePortal

    fun enable() {
        plugin.configReader.getLocation("portal")?.let {
            portal = plugin.system.createGamePortal(plugin, it)
        }
    }

    fun disable() {
        portal.removeHologram()
    }

    fun update() {
        portal.updateHologram()
    }
}
