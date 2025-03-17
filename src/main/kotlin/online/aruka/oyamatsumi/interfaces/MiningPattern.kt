package online.aruka.oyamatsumi.interfaces

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface MiningPattern {
    fun onMining(
        block: Block,
        face: BlockFace,
        tool: ItemStack,
        miner: Player,
        maxMiningBlock: Int,
        targets: Set<Material>
    ): Set<Location>
}