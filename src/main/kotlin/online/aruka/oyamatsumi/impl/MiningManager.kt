package online.aruka.oyamatsumi.impl

import net.kyori.adventure.text.minimessage.MiniMessage
import online.aruka.oyamatsumi.Oyamatsumi
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.util.UUID

object MiningManager: CommandExecutor {
    val MINING_COMPONENTS: MutableSet<MiningSettingComponent> = mutableSetOf()
    val ENABLED_PLAYERS: MutableSet<UUID> = mutableSetOf()
    val FORCE_OFF_PLAYERS: MutableSet<UUID> = mutableSetOf()

    fun loadPlayerSettings() {
        Oyamatsumi.instance.saveDefaultConfig()
        val config: FileConfiguration = Oyamatsumi.instance.config
        config.getStringList("enabled")
            .forEach { id ->
                ENABLED_PLAYERS.add(UUID.fromString(id))
            }

        config.getStringList("force_off")
            .forEach { id ->
                FORCE_OFF_PLAYERS.add(UUID.fromString(id))
            }
    }

    fun flushPlayerSettings() {
        val config: FileConfiguration = Oyamatsumi.instance.config
        config.set("enabled", ENABLED_PLAYERS.map { u -> u.toString() })
        config.set("force_off", FORCE_OFF_PLAYERS.map { u -> u.toString() })
        Oyamatsumi.instance.saveConfig()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender.hasPermission("oyamatsumi.normal")
                && (args == null || args.isEmpty())
                && sender is Player
            ) {
            toggle(sender)
            return true
        }

        if ((sender.hasPermission("oyamatsumi.admin") || sender is ConsoleCommandSender) && args != null) {
            if (args[0] == "force-off" || args[0] == "force-on") {
                val identity: UUID =
                    if (args.size < 2) return false
                    else if (args[1].matches(Regex("[a-z0-9]{8}-([a-z0-8]{4}-){3}[a-z0-9]{12}"))) UUID.fromString(args[1])
                    else Bukkit.getOnlinePlayers().firstOrNull { p -> p.name == args[1] }
                        ?.uniqueId
                        ?: Bukkit.getOfflinePlayer(args[1]).uniqueId
                when (args[0]) {
                    "force-off" -> forceOff(identity, sender) // /miner-admin force-off (name or uuid)
                    "force-on" -> forceOn(identity, sender) // /miner-admin force-on (name or uuid)
                }
            }
            else if (args[0] == "flush") flushPlayerSettings()
            else if (args[0] == "load") {
                ENABLED_PLAYERS.clear()
                FORCE_OFF_PLAYERS.clear()
                loadPlayerSettings()
            }
        }
        return true
    }

    private fun forceOn(target: UUID, sender: CommandSender) {
        if (ENABLED_PLAYERS.add(target)) {
            sender.sendMessage("Turn on successful. (Target = $target")
        } else sender.sendMessage("The specified player is already turn on or not exists.")

        if (target in FORCE_OFF_PLAYERS) FORCE_OFF_PLAYERS.remove(target)
    }

    private fun forceOff(target: UUID, sender: CommandSender) {
        if (ENABLED_PLAYERS.remove(target)) {
            sender.sendMessage("Turn off successful. (Target = $target")
        } else sender.sendMessage("The specified player is not found or not turn on bulk mining.")
        FORCE_OFF_PLAYERS.add(target)
    }

    private fun toggle(player: Player) {
        if (player.uniqueId in ENABLED_PLAYERS) {
            // off
            ENABLED_PLAYERS.remove(player.uniqueId)
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Bulk Mining Disabled."))
        } else {
            // on
            if (player.uniqueId in FORCE_OFF_PLAYERS) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You cannot toggle this."))
                return
            }
            ENABLED_PLAYERS.add(player.uniqueId)
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Bulk Mining Enabled!!!"))
        }
    }
}