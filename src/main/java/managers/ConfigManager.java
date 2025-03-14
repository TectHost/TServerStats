package managers;

import org.bukkit.configuration.file.FileConfiguration;
import tect.host.TServerStats;
import utils.ConfigFile;

public class ConfigManager {
    private final ConfigFile configFile;

    private String langFile;
    private boolean statsEnabled;
    private boolean consoleEnabled;
    private int port;
    private String indexFile;
    private String statsRouteFile;
    private String link;
    private String statsFile;
    private String prefixPlaceholder;
    private String hoursPlaceholder;
    private String consoleRouteFile;
    private String consoleFile;

    public ConfigManager(TServerStats plugin) {
        this.configFile = new ConfigFile("config.yml", null, plugin);
        this.configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        langFile = config.getString("lang-file");
        statsEnabled = config.getBoolean("general.stats-page");
        consoleEnabled = config.getBoolean("general.console-page");
        port = config.getInt("web.port");
        indexFile = config.getString("web.general.index-file");
        statsRouteFile = config.getString("web.stats.route");
        link = config.getString("web.link");
        statsFile = config.getString("web.stats.stats-file");
        prefixPlaceholder = config.getString("players.prefix-placeholder");
        hoursPlaceholder = config.getString("players.hours-placeholder");
        consoleRouteFile = config.getString("web.console.route");
        consoleFile = config.getString("web.console.console-file");
    }

    public void reloadConfig() {
        configFile.reloadConfig();
        loadConfig();
    }

    public String getLangFile() {
        return langFile;
    }

    public String getConsoleRouteFile() {
        return consoleRouteFile;
    }

    public String getConsoleFile() {
        return consoleFile;
    }

    public String getPrefixPlaceholder() {
        return prefixPlaceholder;
    }

    public String getHoursPlaceholder() {
        return hoursPlaceholder;
    }

    public String getStatsFile() {
        return statsFile;
    }

    public String getLink() {
        return link;
    }

    public String getStatsRouteFile() {
        return statsRouteFile;
    }

    public String getIndexFile() {
        return indexFile;
    }

    public int getPort() {
        return port;
    }

    public boolean isStatsEnabled() {
        return statsEnabled;
    }

    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }
}
