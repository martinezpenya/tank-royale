package dev.robocode.tankroyale.botapi;

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
        startBot();
        assertThat(server.awaitConnection(1000)).isTrue();
    }

    @Test
    @Description("go()")
    void givenTestBot_whenCallingGo_thenBotIntentIsReceivedAtServer() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
    }

    @Test
    @Description("getVariant()")
    void givenMockedServer_whenCallingGetVariant_thenVariantMustContainMockedValue() {
        var bot = startBot();
        server.awaitBotHandshake(1000);
        assertThat(bot.getVariant()).isEqualTo(MockedServer.VARIANT);
    }

    @Test
    @Description("getVersion()")
    void givenMockedServer_whenCallingGetVersion_thenVersionMustContainMockedValue() {
        var bot = startBot();
        server.awaitBotHandshake(1000);
        assertThat(bot.getVersion()).isEqualTo(MockedServer.VERSION);
    }

    @Test
    @Description("getMyId()")
    void givenMockedServer_whenCallingGetMyId_thenMyIDMustContainMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getMyId()).isEqualTo(MockedServer.MY_ID);
    }

    private static BaseBot startBot() {
        var bot = new  TestBot();
        new Thread(bot::start).start();
        return bot;
    }

    private void go(BaseBot bot) {
        server.awaitBotHandshake(1000);
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignore) {}
            bot.go();
        }).start();
        assertThat(server.awaitBotIntent(1000)).isTrue();
    }
}
