package net.minecord.gamesys.game.player

import net.minecord.gamesys.Gamesys
import net.minecord.gamesys.game.Game
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.collections.HashMap
import kotlin.collections.hashMapOf


open class GamePlayer(val plugin: Gamesys, val player: Player) {
    private var storedItems = arrayOf<ItemStack>()
    private var storedArmorContents = arrayOf<ItemStack>()
    private var storedExtraContents = arrayOf<ItemStack>()
    var status = GamePlayerStatus.NONE
    var game: Game? = null
    var lastAttacker: GamePlayer? = null
    var lastDamageCause: EntityDamageEvent.DamageCause? = null
    var kills = 0
    var deaths = 0

    open fun onAttack(lastAttacker: GamePlayer, damageCause: EntityDamageEvent.DamageCause) {
        this.lastAttacker = lastAttacker
        this.lastDamageCause = damageCause
        object : BukkitRunnable() {
            override fun run() {
                if (this@GamePlayer.lastAttacker != null && this@GamePlayer.lastAttacker == lastAttacker) this@GamePlayer.lastAttacker = null
                if (this@GamePlayer.lastDamageCause != null && this@GamePlayer.lastDamageCause == damageCause) this@GamePlayer.lastDamageCause = null
            }
        }.runTaskLaterAsynchronously(plugin, 100)
    }

    fun storeAndClearInventory() {
        storedItems = player.inventory.contents.clone()
        storedArmorContents = player.inventory.armorContents.clone()
        storedExtraContents = player.inventory.extraContents.clone()
        player.inventory.clear()
        player.inventory.setArmorContents(null)
        player.inventory.setExtraContents(null)
    }

    open fun restoreInventory() {
        player.inventory.contents = storedItems
        player.inventory.setArmorContents(storedArmorContents)
        player.inventory.setExtraContents(storedExtraContents)
    }

    fun teleport(location: Location) {
        object : BukkitRunnable() {
            override fun run() {
                player.teleport(location)
            }
        }.runTask(plugin)
    }

    fun setGameMode(gameMode: GameMode) {
        object : BukkitRunnable() {
            override fun run() {
                player.gameMode = gameMode
            }
        }.runTask(plugin)
    }

    open fun getGameItems(): HashMap<Int, ItemStack> {
        return hashMapOf()
    }

    open fun getLobbyItems(): HashMap<Int, ItemStack> {
        return hashMapOf()
    }

    open fun getDefaultGameMode(): GameMode {
        return GameMode.SURVIVAL
    }

    open fun getLobbyGameMode(): GameMode {
        return GameMode.ADVENTURE
    }
}
