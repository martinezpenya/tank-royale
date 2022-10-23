package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.TickEvent;
import jdk.jfr.Description;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test_utils.MockedServer;

import static dev.robocode.tankroyale.botapi.Constants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// TODO: setRescan()
// TODO: setFireAssist()
// TODO: setInterruptible()

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
        startAndGoAndAwaitBotIntent();
    }

    @Test
    @Description("getVariant()")
    void givenMockedServer_whenCallingGetVariant_thenVariantMustContainMockedValue() {
        var bot = startAndAwaitHandshake();
        assertThat(bot.getVariant()).isEqualTo(MockedServer.VARIANT);
    }

    @Test
    @Description("getVersion()")
    void givenMockedServer_whenCallingGetVersion_thenVersionMustContainMockedValue() {
        var bot = startAndAwaitHandshake();
        assertThat(bot.getVersion()).isEqualTo(MockedServer.VERSION);
    }

    @Test
    @Description("getMyId()")
    void givenMockedServer_whenCallingGetMyId_thenMyIdMustContainMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getMyId()).isEqualTo(MockedServer.MY_ID);
    }

    @Test
    @Description("getGameType()")
    void givenMockedServer_whenCallingGetGameType_thenGameTypeMustContainMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getGameType()).isEqualTo(MockedServer.GAME_TYPE);
    }

    @Test
    @Description("getArenaWidth()")
    void givenMockedServer_whenCallingGetArenaWidth_thenArenaWidthMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getArenaWidth()).isEqualTo(MockedServer.ARENA_WIDTH);
    }

    @Test
    @Description("getArenaHeight()")
    void givenMockedServer_whenCallingGetArenaHeight_thenArenaHeightMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getArenaHeight()).isEqualTo(MockedServer.ARENA_HEIGHT);
    }

    @Test
    @Description("getNumberOfRounds()")
    void givenMockedServer_whenCallingGetNumberOfRounds_thenNumberOfRoundsMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getNumberOfRounds()).isEqualTo(MockedServer.NUMBER_OF_ROUNDS);
    }

    @Test
    @Description("getGunCoolingRate()")
    void givenMockedServer_whenCallingGetGunCoolingRate_thenGunCoolingRateMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getGunCoolingRate()).isEqualTo(MockedServer.GUN_COOLING_RATE);
    }

    @Test
    @Description("getMaxInactivityTurns()")
    void givenMockedServer_whenCallingMaxInactivityTurns_thenMaxInactivityTurnsMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getMaxInactivityTurns()).isEqualTo(MockedServer.MAX_INACTIVITY_TURNS);
    }

    @Test
    @Description("getTurnTimeout()")
    void givenMockedServer_whenCallingGetTurnTimeout_thenTurnTimeoutMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getTurnTimeout()).isEqualTo(MockedServer.TURN_TIMEOUT);
    }

    @Test
    @Description("getTurnLeft()")
    @Disabled // FIXME
    void givenMockedServer_whenCallingGetTurnLeft_thenTurnLeftMustBeLesserThanTurnTimeout() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getTimeLeft()).isBetween(0, MockedServer.TURN_TIMEOUT);
    }

    @Test
    @Description("getRoundNumber()")
    void givenMockedServer_whenCallingGetRoundNumber_thenRoundNumberMustBe1() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getRoundNumber()).isEqualTo(1);
    }

    @Test
    @Description("getTurnNumber()")
    void givenMockedServer_whenCallingGetTurnNumber_thenTurnNumberMustBe1() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getTurnNumber()).isEqualTo(1);
    }

    @Test
    @Description("getEnemyCount()")
    void givenMockedServer_whenCallingGetEnemyCount_thenEnemyCountMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getEnemyCount()).isEqualTo(MockedServer.BOT_ENEMY_COUNT);
    }

    @Test
    @Description("getEnergy()")
    void givenMockedServer_whenCallingGetEnergy_thenEnergyMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getEnergy()).isEqualTo(MockedServer.BOT_ENERGY);
    }

    @Test
    @Description("isDisabled() is false")
    void givenMockedServerAndSettingEnergyToNonZero_whenCallingIsDisabled_thenDisabledValueMustBeFalse() {
        server.setBotEnergy(0.1);
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.isDisabled()).isFalse();
    }

    @Test
    @Description("isDisabled() is true")
    void givenMockedServerAndSettingEnergyToZero_whenCallingIsDisabled_thenDisabledValueMustBeTrue() {
        server.setBotEnergy(0.0);
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.isDisabled()).isTrue();
    }

    @Test
    @Description("getX()")
    void givenMockedServer_whenCallingGetX_thenXMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getX()).isEqualTo(MockedServer.BOT_X);
    }

    @Test
    @Description("getY()")
    void givenMockedServer_whenCallingGetY_thenYMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getY()).isEqualTo(MockedServer.BOT_Y);
    }

    @Test
    @Description("getDirection()")
    void givenMockedServer_whenCallingGetDirection_thenDirectionMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getDirection()).isEqualTo(MockedServer.BOT_DIRECTION);
    }

    @Test
    @Description("getGunDirection()")
    void givenMockedServer_whenCallingGetGunDirection_thenGunDirectionMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getGunDirection()).isEqualTo(MockedServer.BOT_GUN_DIRECTION);
    }

    @Test
    @Description("getRadarDirection()")
    void givenMockedServer_whenCallingGetRadarDirection_thenRadarDirectionMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getRadarDirection()).isEqualTo(MockedServer.BOT_RADAR_DIRECTION);
    }

    @Test
    @Description("getSpeed()")
    void givenMockedServer_whenCallingGetSpeed_thenSpeedMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getSpeed()).isEqualTo(MockedServer.BOT_SPEED);
    }

    @Test
    @Description("getGunHeat()")
    void givenMockedServer_whenCallingGetGunHeat_thenGunHeatMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getGunHeat()).isEqualTo(MockedServer.BOT_GUN_HEAT);
    }

    @Test
    @Description("getBulletStates()")
    void givenMockedServer_whenCallingGetBulletStates_thenBulletStatesMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();

        var bulletStates = bot.getBulletStates();
        assertThat(bulletStates).isNotNull();
        assertThat(bulletStates.size()).isEqualTo(2);
        assertThat(bulletStates.stream().anyMatch(bullet -> bullet.getBulletId() == 1)).isTrue();
        assertThat(bulletStates.stream().anyMatch(bullet -> bullet.getBulletId() == 2)).isTrue();
    }

    @Test
    @Description("getEvents()")
    void givenMockedServer_whenCallingGetEvents_thenEventsMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();

        var events = bot.getEvents();
        assertThat(events).isNotNull();
        assertThat(events.size()).isEqualTo(2);
        assertThat(events.stream().anyMatch(event -> event instanceof TickEvent)).isTrue();
        assertThat(events.stream().anyMatch(event -> event instanceof ScannedBotEvent)).isTrue();
    }

    @Test
    @Description("clearEvents()")
    void givenMockedServer_whenCallingClearEvents_thenEventsMustBeEmpty() {
        var bot = startAndGoAndAwaitBotIntent();

        bot.clearEvents();
        var events = bot.getEvents();
        assertThat(events).isNotNull();
        assertThat(events.size()).isEqualTo(0);
    }

    @Test
    @Description("getTurnRate()")
    void givenMockedServer_whenCallingGetTurnRate_thenTurnRateMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getTurnRate()).isEqualTo(MockedServer.BOT_TURN_RATE);
    }

    @Test
    @Description("setTurnRate() <= max turn rate")
    void givenMockedServer_whenCallingSetTurnRateLowerThanMax_thenTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setTurnRate(7.25);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getTurnRate()).isEqualTo(7.25);
    }

    @Test
    @Description("setTurnRate() > max turn rate")
    void givenMockedServer_whenCallingSetTurnRateLowerGreaterThanMax_thenTurnRateMustBeSetToMaxValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setTurnRate(MAX_TURN_RATE + 1.0);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getTurnRate()).isEqualTo(MAX_TURN_RATE);
    }

    @Test
    @Description("getMaxTurnRate()")
    void givenMockedServer_whenCallingGetMaxTurnRate_thenMaxTurnRateMustBeEqualToDefaultValue() {
        var bot = startBot();
        assertThat(bot.getMaxTurnRate()).isEqualTo(MAX_TURN_RATE);
    }

    @Test
    @Description("setMaxTurnRate()")
    void givenMockedServer_whenCallingSetMaxTurnRate_thenMaxTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setMaxTurnRate(5.0);
        bot.setTurnRate(7.0);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getMaxTurnRate()).isEqualTo(5.0);
        assertThat(bot.getTurnRate()).isEqualTo(5.0);
    }

    @Test
    @Description("getGunTurnRate()")
    void givenMockedServer_whenCallingGetGunTurnRate_thenGunTurnRateMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getGunTurnRate()).isEqualTo(MockedServer.BOT_GUN_TURN_RATE);
    }

    @Test
    @Description("setGunTurnRate() <= max gun turn rate")
    void givenMockedServer_whenCallingSetGunTurnRateLowerThanMax_thenGunTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setGunTurnRate(17.25);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getGunTurnRate()).isEqualTo(17.25);
    }

    @Test
    @Description("setGunTurnRate() > max gun turn rate")
    void givenMockedServer_whenCallingSetGunTurnRateLowerGreaterThanMax_thenGunTurnRateMustBeSetToMaxValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setGunTurnRate(MAX_GUN_TURN_RATE + 1.0);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getGunTurnRate()).isEqualTo(MAX_GUN_TURN_RATE);
    }

    @Test
    @Description("getMaxTurnRate()")
    void givenMockedServer_whenCallingGetMaxGunTurnRate_thenMaxGunTurnRateMustBeEqualToDefaultValue() {
        var bot = startBot();
        assertThat(bot.getMaxGunTurnRate()).isEqualTo(MAX_GUN_TURN_RATE);
    }

    @Test
    @Description("setMaxTurnRate()")
    void givenMockedServer_whenCallingSetGunMaxTurnRate_thenMaxGunTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setMaxGunTurnRate(15.0);
        bot.setGunTurnRate(17.0);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getMaxGunTurnRate()).isEqualTo(15.0);
        assertThat(bot.getGunTurnRate()).isEqualTo(15.0);
    }

    @Test
    @Description("getRadarTurnRate()")
    void givenMockedServer_whenCallingGetRadarTurnRate_thenRadarTurnRateMustBeEqualToMockedValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getRadarTurnRate()).isEqualTo(MockedServer.BOT_RADAR_TURN_RATE);
    }

    @Test
    @Description("setRadarTurnRate() <= max radar turn rate")
    void givenMockedServer_whenCallingSetRadarTurnRateLowerThanMax_thenRadarTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setRadarTurnRate(37.25);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getRadarTurnRate()).isEqualTo(37.25);
    }

    @Test
    @Description("setRadarTurnRate() > max radar turn rate")
    void givenMockedServer_whenCallingSetRadarTurnRateLowerGreaterThanMax_thenRadarTurnRateMustBeSetToMaxValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setRadarTurnRate(MAX_RADAR_TURN_RATE + 1.0);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getRadarTurnRate()).isEqualTo(MAX_RADAR_TURN_RATE);
    }

    @Test
    @Description("getMaxRadarTurnRate()")
    void givenMockedServer_whenCallingGetMaxRadarTurnRate_thenMaxRadarTurnRateMustBeEqualToDefaultValue() {
        var bot = startBot();
        assertThat(bot.getMaxRadarTurnRate()).isEqualTo(MAX_RADAR_TURN_RATE);
    }

    @Test
    @Description("setMaxRadarTurnRate()")
    void givenMockedServer_whenCallingSetMaxRadarTurnRate_thenMaxRadarTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setMaxRadarTurnRate(25.0);
        bot.setRadarTurnRate(27.0);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getMaxRadarTurnRate()).isEqualTo(25.0);
        assertThat(bot.getRadarTurnRate()).isEqualTo(25.0);
    }

    @Test
    @Description("getTargetSpeed() and setTargetSpeed()")
    void givenMockedServer_whenCallingSetAndGetTargetSpeed_thenTargetSpeedMustBeSetValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setTargetSpeed(5.75);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getTargetSpeed()).isEqualTo(5.75);
    }


    @Test
    @Description("getMaxSpeed()")
    void givenMockedServer_whenCallingGetMaxSpeed_thenMaxSpeedMustBeEqualToDefaultValue() {
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.getMaxSpeed()).isEqualTo(MAX_SPEED);
    }

    @Test
    @Description("setMaxSpeed()")
    void givenMockedServer_whenCallingSetMaxSpeed_thenMaxSpeedMustBeUpdatedToNewValue() {
        var bot = startAndGoAndAwaitBotIntent();
        bot.setMaxSpeed(4.0);
        bot.setTargetSpeed(6.0);
        bot.go();
        awaitBotIntent();
        assertThat(bot.getMaxSpeed()).isEqualTo(4.0);
        assertThat(bot.getTargetSpeed()).isEqualTo(4.0);
    }

    @Test
    @Description("setFire() when gunHeat > 0")
    void givenMockedServer_whenCallingSetFireAndGunHeatGreaterThanZero_thenReturnFalse() {
        server.setBotGunHeat(0.1);
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.setFire(3)).isFalse();
    }

    @Test
    @Description("setFire() when gunHeat is 0")
    void givenMockedServer_whenCallingSetFireAndGunHeatIsZero_thenReturnTrue() {
        server.setBotGunHeat(0);
        var bot = startAndGoAndAwaitBotIntent();
        assertThat(bot.setFire(3)).isTrue();
    }

    @Test
    @Description("getFirepower()")
    void givenMockedServer_whenCallingGetFirepowerAfterSetFire_thenReturnSameValueAsFired() {
        server.setBotGunHeat(0);
        var bot = startAndGoAndAwaitBotIntent();
        bot.setFire(3);
        assertThat(bot.getFirepower()).isEqualTo(3);
    }

    @Test
    @Description("setAdjustGunForBodyTurn() and isAdjustGunForBodyTurn()")
    void givenMockedServer_whenCallingSetAndGetAdjustGunForBodyTurn_thenReturnSameValueAsSet() {
        var bot = startBot();

        bot.setAdjustGunForBodyTurn(false);
        assertThat(bot.isAdjustGunForBodyTurn()).isFalse();

        bot.setAdjustGunForBodyTurn(true);
        assertThat(bot.isAdjustGunForBodyTurn()).isTrue();
    }

    @Test
    @Description("setAdjustRadarForBodyTurn() and isAdjustRadarForBodyTurn()")
    void givenMockedServer_whenCallingSetAndGetAdjustRadarForBodyTurn_thenReturnSameValueAsSet() {
        var bot = startBot();

        bot.setAdjustRadarForBodyTurn(false);
        assertThat(bot.isAdjustRadarForBodyTurn()).isFalse();

        bot.setAdjustRadarForBodyTurn(true);
        assertThat(bot.isAdjustRadarForBodyTurn()).isTrue();
    }

    @Test
    @Description("setAdjustRadarForGunTurn() and isAdjustRadarForGunTurn()")
    void givenMockedServer_whenCallingSetAndGetAdjustRadarForGunTurn_thenReturnSameValueAsSet() {
        var bot = startBot();

        bot.setAdjustRadarForGunTurn(false);
        assertThat(bot.isAdjustRadarForGunTurn()).isFalse();

        bot.setAdjustRadarForGunTurn(true);
        assertThat(bot.isAdjustRadarForGunTurn()).isTrue();
    }

    private static BaseBot startBot() {
        var bot = new  TestBot();
        new Thread(bot::start).start();
        return bot;
    }

    private static BaseBot startAndGo() {
        var bot = startBot();
        bot.go();
        return bot;
    }

    private BaseBot startAndAwaitHandshake() {
        var bot = startBot();
        server.awaitBotHandshake(1000);
        return bot;
    }

    private BaseBot startAndGoAndAwaitBotIntent() {
        var bot = startAndGo();
        awaitBotIntent();
        return bot;
    }

    private void awaitBotIntent() {
        server.awaitBotIntent(1000);
    }
}
