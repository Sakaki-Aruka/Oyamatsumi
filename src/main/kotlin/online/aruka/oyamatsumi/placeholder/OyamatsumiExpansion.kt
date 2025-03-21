package online.aruka.oyamatsumi.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import online.aruka.oyamatsumi.Oyamatsumi
import online.aruka.oyamatsumi.impl.MiningManager
import org.bukkit.OfflinePlayer

object OyamatsumiExpansion: PlaceholderExpansion() {
    override fun getIdentifier(): String = "oyamatsumi"

    override fun getAuthor(): String = "Sakaki-Aruka"

    override fun getVersion(): String {
        @Suppress("UnstableApiUsage")
        return Oyamatsumi.instance.pluginMeta.version
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        player?.let { p ->
            // player = not null, params = EMPTY
            // Returns a player is using bulk mining or not. ("true" or "false")
            if (params.isEmpty()) return (p.uniqueId in MiningManager.ENABLED_PLAYERS).toString()
        } ?: run {
            if (params == "enabled_all") {
                // player = null, params = "enabled_all"
                // Returns enabled players uuid list.
                // Each id is separated with a comma.
                return MiningManager.ENABLED_PLAYERS
                    .joinToString(",") { uid -> uid.toString() }
            }
        }

        // If a queried player is not registered or received an illegal parameter,
        // this returns empty string.
        return ""
    }
}