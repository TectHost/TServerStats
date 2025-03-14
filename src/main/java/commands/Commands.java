package commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import tect.host.TServerStats;
import utils.WebServer;

import java.util.Map;
import java.util.function.Consumer;

public class Commands implements CommandExecutor {
    private final TServerStats plugin;
    private final WebServer webServer;
    private final Map<String, Consumer<CommandSender>> commands;

    public Commands(TServerStats plugin) {
        this.plugin = plugin;
        this.webServer = new WebServer(plugin);
        this.commands = Map.of(
                "reload", sender -> executeIfPermitted(sender, "tstats.reload", () -> reloadConfigs(sender)),
                "version", sender -> executeIfPermitted(sender, "tstats.version", () -> sendVersion(sender)),
                "viewer", sender -> executeIfPermitted(sender, "tstats.viewer", () -> openViewer(sender))
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length != 1 || !commands.containsKey(args[0].toLowerCase())) {
            sender.sendMessage(getMessage("unknown"));
            return false;
        }

        commands.get(args[0].toLowerCase()).accept(sender);
        return true;
    }

    private void openViewer(@NotNull CommandSender sender) {
        Bukkit.getScheduler().runTask(plugin, webServer::openWebPage);
        sender.sendMessage(getMessage("openViewer"));
    }

    private void reloadConfigs(@NotNull CommandSender sender) {
        plugin.getConfigManager().reloadConfig();
        plugin.getMessagesManager().reloadConfig();
        sender.sendMessage(getMessage("reload"));
    }

    private void sendVersion(@NotNull CommandSender sender) {
        sender.sendMessage(getMessage("version").replaceText(builder ->
                builder.matchLiteral("%version%").replacement(plugin.getDescription().getVersion())
        ));
    }

    private void executeIfPermitted(@NotNull CommandSender sender, String permission, Runnable action) {
        if (sender.hasPermission(permission) || sender.hasPermission("tstats.admin")) {
            action.run();
        } else {
            sender.sendMessage(getMessage("noPerm"));
        }
    }

    private Component getMessage(String key) {
        String message = plugin.getMessagesManager().getMessage(key);
        return plugin.getTranslateColors().translateColors(null, message);
    }
}
