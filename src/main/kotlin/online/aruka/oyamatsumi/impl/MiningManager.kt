package online.aruka.oyamatsumi.impl

import net.kyori.adventure.text.minimessage.MiniMessage
import online.aruka.oyamatsumi.Oyamatsumi
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.util.UUID

object MiningManager: CommandExecutor {
    val MINING_COMPONENTS: MutableSet<MiningSettingComponent> = mutableSetOf()
    val ENABLED_PLAYERS: MutableSet<UUID> = mutableSetOf()

    fun loadEnabledPlayers() {
        Oyamatsumi.instance.saveDefaultConfig()
        val config: FileConfiguration = Oyamatsumi.instance.config
        config.getStringList("enabled")
            .forEach { id ->
                ENABLED_PLAYERS.add(UUID.fromString(id))
            }
    }

    fun flushEnabledPlayers() {
        val config: FileConfiguration = Oyamatsumi.instance.config
        config.set("enabled", ENABLED_PLAYERS.map { u -> u.toString() })
        Oyamatsumi.instance.saveConfig()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command is only for a player, not for a console.")
            return true
        }

        if (sender.uniqueId in ENABLED_PLAYERS) {
            // off
            ENABLED_PLAYERS.remove(sender.uniqueId)
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Bulk Mining Disabled."))
        } else {
            // on
            ENABLED_PLAYERS.add(sender.uniqueId)
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Bulk Mining Enabled!!!"))
        }
        return true
    }
}