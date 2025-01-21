package es.redactado.loader;

import com.google.inject.Injector;

public abstract class Loader {
    public Injector injector;

    public Loader(Injector injector) {
        this.injector = injector;
    }

    public abstract void onEnable(Injector injector);

    public abstract void onDisable(Injector injector);

    public abstract void onReload(Injector injector);
}
