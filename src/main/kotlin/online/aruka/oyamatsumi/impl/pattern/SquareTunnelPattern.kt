package online.aruka.oyamatsumi.impl.pattern

import online.aruka.oyamatsumi.impl.listener.MineListener
import online.aruka.oyamatsumi.interfaces.MiningPattern
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow

class SquareTunnelPattern(
    private val radius: Int = 1,
    private val moveCenterY: Int = radius - 1
): MiningPattern {

    private fun of(world: World, x: Int, y: Int, z: Int): Location {
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }

    private fun initDiff(
        base: Location,
        face: BlockFace,
        d: Int
    ): Location {
        val world: World = base.world
        val x: Int = base.blockX
        val y: Int = base.blockY
        val z: Int = base.blockZ
        return when(face) {
            BlockFace.EAST -> { of(world, x + d, y, z) }
            BlockFace.WEST -> { of(world, x - d, y, z) }
            BlockFace.SOUTH -> { of(world, x, y, z + d) }
            BlockFace.NORTH -> { of(world, x, y, z - d) }
            BlockFace.UP -> { of(world, x, y + d, z) }
            BlockFace.DOWN -> { of(world, x, y - d, z) }
            else -> base
        }
    }

    private fun diff(
        base: Location,
        face: BlockFace,
        di: Int,
        dj: Int
    ): Location {
        val world: World = base.world
        val x: Int = base.blockX
        val y: Int = base.blockY
        val z: Int = base.blockZ
        return when(face) {
            BlockFace.EAST, BlockFace.WEST -> { of(world, x, y + di + moveCenterY, z + dj) }
            BlockFace.SOUTH, BlockFace.NORTH -> { of(world, x + di, y + dj + moveCenterY, z) }
            BlockFace.UP, BlockFace.DOWN -> { of(world, x + di, y, z + dj) }
            else -> base
        }
    }

    private fun square(center: Location, face: BlockFace): List<Location> {
        val result: MutableList<Location> = mutableListOf()
        for (i in (-radius..radius)) {
            for (j in (-radius..radius)) {
                result.add(diff(center, face, i, j))
            }
        }
        return result
    }

    override fun onMining(
        block: Block,
        face: BlockFace,
        tool: ItemStack,
        miner: Player,
        maxMiningBlock: Int,
        targets: Set<Material>
    ): Set<Location> {
        val blockPerTunnelPanel: Double = ceil(((radius * 2) + 1).toDouble().pow(2))
        val maxTunnelLength: Int = min((maxMiningBlock / blockPerTunnelPanel).toInt(), 5)
        val result: MutableSet<Location> = mutableSetOf()
        out@ for (i in 0..maxTunnelLength) {
            if (result.size >= maxMiningBlock) break
            val center: Location = initDiff(block.location, face.oppositeFace, i)
            var changed = 0
            for (loc in square(center, face)) {
                val b: Block = loc.block
                if (b.type !in targets) continue
                if (result.size == maxMiningBlock) break@out
                changed++
                result.add(b.location)
            }
            if (changed == 0) break
        }
        return result
    }
}
