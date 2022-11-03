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
        runAsync(bot);
        return bot;
    }

    protected static void runAsync(BaseBot bot) {
        new Thread(bot::start).start();
    }

    protected BaseBot startAndAwaitHandshake() {
        var bot = start();
        awaitBotHandshake();
        return bot;
    }

    protected BaseBot startAndAwaitTickEvent() {
        var bot = start();
        awaitTickEvent();
        return bot;
    }

    protected BaseBot startAndAwaitGameStarted() {
        var bot = start();
        awaitGameStarted();
        return bot;
    }

    protected void awaitBotHandshake() {
        assertThat(server.awaitBotHandshake(500)).isTrue();
    }

    protected void awaitGameStarted() {
        sleep(); // must be processed within the bot api first
        assertThat(server.awaitGameStarted(500)).isTrue();
    }

    protected void awaitTickEvent() {
        sleep(); // must be processed within the bot api first
        assertThat(server.awaitTickEvent(500)).isTrue();
    }

    protected void awaitBotIntent() {
        assertThat(server.awaitBotIntent(1000)).isTrue();
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
