package online.aruka.oyamatsumi

import com.sk89q.worldguard.WorldGuard
import me.ryanhamshire.GriefPrevention.DataStore
import me.ryanhamshire.GriefPrevention.GriefPrevention
import online.aruka.oyamatsumi.impl.MiningManager
import online.aruka.oyamatsumi.impl.MiningSettingComponent
import online.aruka.oyamatsumi.impl.listener.MineListener
import online.aruka.oyamatsumi.impl.pattern.NormalVeinPattern
import online.aruka.oyamatsumi.impl.pattern.SquareTunnelPattern
import online.aruka.oyamatsumi.interfaces.MiningPattern
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

class Oyamatsumi : JavaPlugin() {

    private val allPickaxes: Set<Material> = Material.entries.filter { type -> type.name.lowercase().endsWith("pickaxe") }.toSet()
    private val allShovels: Set<Material> = Material.entries.filter { type -> type.name.lowercase().endsWith("shovel") }.toSet()
    private val allAxes: Set<Material> = Material.entries.filter { type -> type.name.lowercase().endsWith("axe") }.toSet()

    companion object {
        var GRIEF_PREVENTION_ENABLED: Boolean = false
        var GRIEF_PREVENTION_INSTANCE: GriefPrevention? = null
        var GRIEF_PREVENTION_DATA: DataStore? = null

        var WORLD_GUARD_ENABLED: Boolean = false
        var WORLD_GUARD: WorldGuard? = null
    }

    override fun onEnable() {

        Bukkit.getPluginManager().getPlugin("GriefPrevention")?.let { pl ->
            logger.info("GriefPrevention detected.")
            (pl as? GriefPrevention)?.let { instance ->
                GRIEF_PREVENTION_ENABLED = true
                GRIEF_PREVENTION_INSTANCE = instance
                GRIEF_PREVENTION_DATA = instance.dataStore
            } ?: run {
                logger.info("Failed to enable GriefPrevention features.")
            }
        } ?: run {
            logger.info("GriefPrevention not detected.")
        }

        Bukkit.getPluginManager().getPlugin("WorldGuard")?.let { pl ->
            logger.info("WorldGuard detected.")
            WORLD_GUARD_ENABLED = true
            WORLD_GUARD = WorldGuard.getInstance()
        } ?: run {
            logger.info("WorldGuard not detected.")
        }

        Bukkit.getPluginManager().registerEvents(MineListener, this)
        addNormalCategorizedSettings(
            allShovels,
            SquareTunnelPattern(),
            maxMiningBlock = 9,
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.COARSE_DIRT,
            Material.PODZOL,
            Material.MYCELIUM,
            Material.SAND,
            Material.RED_SAND,
            Material.GRAVEL,
            Material.CLAY,
            Material.MUD,
            Material.PACKED_MUD,
            Material.SNOW_BLOCK,
            Material.SNOW,
            Material.ICE,
            Material.PACKED_ICE,
            Material.BLUE_ICE,
            Material.FROSTED_ICE,
            Material.SOUL_SAND,
            Material.SOUL_SOIL
        )

        addNormalCategorizedSettings(
            allPickaxes,
            SquareTunnelPattern(),
            maxMiningBlock = 9,
            Material.NETHERRACK,
            Material.STONE,
            Material.GRANITE,
            Material.DIORITE,
            Material.ANDESITE,
            Material.DEEPSLATE,
            Material.TUFF,
            Material.CALCITE,
            Material.DRIPSTONE_BLOCK,
            Material.POINTED_DRIPSTONE,
            Material.ROOTED_DIRT
        )

        addNormalCategorizedSettings(
            allAxes,
            NormalVeinPattern(),
            maxMiningBlock = 200,
            Material.ACACIA_LOG,
            Material.ACACIA_WOOD,
            Material.BIRCH_LOG,
            Material.BIRCH_WOOD,
            Material.BROWN_MUSHROOM_BLOCK,
            Material.CARVED_PUMPKIN,
            Material.CHERRY_LOG,
            Material.CHERRY_WOOD,
            Material.CRIMSON_HYPHAE,
            Material.CRIMSON_STEM,
            Material.DARK_OAK_LOG,
            Material.DARK_OAK_WOOD,
            Material.JUNGLE_LOG,
            Material.JUNGLE_WOOD,
            Material.MANGROVE_LOG,
            Material.MANGROVE_ROOTS,
            Material.MANGROVE_WOOD,
            Material.MELON,
            Material.OAK_LOG,
            Material.OAK_WOOD,
            Material.PUMPKIN,
            Material.RED_MUSHROOM_BLOCK,
            Material.SPRUCE_LOG,
            Material.SPRUCE_WOOD,
            Material.WARPED_HYPHAE,
            Material.WARPED_STEM
        )

        addNormalCategorizedSettings(
            setOf(Material.SHEARS),
            NormalVeinPattern(),
            maxMiningBlock = 200,
            Material.ACACIA_LEAVES,
            Material.AZALEA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.BLACK_WOOL,
            Material.BLUE_WOOL,
            Material.BROWN_WOOL,
            Material.CHERRY_LEAVES,
            Material.COBWEB,
            Material.CYAN_WOOL,
            Material.DARK_OAK_LEAVES,
            Material.FLOWERING_AZALEA_LEAVES,
            Material.GRAY_WOOL,
            Material.GREEN_WOOL,
            Material.JUNGLE_LEAVES,
            Material.LIGHT_BLUE_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.LIME_WOOL,
            Material.MAGENTA_WOOL,
            Material.MANGROVE_LEAVES,
            Material.OAK_LEAVES,
            Material.ORANGE_WOOL,
            Material.PINK_WOOL,
            Material.PURPLE_WOOL,
            Material.RED_WOOL,
            Material.SPRUCE_LEAVES,
            Material.WHITE_WOOL,
            Material.YELLOW_WOOL
        )

        MiningManager.MINING_COMPONENTS.add(
            MiningSettingComponent(
                targets = setOf(Material.MAGMA_BLOCK),
                tools = allPickaxes,
                maxMiningBlocks = 200,
                pattern = NormalVeinPattern()
            )
        )

        Material.entries
            .filter { type -> type.name.lowercase().endsWith("stone") }
            .forEach { type ->
                MiningManager.MINING_COMPONENTS.add(
                    MiningSettingComponent(
                        targets = setOf(type),
                        tools = allPickaxes,
                        maxMiningBlocks = 9,
                        pattern = SquareTunnelPattern()
                    )
                )
            }

        Material.entries
            .filter { type -> type.name.lowercase().endsWith("ore") }
            .forEach { type ->
                MiningManager.MINING_COMPONENTS.add(
                    MiningSettingComponent(
                        targets = setOf(type),
                        tools = allPickaxes,
                        maxMiningBlocks = 200,
                        pattern = NormalVeinPattern()
                    )
                )
            }


        Material.entries
            .filter { type -> type.name.lowercase().startsWith("raw_") }
            .forEach { type ->
                MiningManager.MINING_COMPONENTS.add(
                    MiningSettingComponent(
                        targets = setOf(type),
                        tools = allPickaxes,
                        maxMiningBlocks = 200,
                        pattern = NormalVeinPattern()
                    )
                )
            }

    }

    private fun addNormalCategorizedSettings(
        tools: Set<Material>,
        pattern: MiningPattern,
        maxMiningBlock: Int,
        vararg types: Material
    ) {
        types.forEach { t ->
            MiningManager.MINING_COMPONENTS.add(MiningSettingComponent(setOf(t), tools, maxMiningBlock, pattern))
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
