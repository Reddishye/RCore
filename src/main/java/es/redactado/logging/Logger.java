package es.redactado.logging;

import lombok.Getter;
import org.spacemc.randomUtils.config.Config;
import org.spacemc.randomUtils.config.ConfigContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public class Logger {
    private final MiniMessage miniMessage;
    private final Component prefix;
    private final Audience console;
    private final ConfigContainer<Config> configContainer;
    private final Config config;

    @Inject
    public Logger(JavaPlugin plugin, ConfigContainer<Config> configContainer) {
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = miniMessage.deserialize("<#eb64f8><bold>RandomUtils</bold></#eb64f8> <dark_gray>» ");
        this.console = BukkitAudiences.create(plugin).console();
        this.configContainer = configContainer;
        this.config = configContainer.get();
    }

    public void info(String message, Object... args) {
        log(LogLevel.INFO, message, args);
    }

    public void info(Component message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message, Object... args) {
        log(LogLevel.WARN, message, args);
    }

    public void warn(Component message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message, Object... args) {
        log(LogLevel.ERROR, message, args);
    }

    public void error(Component message) {
        log(LogLevel.ERROR, message);
    }

    public void debug(String message, Object... args) {
        if (config.isDebug) {
            log(LogLevel.DEBUG, message, args);
        }
    }

    public void debug(Component message) {
        if (config.isDebug) {
            log(LogLevel.DEBUG, message);
        }
    }

    private void log(LogLevel level, String message, Object... args) {
        Component formattedMessage = prefix.append(level.getColor()).append(miniMessage.deserialize(String.format(message, args)));
        console.sendMessage(formattedMessage);
    }

    private void log(LogLevel level, Component message) {
        Component formattedMessage = prefix.append(level.getColor()).append(message);
        console.sendMessage(formattedMessage);
    }

    @Getter
    private enum LogLevel {
        INFO(Component.text().color(net.kyori.adventure.text.format.TextColor.fromHexString("#f9b4eb")).build()),
        WARN(Component.text().color(net.kyori.adventure.text.format.TextColor.fromHexString("#f9e5b4")).build()),
        ERROR(Component.text().color(net.kyori.adventure.text.format.TextColor.fromHexString("#f9beb4")).build()),
        DEBUG(Component.text().color(net.kyori.adventure.text.format.TextColor.fromHexString("#b4c8f9")).build());

        private final Component color;

        LogLevel(Component color) {
            this.color = color;
        }

    }
}