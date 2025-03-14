package utils;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tect.host.TServerStats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class WebServer {

    private HttpServer server;
    private final TServerStats plugin;
    private final int PORT;
    private static final String BASE_PATH = "html";
    private static final String FOOTER = "<footer style='text-align:center; margin-top:20px; font-size:12px;'>TServerStats - Made by Tect.host</footer>";

    public WebServer(@NotNull TServerStats plugin) {
        this.plugin = plugin;
        this.PORT = plugin.getConfigManager().getPort();
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new FileHandler());
            if (plugin.getConfigManager().isStatsEnabled()) { server.createContext(plugin.getConfigManager().getStatsRouteFile(), new StatsHandler()); }
            if (plugin.getConfigManager().isConsoleEnabled()) { server.createContext(plugin.getConfigManager().getConsoleRouteFile(), new ConsoleHandler()); }
            server.start();
            plugin.getLogger().log(Level.INFO, "WebServer started on port " + PORT);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error starting WebServer", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().log(Level.INFO, "WebServer stopped");
        }
    }

    public void openWebPage() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(plugin.getConfigManager().getLink()));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "The website could not be opened: ", e);
            }
        });
    }

    private class FileHandler implements HttpHandler {
        @Override
        public void handle(@NotNull HttpExchange exchange) throws IOException {
            String requestedFile = exchange.getRequestURI().getPath();

            if (requestedFile.equals("/")) {
                requestedFile = plugin.getConfigManager().getIndexFile();
            }

            Path filePath = Path.of(plugin.getDataFolder().toString(), BASE_PATH, requestedFile);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String htmlContent = readHtmlFile(filePath);
                String translatedHtml = PlaceholderAPI.setPlaceholders(null, htmlContent);
                translatedHtml += FOOTER;

                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                byte[] responseBytes = translatedHtml.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                send404(exchange);
            }
        }

        private @NotNull String readHtmlFile(Path filePath) throws IOException {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        }

        private void send404(@NotNull HttpExchange exchange) throws IOException {
            String notFoundHtml = "<html><body><h1>ERROR 404: Not Found</h1></body></html>";
            exchange.sendResponseHeaders(404, notFoundHtml.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFoundHtml.getBytes(StandardCharsets.UTF_8));
            }
            plugin.getLogger().log(Level.WARNING, "404 Not Found for: " + exchange.getRequestURI().getPath());
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(@NotNull HttpExchange exchange) throws IOException {
            try {
                List<String> players = Bukkit.getOnlinePlayers().stream()
                        .map(this::getPlayerHtml)
                        .toList();

                Path indexPath = Path.of(plugin.getDataFolder().toString(), BASE_PATH, plugin.getConfigManager().getStatsFile());
                String htmlResponse = Files.exists(indexPath) ? new String(Files.readAllBytes(indexPath)) : "<html><body><h1>ERROR 404: Stats Page Not Found</h1></body></html>";
                htmlResponse = htmlResponse.replace("%player_list%", String.join("", players));
                htmlResponse = PlaceholderAPI.setPlaceholders(null, htmlResponse);
                htmlResponse += FOOTER;

                byte[] responseBytes = htmlResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error in StatsHandler: ", e);
                exchange.sendResponseHeaders(500, 0);
            }
        }

        private @NotNull String getPlayerHtml(@NotNull Player player) {
            String rank = PlaceholderAPI.setPlaceholders(player, plugin.getConfigManager().getPrefixPlaceholder());
            String hoursPlayed = PlaceholderAPI.setPlaceholders(player, plugin.getConfigManager().getHoursPlaceholder());
            rank = removeMinecraftColorCodes(rank);

            return """
                <div class="player">
                    <img src="https://mc-heads.net/avatar/%s" alt="Player Avatar" class="player-avatar">
                    <div class="player-info">
                        <div class="player-name">%s</div>
                        <div class="player-rank">%s • %sh jugadas</div>
                    </div>
                </div>
            """.formatted(player.getName(), player.getName(), rank, hoursPlayed);
        }

        private @NotNull String removeMinecraftColorCodes(@NotNull String message) {
            return message.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
        }
    }

    private class ConsoleHandler implements HttpHandler {
        @Override
        public void handle(@NotNull HttpExchange exchange) throws IOException {
            String requestedFile = exchange.getRequestURI().getPath();
            if (plugin.getConfigManager().getConsoleRouteFile().equals(requestedFile)) {
                if ("POST".equals(exchange.getRequestMethod())) {
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        requestBody.append(line);
                    }

                    String command = extractCommand(requestBody.toString());
                    String output = executeCommand(command);

                    String jsonResponse = "{\"output\": \"" + output + "\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, jsonResponse.length());

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    requestedFile = plugin.getConfigManager().getConsoleFile();
                    Path filePath = Path.of(plugin.getDataFolder().toString(), BASE_PATH, requestedFile);

                    if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                        String htmlContent = readHtmlFile(filePath);
                        String translatedHtml = PlaceholderAPI.setPlaceholders(null, htmlContent);
                        translatedHtml += FOOTER;

                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        byte[] responseBytes = translatedHtml.getBytes(StandardCharsets.UTF_8);
                        exchange.sendResponseHeaders(200, responseBytes.length);

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(responseBytes);
                        }
                    } else {
                        send404(exchange);
                    }
                }
            }
        }

        private @NotNull String readHtmlFile(Path filePath) throws IOException {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        }

        private void send404(@NotNull HttpExchange exchange) throws IOException {
            String notFoundHtml = "<html><body><h1>ERROR 404: Not Found</h1></body></html>";
            exchange.sendResponseHeaders(404, notFoundHtml.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFoundHtml.getBytes(StandardCharsets.UTF_8));
            }
        }

        private String extractCommand(String requestBody) {
            try {
                Gson gson = new Gson();
                Map map = gson.fromJson(requestBody, Map.class);
                return map.get("command").toString();
            } catch (Exception e) {
                return "";
            }
        }

        private @NotNull String executeCommand(String command) {
            try {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                });

                return "Executed command: " + command;
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing the command on the server: " + e.getMessage());
                return "Error executing the command on the server: " + e.getMessage();
            }
        }
    }
}
