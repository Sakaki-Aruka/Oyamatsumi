package online.aruka.oyamatsumi.impl.pattern

import online.aruka.oyamatsumi.interfaces.MiningPattern
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class NormalVeinPattern(
    private val maxBlockChain: Int = 200
): MiningPattern {
    override fun onMining(
        block: Block,
        face: BlockFace,
        tool: ItemStack,
        miner: Player,
        maxMiningBlock: Int,
        targets: Set<Material>
    ): Set<Location> {
        val result: MutableSet<Location> = mutableSetOf()
        val queue: ArrayDeque<Location> = ArrayDeque(listOf(block.location))
        while (queue.isNotEmpty()) {
            if (result.size >= maxBlockChain) break
            val b: Block = queue.removeFirst().block
            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        val new: Block = b.getRelative(x, y, z)
                        if (new.location in result || new.type !in targets) continue
                        result.add(new.location)
                        queue.add(new.location)
                    }
                }
            }
        }
        return result
    }

}