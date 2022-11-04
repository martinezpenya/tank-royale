package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.InitialPosition;

import java.util.*;

/**
 * Utility class for reading environment variables.
 */
final class EnvVars {

    // Hides constructor
    private EnvVars() {
    }

    /**
     * Name of environment variable for server URI.
     */
    static final String SERVER_URL = "SERVER_URL";
    /**
     * Name of environment variable for server URI.
     */
    static final String SERVER_SECRET = "SERVER_SECRET";
    /**
     * Name of environment variable for bot name.
     */
    static final String BOT_NAME = "BOT_NAME";
    /**
     * Name of environment variable for bot version.
     */
    static final String BOT_VERSION = "BOT_VERSION";
    /**
     * Name of environment variable for bot author(s).
     */
    static final String BOT_AUTHORS = "BOT_AUTHORS";
    /**
     * Name of environment variable for bot description.
     */
    static final String BOT_DESCRIPTION = "BOT_DESCRIPTION";
    /**
     * Name of environment variable for bot homepage URL.
     */
    static final String BOT_HOMEPAGE = "BOT_HOMEPAGE";
    /**
     * Name of environment variable for bot country code(s).
     */
    static final String BOT_COUNTRY_CODES = "BOT_COUNTRY_CODES";
    /**
     * Name of environment variable for bot game type(s).
     */
    static final String BOT_GAME_TYPES = "BOT_GAME_TYPES";
    /**
     * Name of environment variable for bot platform.
     */
    static final String BOT_PLATFORM = "BOT_PLATFORM";
    /**
     * Name of environment variable for bot programming language.
     */
    static final String BOT_PROG_LANG = "BOT_PROG_LANG";
    /**
     * Name of environment variable for bot initial position.
     */
    static final String BOT_INITIAL_POS = "BOT_INITIAL_POS";

    private static final String MISSING_ENV_VALUE = "Missing environment variable: ";

    /**
     * Bot Info
     */
    static BotInfo getBotInfo() {
        if (isBlank(getBotName())) {
            throw new BotException(MISSING_ENV_VALUE + BOT_NAME);
        }
        if (isBlank(getBotVersion())) {
            throw new BotException(MISSING_ENV_VALUE + BOT_VERSION);
        }
        if (isBlank(getBotAuthors())) {
            throw new BotException(MISSING_ENV_VALUE + BOT_AUTHORS);
        }
        return new BotInfo(
                getBotName(),
                getBotVersion(),
                getBotAuthors(),
                getBotDescription(),
                getBotHomepage(),
                getBotCountryCodes(),
                getBotGameTypes(),
                getBotPlatform(),
                getBotProgrammingLang(),
                getBotInitialPosition());
    }

    /**
     * Server URL
     */
    static String getServerUrl() {
        return System.getenv(SERVER_URL);
    }

    /**
     * Server secret
     */
    static String getServerSecret() {
        return System.getenv(SERVER_SECRET);
    }

    /**
     * Bot name
     */
    static String getBotName() {
        return System.getenv(BOT_NAME);
    }

    /**
     * Bot version
     */
    static String getBotVersion() {
        return System.getenv(BOT_VERSION);
    }

    /**
     * Bot author(s)
     */
    static List<String> getBotAuthors() {
        return propertyAsList(BOT_AUTHORS);
    }

    /**
     * Bot description
     */
    static String getBotDescription() {
        return System.getenv(BOT_DESCRIPTION);
    }

    /**
     * Bot homepage URL.
     */
    static String getBotHomepage() {
        return System.getenv(BOT_HOMEPAGE);
    }

    /**
     * Bot country code(s)
     */
    static List<String> getBotCountryCodes() {
        return propertyAsList(BOT_COUNTRY_CODES);
    }

    /**
     * Set of game type(s), which the bot supports
     */
    static Set<String> getBotGameTypes() {
        return new HashSet<>(propertyAsList(BOT_GAME_TYPES));
    }

    /**
     * Platform used for running the bot
     */
    static String getBotPlatform() {
        return System.getenv(BOT_PLATFORM);
    }

    /**
     * Language used for programming the bot
     */
    static String getBotProgrammingLang() {
        return System.getenv(BOT_PROG_LANG);
    }

    /**
     * Initial starting position used for debugging the bot
     */
    static InitialPosition getBotInitialPosition() {
        return InitialPosition.fromString(System.getenv(BOT_INITIAL_POS));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean isBlank(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    private static List<String> propertyAsList(String propertyName) {
        String value = System.getenv(propertyName);
        if (value == null || value.trim().length() == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split("\\s*,\\s*"));
    }
}
