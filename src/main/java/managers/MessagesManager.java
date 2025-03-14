package managers;

import org.bukkit.configuration.file.FileConfiguration;
import tect.host.TServerStats;
import utils.ConfigFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessagesManager {

    private ConfigFile messagesFile;
    private final TServerStats plugin;
    private Map<String, String> messages;

    public MessagesManager(TServerStats plugin) {
        this.plugin = plugin;
        this.messagesFile = new ConfigFile(plugin.getConfigManager().getLangFile(), "lang", plugin);
        this.messagesFile.registerConfig();
        loadConfig();
        generateAdditionalFiles();
    }

    public void loadConfig() {
        FileConfiguration config = messagesFile.getConfig();
        messages = new HashMap<>();

        for (String key : Objects.requireNonNull(config.getConfigurationSection("messages")).getKeys(false)) {
            messages.put(key, config.getString("messages." + key));
        }
    }

    public void reloadConfig() {
        this.messagesFile = new ConfigFile(plugin.getConfigManager().getLangFile(), "lang", plugin);
        messagesFile.reloadConfig();
        loadConfig();
    }

    public void generateAdditionalFiles() {
        createConfigFile("messages_es.yml");
        createConfigFile("messages_en.yml");
    }

    private void createConfigFile(String fileName) {
        ConfigFile configFile = new ConfigFile(fileName, "lang", plugin);
        configFile.registerConfig();
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }
}
