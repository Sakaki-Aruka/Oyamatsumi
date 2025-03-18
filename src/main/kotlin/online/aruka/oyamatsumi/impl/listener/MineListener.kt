package online.aruka.oyamatsumi.impl.listener

import online.aruka.oyamatsumi.impl.MiningManager
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

object MineListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakEvent.onBreak() {
        if (this.isCancelled || this.player.uniqueId !in MiningManager.ENABLED_PLAYERS) return
        val tool: ItemStack = this.player.inventory.itemInMainHand
        val blockType: Material = this.block.type

        MiningManager.MINING_COMPONENTS
            .firstOrNull { c ->
                tool.type in c.tools
                        && blockType in c.targets
                        && player.gameMode in c.enabledGameMode
                        && c.predicate(player)
            }?.onMine(this)
    }
}