package online.aruka.oyamatsumi

import com.sk89q.worldguard.WorldGuard
import me.ryanhamshire.GriefPrevention.DataStore
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.coreprotect.CoreProtect
import net.coreprotect.CoreProtectAPI
import online.aruka.oyamatsumi.impl.MiningManager
import online.aruka.oyamatsumi.impl.MiningSettingComponent
import online.aruka.oyamatsumi.impl.listener.MineListener
import online.aruka.oyamatsumi.impl.pattern.NormalVeinPattern
import online.aruka.oyamatsumi.impl.pattern.SquareTunnelPattern
import online.aruka.oyamatsumi.interfaces.MiningPattern
import online.aruka.oyamatsumi.placeholder.OyamatsumiExpansion
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

class Oyamatsumi : JavaPlugin() {

    private fun nameSet(
        predicate: (String) -> Boolean
    ): Set<Material> {
        return Material.entries
            .filter { m -> predicate(m.name.lowercase()) }
            .toSet()
    }

    private fun loosen(
        vararg collections: Collection<Material>
    ): Set<Material> {
        val result: MutableSet<Material> = mutableSetOf()
        for (c in collections) {
            result.addAll(c)
        }
        return result
    }

    // tools
    private val allPickaxes: Set<Material> = nameSet { s -> s.endsWith("pickaxe") }
    private val allShovels: Set<Material> = nameSet { s -> s.endsWith("shovel") }
    private val allAxes: Set<Material> = nameSet { s -> s.endsWith("axe") }
    private val allHoes: Set<Material> = nameSet { s -> s.endsWith("hoe") }

    // blocks
    private val allLogs: Set<Material> = nameSet { s -> s.endsWith("log") }
    private val allWoods: Set<Material> = nameSet { s -> s.endsWith("wood") }
    private val allLeaves: Set<Material> = nameSet { s -> s.endsWith("leaves") }
    private val allWools: Set<Material> = nameSet { s -> s.endsWith("wool") }

    companion object {
        var GRIEF_PREVENTION_ENABLED: Boolean = false
        var GRIEF_PREVENTION_INSTANCE: GriefPrevention? = null
        var GRIEF_PREVENTION_DATA: DataStore? = null

        var WORLD_GUARD_ENABLED: Boolean = false
        var WORLD_GUARD: WorldGuard? = null

        var CORE_PROTECT_ENABLED: Boolean = false
        var CORE_PROTECT_API: CoreProtectAPI? = null

        lateinit var instance: Oyamatsumi
    }

    override fun onEnable() {

        instance = this
        MiningManager.loadPlayerSettings()
        getCommand("miner")?.setExecutor(MiningManager)

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

        Bukkit.getPluginManager().getPlugin("WorldGuard")?.let { _ ->
            logger.info("WorldGuard detected.")
            WORLD_GUARD_ENABLED = true
            WORLD_GUARD = WorldGuard.getInstance()
        } ?: run {
            logger.info("WorldGuard not detected.")
        }

        Bukkit.getPluginManager().getPlugin("PlaceholderAPI")?.let { _ ->
            logger.info("PlaceholderAPI detected.")
            OyamatsumiExpansion.register()
        } ?: run {
            logger.info("PlaceholderAPI not detected.")
        }

        Bukkit.getPluginManager().getPlugin("CoreProtect")?.let { pl ->
            (pl as? CoreProtect)?.let { cp ->
                CORE_PROTECT_ENABLED = true
                CORE_PROTECT_API = cp.api
                logger.info("CoreProtect detected.")
            } ?: run {
                logger.info("CoreProtect not detected.")
            }
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
            Material.SOUL_SOIL,
            Material.ROOTED_DIRT
        )

        Material
            .entries
            .filter { type -> type.name.endsWith("WOOD") && type.isBlock }
            .forEach { type ->
                val name: String = type.name.split("_").first()
                val sameWoodType: Set<Material> = Material.entries.filter { t -> t.name.startsWith(name) }.toSet()
                MiningManager.MINING_COMPONENTS.add(
                    MiningSettingComponent(
                        targets = sameWoodType,
                        tools = allAxes,
                        maxMiningBlocks = 1000,
                        NormalVeinPattern(1000)
                    )
                )
            }

        MiningManager.MINING_COMPONENTS
            .firstOrNull { c -> c.targets.contains(Material.OAK_LOG) }
            ?.let { c ->
                MiningManager.MINING_COMPONENTS.remove(c)
                MiningManager.MINING_COMPONENTS.add(
                    MiningSettingComponent(
                        targets = setOf(
                            *c.targets.toTypedArray(),
                            Material.FLOWERING_AZALEA_LEAVES,
                            Material.AZALEA_LEAVES),
                        tools = c.tools,
                        maxMiningBlocks = c.maxMiningBlocks,
                        pattern = c.pattern
                    )
                )
            }

        MiningManager.MINING_COMPONENTS.add(
            MiningSettingComponent(
                targets = Material
                    .entries
                    .filter { type -> type.name.contains("MUSHROOM") && type.isBlock }
                    .toSet(),
                tools = allAxes,
                maxMiningBlocks = 500,
                NormalVeinPattern(500)
            )
        )

        MiningManager.MINING_COMPONENTS.add(
            MiningSettingComponent(
                targets = setOf(Material.CRIMSON_STEM, Material.CRIMSON_HYPHAE),
                tools = allAxes,
                maxMiningBlocks = 500,
                NormalVeinPattern(500)
            )
        )

        MiningManager.MINING_COMPONENTS.add(
            MiningSettingComponent(
                targets = setOf(Material.WARPED_STEM, Material.WARPED_HYPHAE),
                tools = allAxes,
                maxMiningBlocks = 500,
                NormalVeinPattern(500)
            )
        )

        addNormalCategorizedSettings(
            allAxes,
            NormalVeinPattern(),
            maxMiningBlock = 200,
            Material.MANGROVE_ROOTS,
            Material.MELON,
            Material.PUMPKIN,
        )

        addNormalCategorizedSettings(
            setOf(Material.SHEARS),
            NormalVeinPattern(),
            maxMiningBlock = 200,
            *loosen(
                allLeaves, allWools
            ).toTypedArray(),
            Material.COBWEB,
        )

        addNormalCategorizedSettings(
            allHoes,
            NormalVeinPattern(),
            maxMiningBlock = 200,
            *allLeaves.toTypedArray(),
            Material.MOSS_BLOCK,
            Material.MOSSY_COBBLESTONE
        )

        MiningManager.MINING_COMPONENTS.add(
            MiningSettingComponent(
                targets = setOf(
                    Material.NETHERRACK,
                    Material.STONE,
                    Material.COBBLESTONE,
                    Material.GRANITE,
                    Material.DIORITE,
                    Material.ANDESITE,
                    Material.DEEPSLATE,
                    Material.COBBLED_DEEPSLATE,
                    Material.TUFF,
                    Material.CALCITE,
                    Material.DRIPSTONE_BLOCK,
                    Material.POINTED_DRIPSTONE,
                ),
                tools = allPickaxes,
                maxMiningBlocks = 9,
                SquareTunnelPattern()
            )
        )

        addNormalCategorizedSettings(
            tools = allPickaxes,
            pattern = NormalVeinPattern(),
            maxMiningBlock = 200,
            Material.MAGMA_BLOCK,
            *nameSet { s -> s.endsWith("ore") }.toTypedArray(),
            *nameSet { s -> s.startsWith("raw_") }.toTypedArray()
        )

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
        MiningManager.flushPlayerSettings()
    }
}
