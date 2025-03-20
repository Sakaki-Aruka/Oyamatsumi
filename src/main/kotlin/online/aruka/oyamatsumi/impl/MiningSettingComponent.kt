package online.aruka.oyamatsumi.impl

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.protection.managers.RegionManager
import me.ryanhamshire.GriefPrevention.ClaimPermission
import online.aruka.oyamatsumi.Oyamatsumi
import online.aruka.oyamatsumi.interfaces.MiningPattern
import org.bukkit.FluidCollisionMode
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.util.RayTraceResult

data class MiningSettingComponent(
    val targets: Set<Material>,
    val tools: Set<Material>,
    val maxMiningBlocks: Int,
    val pattern: MiningPattern,
    val enabledGameMode: Set<GameMode> = setOf(GameMode.SURVIVAL),
    val predicate: (Player) -> Boolean = PLAYER_REQUIRES,
    val durabilityReducer: (ItemStack, Map<Location, Boolean>) -> Map<Location, Boolean> = REDUCE_ONE,
    val regionProtectChecker: (Player, Set<Location>) -> Map<Location, Boolean> = DEFAULT_REGION_CHECKER
) {

    companion object {
        private val PLAYER_REQUIRES: (Player) -> Boolean = { player ->
            player.isSneaking
        }

        private val REDUCE_ONE: (ItemStack, Map<Location, Boolean>) -> Map<Location, Boolean> = { tool, loc ->
            if (!tool.itemMeta.isUnbreakable && tool.itemMeta is Damageable) {
                tool.editMeta { meta ->
                    (meta as Damageable).damage += 1
                }
            }
            loc
        }

        private val DEFAULT_REGION_CHECKER: (Player, Set<Location>) -> Map<Location, Boolean> = { player, locations ->
            val result: MutableMap<Location, Boolean> = mutableMapOf()
            for (loc in locations) {
                val element: MutableSet<Boolean> = mutableSetOf(true)
                if (Oyamatsumi.GRIEF_PREVENTION_ENABLED) {
                    element.add(Oyamatsumi.GRIEF_PREVENTION_DATA!!
                        .claims
                        .filter { c -> c.contains(loc, false, false) }
                        .all { c -> c.hasExplicitPermission(player, ClaimPermission.Build) }
                    )
                }

                if (Oyamatsumi.WORLD_GUARD_ENABLED) {
                    val regionManager: RegionManager = Oyamatsumi.WORLD_GUARD!!.platform
                        .regionContainer
                        .get(BukkitAdapter.adapt(loc.world))
                        ?: continue
                    val x: Int = loc.blockX
                    val y: Int = loc.blockY
                    val z: Int = loc.blockZ

                    element.add(regionManager.regions
                        .filter{ (_, region) -> region.contains(BlockVector3.at(x, y, z))}
                        .all { (_, region) ->
                            region.members.contains(player.uniqueId)
                                    || region.owners.contains(player.uniqueId)
                        })
                }
                result[loc] = element.size == 1 && element.first()
            }

            result
        }
    }

    fun onMine(event: BlockBreakEvent) {
        val hitBlockFace: BlockFace = getHitBlockFace(event.player) ?: return
        val tool: ItemStack = event.player.inventory.itemInMainHand
        val player: Player = event.player

        val blocks: Set<Location> = this.pattern.onMining(
            block = event.block,
            face = hitBlockFace,
            tool = tool,
            miner = player,
            maxMiningBlock = maxMiningBlocks,
            targets = targets
        )

        durabilityReducer(tool, regionProtectChecker(player, blocks))
            .filter { (_, result) -> result }
            .map { (l, _) -> l.block }
            .forEach { block ->
                if (block.isPreferredTool(tool)) {

                    if (Oyamatsumi.CORE_PROTECT_ENABLED) {
                        Oyamatsumi.CORE_PROTECT_API!!.logRemoval(player.name, block.state)
                    }

                    val hasMendingTool: Boolean = player.inventory
                        .let { inv ->
                            tool.enchantments.containsKey(Enchantment.MENDING)
                                .or(inv.itemInOffHand.enchantments.containsKey(Enchantment.MENDING))
                                .or(inv.armorContents.any { i ->
                                    i?.enchantments?.containsKey(Enchantment.MENDING) == true
                                })
                        }
                    player.giveExp(event.expToDrop, hasMendingTool)
                    player.inventory.addItem(*block.getDrops(tool).toTypedArray())
                        .forEach { (_, overflow) -> player.world.dropItem(player.location, overflow) }
                    block.type = Material.AIR
                }
            }
    }

    private fun getHitBlockFace(player: Player): BlockFace? {
        val reachDistance: Double = getReachDistance(player)
        val rayTraceResult: RayTraceResult = player.rayTraceBlocks(reachDistance, FluidCollisionMode.NEVER) ?: return null
        return rayTraceResult.hitBlockFace
    }

    private val attribute: Attribute? = getAttribute(
        NamespacedKey.minecraft("player.block_interaction_range"),
        NamespacedKey.minecraft("block_interaction_range")
    )

    private fun getReachDistance(player: Player): Double {
        return attribute?.let { attr ->
            player.getAttribute(attr)?.value
        } ?: (4.5 + if (player.gameMode == GameMode.CREATIVE) 1 else 0)
    }

    private fun getAttribute(vararg keys: NamespacedKey): Attribute? {
        return keys.firstNotNullOfOrNull { k -> Registry.ATTRIBUTE.get(k) }
    }
}
