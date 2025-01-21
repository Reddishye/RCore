package es.redactado;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mojang.brigadier.CommandDispatcher;
import es.redactado.command.types.BaseCommand;
import es.redactado.loader.Loader;
import es.redactado.loader.annotations.LoaderData;
import es.redactado.loader.annotations.LoaderData.LoaderDependency;
import es.redactado.loader.enums.LoaderDependencyType;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public abstract class RPlugin extends JavaPlugin {
    private final List<Loader> loaders = new ArrayList<>();
    private final List<BaseCommand> commands = new ArrayList<>();
    private CommandDispatcher<CommandSender> dispatcher;
    private Injector injector;

    @Override
    public void onEnable() {
        // Create the main injector
        injector = Guice.createInjector(new RPluginModule(this));

        // Load and sort loaders
        try {
            loadAndSortLoaders();

            // Enable loaders in sorted order
            for (Loader loader : loaders) {
                enableLoader(loader);
            }
        } catch (Exception e) {
            getLogger().severe("Failed to load loaders: " + e.getMessage());
            e.printStackTrace();
        }

        // Register commands
        dispatcher = injector.getInstance(CommandDispatcher.class);
        for (BaseCommand command : commands) {
            command.register(dispatcher);
        }
    }

    @Override
    public void onDisable() {
        // Disable loaders in reverse order
        for (int i = loaders.size() - 1; i >= 0; i--) {
            disableLoader(loaders.get(i));
        }
    }

    /**
     * Registers a new loader dynamically.
     *
     * @param loaderClass The class of the loader to register.
     */
    public void registerLoader(Class<? extends Loader> loaderClass) {
        try {
            // Validate that the class is annotated with @LoaderData
            if (!loaderClass.isAnnotationPresent(LoaderData.class)) {
                throw new IllegalArgumentException("Loader class must be annotated with @LoaderData: " + loaderClass.getName());
            }

            // Instantiate the loader
            Loader loader = instantiateLoader(loaderClass);

            // Add the loader to the list
            loaders.add(loader);

            // Re-sort loaders to ensure correct dependency and priority order
            sortLoaders();

            // Enable the newly added loader (if already in runtime)
            if (isEnabled()) {
                enableLoader(loader);
            }

            getLogger().info("Successfully registered and enabled loader: " + getLoaderId(loader));
        } catch (Exception e) {
            getLogger().severe("Failed to register loader: " + loaderClass.getName());
            e.printStackTrace();
        }
    }

    /**
     * Loads all loaders, resolves dependencies, and sorts them by priority and dependency order.
     */
    private void loadAndSortLoaders() throws Exception {
        // Collect all loader classes annotated with @LoaderData
        List<Class<? extends Loader>> loaderClasses = findAnnotatedLoaders();

        // Instantiate loaders and add them to the list
        for (Class<? extends Loader> loaderClass : loaderClasses) {
            Loader loader = instantiateLoader(loaderClass);
            loaders.add(loader);
        }

        // Sort loaders by dependency and priority
        sortLoaders();
    }

    /**
     * Finds all classes annotated with @LoaderData.
     */
    private List<Class<? extends Loader>> findAnnotatedLoaders() throws Exception {
        List<Class<? extends Loader>> loaderClasses = new ArrayList<>();

        // Example: Replace this with your actual class discovery logic
        return loaderClasses;
    }

    /**
     * Instantiates a loader using its class.
     */
    private Loader instantiateLoader(Class<? extends Loader> loaderClass) throws Exception {
        return loaderClass.getConstructor(Injector.class).newInstance(injector);
    }

    /**
     * Sorts the loaders based on their dependencies and priorities.
     */
    private void sortLoaders() throws Exception {
        Map<String, Loader> loaderMap = new HashMap<>();
        for (Loader loader : loaders) {
            LoaderData data = loader.getClass().getAnnotation(LoaderData.class);
            if (data == null) throw new IllegalStateException("Loader missing @LoaderData annotation: " + loader.getClass());
            loaderMap.put(data.id(), loader);
        }

        List<Loader> sortedLoaders = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (Loader loader : loaders) {
            resolveDependencies(loader, sortedLoaders, visited, loaderMap);
        }

        // Replace the original list with the sorted one
        this.loaders.clear();
        this.loaders.addAll(sortedLoaders);
    }

    /**
     * Resolves dependencies recursively and adds the loaders to the sorted list.
     */
    private void resolveDependencies(Loader loader, List<Loader> sortedLoaders, Set<String> visited, Map<String, Loader> loaderMap) throws Exception {
        LoaderData data = loader.getClass().getAnnotation(LoaderData.class);

        if (visited.contains(data.id())) return; // Already processed

        visited.add(data.id());

        for (LoaderDependency dependency : data.dependsOn()) {
            String dependencyId = dependency.id();
            Loader dependentLoader = loaderMap.get(dependencyId);

            if (dependentLoader == null) {
                throw new IllegalStateException("Missing dependency: " + dependencyId + " for loader: " + data.id());
            }

            if (dependency.type() == LoaderDependencyType.BEFORE) {
                resolveDependencies(dependentLoader, sortedLoaders, visited, loaderMap);
            }
        }

        sortedLoaders.add(loader);

        for (LoaderDependency dependency : data.dependsOn()) {
            if (dependency.type() == LoaderDependencyType.AFTER) {
                String dependencyId = dependency.id();
                Loader dependentLoader = loaderMap.get(dependencyId);

                if (!sortedLoaders.contains(dependentLoader)) {
                    resolveDependencies(dependentLoader, sortedLoaders, visited, loaderMap);
                }
            }
        }
    }

    /**
     * Enables a single loader.
     */
    private void enableLoader(Loader loader) {
        try {
            getLogger().info("Enabling loader: " + getLoaderId(loader));
            loader.onEnable(injector);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enable loader: " + getLoaderId(loader), e);
        }
    }

    /**
     * Disables a single loader.
     */
    private void disableLoader(Loader loader) {
        try {
            getLogger().info("Disabling loader: " + getLoaderId(loader));
            loader.onDisable(injector);
        } catch (Exception e) {
            getLogger().severe("Failed to disable loader: " + getLoaderId(loader));
            e.printStackTrace();
        }
    }

    /**
     * Gets the ID of a loader from its @LoaderData annotation.
     */
    private String getLoaderId(Loader loader) {
        LoaderData data = loader.getClass().getAnnotation(LoaderData.class);
        return data != null ? data.id() : "Unknown";
    }
}
