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

    @Test
    @Description("getGameType")
    void givenMockedServer_whenCallingGetGameType_thenGameTypeMustContainMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getGameType()).isEqualTo(MockedServer.GAME_TYPE);
    }

    @Test
    @Description("getArenaWidth")
    void givenMockedServer_whenCallingGetArenaWidth_thenArenaWidthMustBeEqualToMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getArenaWidth()).isEqualTo(MockedServer.ARENA_WIDTH);
    }

    @Test
    @Description("getArenaHeight")
    void givenMockedServer_whenCallingGetArenaHeight_thenArenaHeightMustBeEqualToMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getArenaHeight()).isEqualTo(MockedServer.ARENA_HEIGHT);
    }

    @Test
    @Description("getNumberOfRounds")
    void givenMockedServer_whenCallingGetNumberOfRounds_thenNumberOfRoundsMustBeEqualToMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getNumberOfRounds()).isEqualTo(MockedServer.NUMBER_OF_ROUNDS);
    }

    @Test
    @Description("getGunCoolingRate")
    void givenMockedServer_whenCallingGetGunCoolingRate_thenGunCoolingRateMustBeEqualToMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getGunCoolingRate()).isEqualTo(MockedServer.GUN_COOLING_RATE);
    }

    @Test
    @Description("getMaxInactivityTurns")
    void givenMockedServer_whenCallingMaxInactivityTurns_thenMaxInactivityTurnsMustBeEqualToMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getMaxInactivityTurns()).isEqualTo(MockedServer.MAX_INACTIVITY_TURNS);
    }

    @Test
    @Description("getTurnTimeout")
    void givenMockedServer_whenCallingGetTurnTimeout_thenTurnTimeoutMustBeEqualToMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getTurnTimeout()).isEqualTo(MockedServer.TURN_TIMEOUT);
    }

    @Test
    @Description("getTurnLeft")
    void givenMockedServer_whenCallingGetTurnLeft_thenTurnLeftMustBeLesserThanTurnTimeout() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getTimeLeft()).isBetween(0, MockedServer.TURN_TIMEOUT);
    }

    @Test
    @Description("getRoundNumber")
    void givenMockedServer_whenCallingGetRoundNumber_thenGetRoundNumberMustBe1() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getRoundNumber()).isEqualTo(1);
    }

    @Test
    @Description("getTurnNumber")
    void givenMockedServer_whenCallingGetTurnNumber_thenGetTurnNumberMustBe1() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getTurnNumber()).isEqualTo(2);
    }

    @Test
    @Description("getEnemyCount")
    void givenMockedServer_whenCallingGetEnemyCount_thenGetEnemyCountMustBeEqualToMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getEnemyCount()).isEqualTo(MockedServer.BOT_ENEMY_COUNT);
    }

    @Test
    @Description("getEnergy")
    void givenMockedServer_whenCallingGetEnergy_thenGetEnergyMustBeEqualToMockedValue() {
        var bot = startBot();
        go(bot);
        assertThat(server.awaitBotIntent(1000)).isTrue();
        assertThat(bot.getEnergy()).isEqualTo(MockedServer.BOT_ENERGY);
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
