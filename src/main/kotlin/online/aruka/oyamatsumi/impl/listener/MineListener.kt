package online.aruka.oyamatsumi.impl.listener

import online.aruka.oyamatsumi.impl.MiningManager
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

object MineListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakEvent.onBreak() {
        val uid: UUID = this.player.uniqueId
        if (this.isCancelled || uid !in MiningManager.ENABLED_PLAYERS || uid in MiningManager.FORCE_OFF_PLAYERS) return
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