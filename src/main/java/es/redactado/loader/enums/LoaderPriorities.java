package es.redactado.loader.enums;

public enum LoaderPriorities {
    VERY_LOW, // Lowest priority available. Should not be used unless strongly required.
    LOW, // This priority may be used on cases where you need a lower priority than normal.
    NORMAL, // Default priority. Recommended for most loaders.
    HIGH, // This priority may be used on cases where you need a higher priority than normal.
    VERY_HIGH // Highest priority available. Should not be used unless strongly required.
}
