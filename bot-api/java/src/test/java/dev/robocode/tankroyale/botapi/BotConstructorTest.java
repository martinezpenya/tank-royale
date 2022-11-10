package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import test_utils.MockedServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static test_utils.EnvironmentVariables.*;

@SetEnvironmentVariable(key = SERVER_URL, value = "ws://localhost:" + MockedServer.PORT)
@SetEnvironmentVariable(key = BOT_NAME, value = "TestBot")
@SetEnvironmentVariable(key = BOT_VERSION, value = "1.0")
@SetEnvironmentVariable(key = BOT_AUTHORS, value = "Author 1, Author 2")
class BotConstructorTest extends AbstractBotTest {

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

    @Test
    void givenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotIsCreatedAndConnectingToServer() {
        startAndAwaitHandshake();
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    void givenMissingServerUrlEnvVar_callingDefaultConstructorFromThread_thenBotIsCreatedButNotConnectingToServer() {
        var bot = new TestBot();
        startAsync(bot);
        assertThat(server.awaitConnection(1000)).isFalse();
    }

    @Test
    void givenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotHandshakeMustBeCorrect() {
        startAndAwaitHandshake();
        var botHandshake = server.getBotHandshake();
        var env = System.getenv();

        assertThat(botHandshake).isNotNull();
        assertThat(botHandshake.getName()).isEqualTo(env.get(BOT_NAME));
        assertThat(botHandshake.getVersion()).isEqualTo(env.get(BOT_VERSION));
        assertThat(botHandshake.getAuthors()).containsAll(Arrays.asList(env.get(BOT_AUTHORS).split("\\s*,\\s*")));
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    @ClearEnvironmentVariable(key = BOT_NAME)
    @ClearEnvironmentVariable(key = BOT_VERSION)
    @ClearEnvironmentVariable(key = BOT_AUTHORS)
    void givenNoEnvVarsSet_callingDefaultConstructorWithBotInfoFromThread_thenBotHandshakeMustBeCorrect() {
        new TestBot(botInfo);
        // passed when this point is reached
    }

    @Test
    void givenServerUrlWithValidPortAsParameter_whenCallingConstructor_thenBotIsConnectingToServer() throws URISyntaxException {
        var bot = new TestBot(null, new URI("ws://localhost:" + MockedServer.PORT)); // valid port
        startAsync(bot);
        assertThat(server.awaitConnection(1000)).isTrue();
    }

    @Test
    void givenServerUrlWithInvalidPortAsParameter_whenCallingConstructor_thenBotIsNotConnectingToServer() throws URISyntaxException {
        var bot = new TestBot(null, new URI("ws://localhost:" + (MockedServer.PORT + 1))); // invalid port
        startAsync(bot);
        assertThat(server.awaitConnection(1000)).isFalse();
    }

    @Test
    void givenServerSecretConstructor_whenCallingConstructor_thenReturnedBotHandshakeContainsSecret() throws URISyntaxException {
        var secret = UUID.randomUUID().toString();
        var bot = new TestBot(null, new URI("ws://localhost:" + MockedServer.PORT), secret);
        startAsync(bot);
        awaitBotHandshake();
        var botHandshake = server.getBotHandshake();
        assertThat(botHandshake.getSecret()).isEqualTo(secret);
    }
}
