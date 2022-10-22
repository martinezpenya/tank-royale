package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.TickEvent;
import jdk.jfr.Description;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test_utils.MockedServer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class BaseBotTest {

    MockedServer server;

    static final BotInfo botInfo = BotInfo.builder()
            .setName("TestBot")
            .setVersion("1.0")
            .addAuthor("Author")
            .build();

    static class TestBot extends BaseBot {
        TestBot() {
            super(botInfo, MockedServer.getServerUrl());
        }

        @Override
        public void onTick(TickEvent e) {
            go();
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


    @Test
    @Description("start()")
    void givenTestBot_whenCallingStart_thenBotConnectsToServer() {
        new Thread(() -> new TestBot().start()).start();
        assertThat(server.awaitConnection(1000)).isTrue();
    }

    @Test
    @Description("go()")
    void givenTestBot_whenCallingGo_thenBotIntentIsReceivedAtServer() {
        var bot = new TestBot();
        new Thread(bot::start).start(); // waits for onClose
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignore) {}
            bot.go();
        }).start();
        assertThat(server.awaitBotIntent(1000)).isTrue();
    }
}
