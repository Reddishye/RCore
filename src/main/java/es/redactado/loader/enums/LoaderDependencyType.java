package es.redactado.loader.enums;

public enum LoaderDependencyType {
    BEFORE, // The required loader will be loaded before the dependant loader
    AFTER // The required loader will be loaded after the dependant loader
}
