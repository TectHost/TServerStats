package managers;

import tect.host.TServerStats;
import utils.ConfigFile;

public class HtmlManager {

    public HtmlManager(TServerStats plugin) {
        ConfigFile statsFile = new ConfigFile("stats.html", "html", plugin);
        statsFile.registerConfig();
        ConfigFile consoleFile = new ConfigFile("console.html", "html", plugin);
        consoleFile.registerConfig();
        ConfigFile indexFile = new ConfigFile("index.html", "html", plugin);
        indexFile.registerConfig();
    }
}
