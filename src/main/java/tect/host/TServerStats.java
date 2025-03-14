package tect.host;

import com.mojang.brigadier.Message;
import commands.Commands;
import managers.ConfigManager;
import managers.HtmlManager;
import managers.MessagesManager;
import org.bukkit.plugin.java.JavaPlugin;
import utils.TranslateColors;
import utils.WebServer;

import java.util.Objects;

public final class TServerStats extends JavaPlugin {
    private TranslateColors translateColors;
    private ConfigManager configManager;
    private WebServer webServer;
    private MessagesManager messagesManager;

    @Override
    public void onEnable() {
        getLogger().info("Starting TVoidGenerator...");

        loadConfigFiles();
        loadCommands();
        initializeManagers();
        loadWebPage();

        getLogger().info("TVoidGenerator Started!");
    }

    @Override
    public void onDisable() {
        webServer.stop();
        getLogger().warning("Stopping TVoidGenerator!");
    }

    public void loadWebPage() {
        webServer = new WebServer(this);
        webServer.start();
    }

    public void loadCommands() {
        Objects.requireNonNull(this.getCommand("tserverstats")).setExecutor(new Commands(this));
    }

    public void initializeManagers() {
        translateColors = new TranslateColors();
    }

    public void loadConfigFiles() {
        configManager = new ConfigManager(this);
        messagesManager = new MessagesManager(this);
        new HtmlManager(this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WebServer getWebServer() {
        return webServer;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public TranslateColors getTranslateColors() {
        return translateColors;
    }
}
