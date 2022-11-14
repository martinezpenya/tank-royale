package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import test_utils.MockedServer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

abstract class AbstractBotTest {

    protected MockedServer server;

    protected static final BotInfo botInfo = BotInfo.builder()
            .setName("TestBot")
            .setVersion("1.0")
            .addAuthor("Author 1")
            .addAuthor("Author 2")
            .setDescription("Short description")
            .setHomepage("https://testbot.robocode.dev")
            .addCountryCode("gb")
            .addCountryCode("us")
            .addGameType("classic")
            .addGameType("melee")
            .addGameType("1v1")
            .setPlatform("JVM 19")
            .setProgrammingLang("Java 19")
            .setInitialPosition(InitialPosition.fromString("10, 20, 30"))
            .build();

    protected static class TestBot extends BaseBot {
        TestBot() {
            super(botInfo, MockedServer.getServerUrl());
        }
    }

    @BeforeEach
    void setUp() {
        server = new MockedServer();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    protected static BaseBot start() {
        var bot = new TestBot();
        startAsync(bot);
        return bot;
    }

    protected static void startAsync(BaseBot bot) {
        new Thread(bot::start).start();
    }

    protected static void goAsync(BaseBot bot) {
        new Thread(bot::go).start();
    }

    protected BaseBot startAndAwaitHandshake() {
        var bot = start();
        awaitBotHandshake();
        return bot;
    }

    protected BaseBot startAndAwaitTick() {
        var bot = start();
        awaitTick();
        return bot;
    }

    protected BaseBot startAndAwaitGameStarted() {
        var bot = start();
        awaitGameStarted(bot);
        return bot;
    }

    protected void awaitBotHandshake() {
        assertThat(server.awaitBotHandshake(1000)).isTrue();
    }

    protected void awaitGameStarted(BaseBot bot) {
        assertThat(server.awaitGameStarted(1000)).isTrue();

        boolean noException = false;
        do {
            try {
                bot.getEnergy();
                noException = true;
            } catch (BotException ex) {
                Thread.yield();
            }
        } while (!noException);
    }

    protected void awaitTick() {
        sleep(); // must be processed within the bot api first
        assertThat(server.awaitTick(1000)).isTrue();
    }

    protected void awaitBotIntent() {
        assertThat(server.awaitBotIntent(1000)).isTrue();
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected static boolean exceptionContainsEnvVarName(BotException botException, String envVarName) {
        return botException.getMessage().toUpperCase().contains(envVarName);
    }
}
