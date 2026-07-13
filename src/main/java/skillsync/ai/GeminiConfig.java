package skillsync.ai;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads Gemini API configuration for the SkillSync platform from
 * {@code application.properties} on the classpath.
 * <p>
 * Configuration is loaded lazily, on first access, rather than in a static
 * initializer. This is a deliberate design choice: a failure inside a static
 * initializer (for example, a missing properties file) would be wrapped by
 * the JVM in an {@link ExceptionInInitializerError} and would permanently
 * poison this class for the remainder of the JVM's lifetime, turning every
 * later access into a {@link NoClassDefFoundError} regardless of whether the
 * underlying problem is fixed. Lazy loading with double-checked locking
 * avoids that failure mode: every access attempt throws a plain, catchable
 * {@link GeminiConfigException} and can succeed on a later attempt.
 */
public final class GeminiConfig {

    /** Classpath location of the properties file this class reads from. */
    private static final String CONFIG_FILE = "application.properties";

    /** Property key under which the Gemini API key is expected to be defined. */
    private static final String API_KEY_PROPERTY = "gemini.api.key";

    /** Property key under which the Gemini API endpoint may optionally be defined. */
    private static final String ENDPOINT_PROPERTY = "gemini.api.endpoint";

    /** Endpoint used when {@code gemini.api.endpoint} is absent from the properties file. */
  private static final String DEFAULT_ENDPOINT =
"https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";

    /** Lazily loaded, cached properties. Guarded by {@link #LOCK}. */
    private static volatile Properties cachedProperties;

    /** Lazily resolved, cached API key. Guarded by {@link #LOCK}. */
    private static volatile String cachedApiKey;

    /** Lazily resolved, cached endpoint. Guarded by {@link #LOCK}. */
    private static volatile String cachedEndpoint;

    /** Monitor used to synchronize lazy initialization of the cached fields. */
    private static final Object LOCK = new Object();

    /**
     * Private constructor to prevent instantiation of this static utility class.
     */
    private GeminiConfig() {
    }

    /**
     * Returns the configured Gemini API key, loading and validating it from
     * {@code application.properties} on first call.
     *
     * @return the non-blank API key
     * @throws GeminiConfigException if the properties file is missing, unreadable,
     *                                or does not define a non-blank {@code gemini.api.key}
     */
    public static String getApiKey() {
        String apiKey = cachedApiKey;
        if (apiKey == null) {
            synchronized (LOCK) {
                apiKey = cachedApiKey;
                if (apiKey == null) {
                    apiKey = resolveApiKey(loadProperties());
                    cachedApiKey = apiKey;
                }
            }
        }
        return apiKey;
    }

    /**
     * Returns the configured Gemini API endpoint, loading it from
     * {@code application.properties} on first call and falling back to the
     * default Gemini 2.5 Flash {@code generateContent} endpoint if none is configured.
     *
     * @return the API endpoint URL
     * @throws GeminiConfigException if the properties file exists but cannot be read
     */
    public static String getEndpoint() {
        String endpoint = cachedEndpoint;
        if (endpoint == null) {
            synchronized (LOCK) {
                endpoint = cachedEndpoint;
                if (endpoint == null) {
                    endpoint = resolveEndpoint(loadProperties());
                    cachedEndpoint = endpoint;
                }
            }
        }
        return endpoint;
    }

    /**
     * Loads and caches {@link #CONFIG_FILE} from the classpath.
     * Safe to call repeatedly; the file is only read from disk once.
     *
     * @return the loaded properties
     * @throws GeminiConfigException if the file is missing or cannot be read
     */
    private static Properties loadProperties() {
        Properties properties = cachedProperties;
        if (properties != null) {
            return properties;
        }

        Properties loaded = new Properties();
        try (InputStream input = GeminiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new GeminiConfigException(
                        "Configuration file '" + CONFIG_FILE + "' was not found on the classpath.");
            }
            loaded.load(input);
        } catch (IOException ex) {
            throw new GeminiConfigException(
                    "Failed to read configuration file '" + CONFIG_FILE + "'.", ex);
        }

        cachedProperties = loaded;
        return loaded;
    }

    /**
     * Resolves and validates the Gemini API key from the given properties.
     *
     * @param properties the loaded configuration properties
     * @return the non-blank API key
     * @throws GeminiConfigException if the key is missing or blank
     */
    private static String resolveApiKey(Properties properties) {
        String apiKey = properties.getProperty(API_KEY_PROPERTY);

        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiConfigException(
                    "Missing required property '" + API_KEY_PROPERTY + "' in " + CONFIG_FILE + ".");
        }

        return apiKey.trim();
    }

    /**
     * Resolves the Gemini API endpoint from the given properties, applying
     * the default endpoint when the property is absent or blank.
     *
     * @param properties the loaded configuration properties
     * @return the resolved endpoint URL
     */
    private static String resolveEndpoint(Properties properties) {
        String endpoint = properties.getProperty(ENDPOINT_PROPERTY);
        return (endpoint == null || endpoint.isBlank()) ? DEFAULT_ENDPOINT : endpoint.trim();
    }

    /**
     * Unchecked exception raised when Gemini configuration cannot be loaded
     * or is invalid, such as a missing properties file or a missing API key.
     * <p>
     * This is always a plain {@link RuntimeException}, never an {@link Error},
     * and never leaves this class in a permanently broken state: a later
     * call to {@link #getApiKey()} or {@link #getEndpoint()} will retry loading.
     */
    public static final class GeminiConfigException extends RuntimeException {

        /**
         * Creates a new exception with the given descriptive message.
         *
         * @param message human-readable description of the failure
         */
        public GeminiConfigException(String message) {
            super(message);
        }

        /**
         * Creates a new exception with the given descriptive message and root cause.
         *
         * @param message human-readable description of the failure
         * @param cause   the underlying cause of the failure
         */
        public GeminiConfigException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}