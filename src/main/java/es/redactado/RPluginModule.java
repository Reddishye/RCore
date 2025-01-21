package es.redactado;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import es.redactado.logging.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public class RPluginModule extends AbstractModule {
    private final JavaPlugin plugin;

    public RPluginModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);
        bind(Logger.class).asEagerSingleton();
        bind(String.class).annotatedWith(Named.class).toInstance("config.yml");
    }

    @Provides
    private BukkitAudiences provideBukkitAudiences() {
        return BukkitAudiences.create(plugin);
    }
}
