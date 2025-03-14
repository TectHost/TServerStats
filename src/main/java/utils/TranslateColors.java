package utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TranslateColors {

    public Component translateColors(Player player, String message) {
        if (player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        message = ChatColor.translateAlternateColorCodes('&', message);
        message = TranslateHexColorCodes.translateHexColorCodes("&#", "", message);

        return MiniMessage.miniMessage().deserialize(message);
    }
}
