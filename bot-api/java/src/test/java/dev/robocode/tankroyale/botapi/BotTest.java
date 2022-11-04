package dev.robocode.tankroyale.botapi;


import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.net.URI;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static test_utils.EnvironmentVariables.*;

@SetEnvironmentVariable(key = BOT_NAME, value = "TestBot")
@SetEnvironmentVariable(key = BOT_VERSION, value = "1.0")
@SetEnvironmentVariable(key = BOT_AUTHORS, value = "Author 1, Author 2")
class BotTest extends AbstractBotTest {

    static class TestBot extends Bot {

        TestBot() {
            super();
        }

        TestBot(BotInfo botInfo) {
            super(botInfo);
        }

        TestBot(BotInfo botInfo, URI serverUrl) {
            super(botInfo, serverUrl);
        }

        TestBot(BotInfo botInfo, URI serverUrl, String serverSecret) {
            super(botInfo, serverUrl, serverSecret);
        }
    }

    @Test
    void givenAllRequiredEnvVarsSet_whenCallingDefaultConstructor_thenBotIsCreated() {
        new TestBot();
        // passed when this point is reached
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    void givenMissingServerUrlEnvVar_whenCallingDefaultConstructor_thenBotIsCreated() {
        new TestBot();
        // passed when this point is reached
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_NAME)
    void givenMissingBotNameEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_NAME)).isTrue();
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_VERSION)
    void givenMissingBotVersionEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_VERSION)).isTrue();
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_AUTHORS)
    void givenMissingBotAuthorsEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_AUTHORS)).isTrue();
    }
}
