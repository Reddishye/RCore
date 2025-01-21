package es.redactado.loader.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import es.redactado.loader.enums.LoaderDependencyType;
import es.redactado.loader.enums.LoaderPriorities;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoaderData {
    /**
     * The ID of the loader. This is used to identify the loader.
     */
    String id();

    /**
     * The loaders that this loader depends on. If a loader is not loaded, this loader will not be loaded.
     */
    LoaderDependency[] dependsOn() default {};

    /**
     * The priority of the loader. This is used to determine the order in which the loaders are loaded.
     */
    LoaderPriorities priority() default LoaderPriorities.NORMAL;

    @interface LoaderDependency {
        String id();

        LoaderDependencyType type();
    }
}