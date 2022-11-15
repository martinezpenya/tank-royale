package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;
import jdk.jfr.Description;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import test_utils.MockedServer;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static dev.robocode.tankroyale.botapi.Constants.*;
import static dev.robocode.tankroyale.botapi.events.DefaultEventPriority.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// TODO: setRescan() in dept. Must call onScannedBot
// TODO: setFireAssist() in dept. Fire direction directly towards target
// TODO: setInterruptible() in dept, if possible?
// TODO: addCondition() check that condition is actually triggering an onCustomEvent + removeCondition stops it again
// TODO: setStop(), setResume(), isStopped() in dept

class BaseBotTest extends AbstractBotTest {

    @Test
    @Description("start()")
    void givenTestBot_whenCallingStart_thenBotConnectsToServer() {
        start();
        assertThat(server.awaitConnection(1000)).isTrue();
    }

    @Test
    @Description("go()")
    void givenTestBot_whenCallingGo_thenBotIntentIsReceivedAtServer() {
        var bot = startAndAwaitTick();
        goAsync(bot);
        awaitBotIntent();
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
        var bot = startAndAwaitGameStarted();
        assertThat(bot.getMyId()).isEqualTo(MockedServer.MY_ID);
    }

    @Test
    @Description("getGameType()")
    void givenMockedServer_whenCallingGetGameType_thenGameTypeMustContainMockedValue() {
        var bot = startAndAwaitGameStarted();
        assertThat(bot.getGameType()).isEqualTo(MockedServer.GAME_TYPE);
    }

    @Test
    @Description("getArenaWidth()")
    void givenMockedServer_whenCallingGetArenaWidth_thenArenaWidthIsEqualToMockedValue() {
        var bot = startAndAwaitGameStarted();
        assertThat(bot.getArenaWidth()).isEqualTo(MockedServer.ARENA_WIDTH);
    }

    @Test
    @Description("getArenaHeight()")
    void givenMockedServer_whenCallingGetArenaHeight_thenArenaHeightIsEqualToMockedValue() {
        var bot = startAndAwaitGameStarted();
        assertThat(bot.getArenaHeight()).isEqualTo(MockedServer.ARENA_HEIGHT);
    }

    @Test
    @Description("getNumberOfRounds()")
    void givenMockedServer_whenCallingGetNumberOfRounds_thenNumberOfRoundsIsEqualToMockedValue() {
        var bot = startAndAwaitGameStarted();
        assertThat(bot.getNumberOfRounds()).isEqualTo(MockedServer.NUMBER_OF_ROUNDS);
    }

    @Test
    @Description("getGunCoolingRate()")
    void givenMockedServer_whenCallingGetGunCoolingRate_thenGunCoolingRateIsEqualToMockedValue() {
        var bot = startAndAwaitGameStarted();
        assertThat(bot.getGunCoolingRate()).isEqualTo(MockedServer.GUN_COOLING_RATE);
    }

    @Test
    @Description("getMaxInactivityTurns()")
    void givenMockedServer_whenCallingGetMaxInactivityTurns_thenMaxInactivityTurnsIsEqualToMockedValue() {
        var bot = startAndAwaitGameStarted();
        assertThat(bot.getMaxInactivityTurns()).isEqualTo(MockedServer.MAX_INACTIVITY_TURNS);
    }

    @Test
    @Description("getTurnTimeout()")
    void givenMockedServer_whenCallingGetTurnTimeout_thenTurnTimeoutIsEqualToMockedValue() {
        var bot = startAndAwaitGameStarted();
        assertThat(bot.getTurnTimeout()).isEqualTo(MockedServer.TURN_TIMEOUT);
    }

    @Test
    @Description("getTimeLeft()")
    @Disabled("the test runs too slow, so the time left is returned as a negative value")
    void givenMockedServer_whenCallingGetTimeLeft_thenTimeLeftMustBeLesserThanTurnTimeout() {
        var bot = startAndAwaitGameStarted();

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getTimeLeft()).isBetween(0, MockedServer.TURN_TIMEOUT);
    }

    @Test
    @Description("getRoundNumber()")
    void givenMockedServer_whenCallingGetRoundNumber_thenRoundNumberMustBeTheFirst() {
        var bot = startAndAwaitTick();
        assertThat(bot.getRoundNumber()).isEqualTo(1);
    }

    @Test
    @Description("getTurnNumber()")
    void givenMockedServer_whenCallingGetTurnNumber_thenTurnNumberMustBeTheFirst() {
        var bot = startAndAwaitTick();
        assertThat(bot.getTurnNumber()).isEqualTo(1);
    }

    @Test
    @Description("getEnemyCount()")
    void givenMockedServer_whenCallingGetEnemyCount_thenEnemyCountIsEqualToMockedValue() {
        var bot = startAndAwaitTick();
        assertThat(bot.getEnemyCount()).isEqualTo(MockedServer.BOT_ENEMY_COUNT);
    }

    @Test
    @Description("getEnergy()")
    void givenMockedServer_whenCallingGetEnergy_thenEnergyIsEqualToMockedValue() {
        var bot = startAndAwaitTick();
        assertThat(bot.getEnergy()).isEqualTo(MockedServer.BOT_ENERGY);
    }

    @Test
    @Description("isDisabled() = false")
    void givenMockedServerAndSettingEnergyToNonZero_whenCallingIsDisabled_thenDisabledValueMustBeFalse() {
        var bot = start();
        server.setBotEnergy(0.1);
        awaitTick(bot);
        assertThat(bot.isDisabled()).isFalse();
    }

    @Test
    @Description("isDisabled() = true")
    void givenMockedServerAndSettingEnergyToZero_whenCallingIsDisabled_thenDisabledValueMustBeTrue() {
        var bot = start();
        server.setBotEnergy(0);
        awaitTick(bot);
        assertThat(bot.isDisabled()).isTrue();
    }

    @Test
    @Description("getX()")
    void givenMockedServer_whenCallingGetX_thenXIsEqualToMockedValue() {
        var bot = startAndAwaitTick();
        assertThat(bot.getX()).isEqualTo(MockedServer.BOT_X);
    }

    @Test
    @Description("getY()")
    void givenMockedServer_whenCallingGetY_thenYIsEqualToMockedValue() {
        var bot = startAndAwaitTick();
        assertThat(bot.getY()).isEqualTo(MockedServer.BOT_Y);
    }

    @Test
    @Description("getDirection()")
    void givenMockedServer_whenCallingGetDirection_thenDirectionIsEqualToMockedValue() {
        var bot = startAndAwaitTick();
        assertThat(bot.getDirection()).isEqualTo(MockedServer.BOT_DIRECTION);
    }

    @Test
    @Description("getGunDirection()")
    void givenMockedServer_whenCallingGetGunDirection_thenGunDirectionIsEqualToMockedValue() {
        var bot = startAndAwaitTick();
        assertThat(bot.getGunDirection()).isEqualTo(MockedServer.BOT_GUN_DIRECTION);
    }

    @Test
    @Description("getRadarDirection()")
    void givenMockedServer_whenCallingGetRadarDirection_thenRadarDirectionIsEqualToMockedValue() {
        var bot = startAndAwaitTick();
        assertThat(bot.getRadarDirection()).isEqualTo(MockedServer.BOT_RADAR_DIRECTION);
    }

    @Test
    @Description("getSpeed()")
    void givenMockedServer_whenCallingGetSpeed_thenSpeedIsEqualToMockedValue() {
        var bot = start();
        assertThat(bot.getSpeed()).isZero();

        awaitTick(bot);
        assertThat(bot.getSpeed()).isEqualTo(MockedServer.BOT_SPEED);
    }

    @Test
    @Description("getGunHeat()")
    void givenMockedServer_whenCallingGetGunHeat_thenGunHeatIsEqualToMockedValue() {
        var bot = start();
        assertThat(bot.getGunHeat()).isZero();

        awaitTick(bot);
        assertThat(bot.getGunHeat()).isEqualTo(MockedServer.BOT_GUN_HEAT);
    }

    @Test
    @Description("getBulletStates()")
    void givenMockedServer_whenCallingGetBulletStates_thenBulletStatesIsEqualToMockedValue() {
        var bot = start();
        assertThat(bot.getBulletStates().size()).isZero();

        awaitTick(bot);

        var bulletStates = bot.getBulletStates();
        assertThat(bulletStates).isNotNull();
        assertThat(bulletStates.size()).isEqualTo(2);
        assertThat(bulletStates.stream().anyMatch(bullet -> bullet.getBulletId() == 1)).isTrue();
        assertThat(bulletStates.stream().anyMatch(bullet -> bullet.getBulletId() == 2)).isTrue();
    }

    @Test
    @Description("getEvents()")
    void givenMockedServer_whenCallingGetEvents_thenEventsIsEqualToMockedValue() {
        var bot = start();
        assertThat(bot.getEvents().size()).isZero();

        awaitTick(bot);

        var events = bot.getEvents();
        assertThat(events).isNotNull();
        assertThat(events.size()).isEqualTo(2);
        assertThat(events.stream().anyMatch(event -> event instanceof TickEvent)).isTrue();
        assertThat(events.stream().anyMatch(event -> event instanceof ScannedBotEvent)).isTrue();
    }

    @Test
    @Description("clearEvents()")
    void givenMockedServer_whenCallingClearEvents_thenEventsMustBeEmpty() {
        var bot = start();

        bot.clearEvents();
        var events = bot.getEvents();
        assertThat(events).isNotNull();
        assertThat(events.size()).isEqualTo(0);
    }

    @Test
    @Description("getTurnRate()")
    void givenMockedServer_whenCallingGetTurnRate_thenTurnRateIsEqualToMockedValue() {
        var bot = start();
        assertThat(bot.getTurnRate()).isZero();

        awaitTick(bot);
        assertThat(bot.getTurnRate()).isEqualTo(MockedServer.BOT_TURN_RATE);
    }

    @Test
    @Description("setTurnRate() <= max turn rate")
    void givenMockedServer_whenCallingSetTurnRateLowerThanMax_thenTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndAwaitTick();
        bot.setTurnRate(7.25);
        assertThat(bot.getTurnRate()).isEqualTo(7.25);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getTurnRate()).isEqualTo(7.25);
    }

    @Test
    @Description("setTurnRate() > max turn rate")
    void givenMockedServer_whenCallingSetTurnRateLowerGreaterThanMax_thenTurnRateMustBeSetToMaxValue() {
        var bot = startAndAwaitTick();
        bot.setTurnRate(MAX_TURN_RATE);
        assertThat(bot.getTurnRate()).isEqualTo(MAX_TURN_RATE);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getTurnRate()).isEqualTo(MAX_TURN_RATE);
    }

    @Test
    @Description("getMaxTurnRate()")
    void givenMockedServer_whenCallingGetMaxTurnRate_thenMaxTurnRateMustBeEqualToDefaultValue() {
        var bot = start();
        assertThat(bot.getMaxTurnRate()).isEqualTo(MAX_TURN_RATE);
    }

    @Test
    @Description("setMaxTurnRate()")
    void givenMockedServer_whenCallingSetMaxTurnRate_thenMaxTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndAwaitTick();
        bot.setMaxTurnRate(5);
        bot.setTurnRate(7);

        assertThat(bot.getMaxTurnRate()).isEqualTo(5);
        assertThat(bot.getTurnRate()).isEqualTo(5);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getMaxTurnRate()).isEqualTo(5);
        assertThat(bot.getTurnRate()).isEqualTo(5);
    }

    @Test
    @Description("getGunTurnRate()")
    void givenMockedServer_whenCallingGetGunTurnRate_thenGunTurnRateIsEqualToMockedValue() {
        var bot = start();
        assertThat(bot.getGunTurnRate()).isZero();

        awaitTick(bot);
        assertThat(bot.getGunTurnRate()).isEqualTo(MockedServer.BOT_GUN_TURN_RATE);
    }

    @Test
    @Description("setGunTurnRate() <= max gun turn rate")
    void givenMockedServer_whenCallingSetGunTurnRateLowerThanMax_thenGunTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndAwaitTick();
        bot.setGunTurnRate(17.25);
        assertThat(bot.getGunTurnRate()).isEqualTo(17.25);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getGunTurnRate()).isEqualTo(17.25);
    }

    @Test
    @Description("setGunTurnRate() > max gun turn rate")
    void givenMockedServer_whenCallingSetGunTurnRateLowerGreaterThanMax_thenGunTurnRateMustBeSetToMaxValue() {
        var bot = startAndAwaitTick();
        bot.setGunTurnRate(MAX_GUN_TURN_RATE + 1);
        assertThat(bot.getGunTurnRate()).isEqualTo(MAX_GUN_TURN_RATE);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getGunTurnRate()).isEqualTo(MAX_GUN_TURN_RATE);
    }

    @Test
    @Description("getMaxTurnRate()")
    void givenMockedServer_whenCallingGetMaxGunTurnRate_thenMaxGunTurnRateMustBeEqualToDefaultValue() {
        var bot = start();
        assertThat(bot.getMaxGunTurnRate()).isEqualTo(MAX_GUN_TURN_RATE);
    }

    @Test
    @Description("setMaxTurnRate()")
    void givenMockedServer_whenCallingSetGunMaxTurnRate_thenMaxGunTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndAwaitTick();
        bot.setMaxGunTurnRate(15);
        bot.setGunTurnRate(17);

        assertThat(bot.getMaxGunTurnRate()).isEqualTo(15);
        assertThat(bot.getGunTurnRate()).isEqualTo(15);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getMaxGunTurnRate()).isEqualTo(15);
        assertThat(bot.getGunTurnRate()).isEqualTo(15);
    }

    @Test
    @Description("getRadarTurnRate()")
    void givenMockedServer_whenCallingGetRadarTurnRate_thenRadarTurnRateIsEqualToMockedValue() {
        var bot = start();
        assertThat(bot.getRadarTurnRate()).isZero();

        awaitTick(bot);
        assertThat(bot.getRadarTurnRate()).isEqualTo(MockedServer.BOT_RADAR_TURN_RATE);
    }

    @Test
    @Description("setRadarTurnRate() <= max radar turn rate")
    void givenMockedServer_whenCallingSetRadarTurnRateLowerThanMax_thenRadarTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndAwaitTick();
        bot.setRadarTurnRate(37.25);
        assertThat(bot.getRadarTurnRate()).isEqualTo(37.25);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getRadarTurnRate()).isEqualTo(37.25);
    }

    @Test
    @Description("setRadarTurnRate() > max radar turn rate")
    void givenMockedServer_whenCallingSetRadarTurnRateLowerGreaterThanMax_thenRadarTurnRateMustBeSetToMaxValue() {
        var bot = startAndAwaitTick();
        bot.setRadarTurnRate(MAX_RADAR_TURN_RATE + 1);
        assertThat(bot.getRadarTurnRate()).isEqualTo(MAX_RADAR_TURN_RATE);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getRadarTurnRate()).isEqualTo(MAX_RADAR_TURN_RATE);
    }

    @Test
    @Description("getMaxRadarTurnRate()")
    void givenMockedServer_whenCallingGetMaxRadarTurnRate_thenMaxRadarTurnRateMustBeEqualToDefaultValue() {
        var bot = start();
        assertThat(bot.getMaxRadarTurnRate()).isEqualTo(MAX_RADAR_TURN_RATE);
    }

    @Test
    @Description("setMaxRadarTurnRate()")
    void givenMockedServer_whenCallingSetMaxRadarTurnRate_thenMaxRadarTurnRateMustBeUpdatedToNewValue() {
        var bot = startAndAwaitTick();
        bot.setMaxRadarTurnRate(25);
        bot.setRadarTurnRate(27);

        assertThat(bot.getMaxRadarTurnRate()).isEqualTo(25);
        assertThat(bot.getRadarTurnRate()).isEqualTo(25);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getMaxRadarTurnRate()).isEqualTo(25);
        assertThat(bot.getRadarTurnRate()).isEqualTo(25);
    }

    @Test
    @Description("getTargetSpeed() and setTargetSpeed()")
    void givenMockedServer_whenCallingSetAndGetTargetSpeed_thenTargetSpeedMustBeSetValue() {
        var bot = startAndAwaitTick();
        bot.setTargetSpeed(5.75);
        assertThat(bot.getTargetSpeed()).isEqualTo(5.75);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getTargetSpeed()).isEqualTo(5.75);
    }


    @Test
    @Description("getMaxSpeed()")
    void givenMockedServer_whenCallingGetMaxSpeed_thenMaxSpeedMustBeEqualToDefaultValue() {
        var bot = start();
        assertThat(bot.getMaxSpeed()).isEqualTo(MAX_SPEED);
    }

    @Test
    @Description("setMaxSpeed()")
    void givenMockedServer_whenCallingSetMaxSpeed_thenMaxSpeedMustBeUpdatedToNewValue() {
        var bot = startAndAwaitTick();
        bot.setMaxSpeed(4);
        bot.setTargetSpeed(6);

        assertThat(bot.getMaxSpeed()).isEqualTo(4);
        assertThat(bot.getTargetSpeed()).isEqualTo(4);

        goAsync(bot);
        awaitBotIntent();

        assertThat(bot.getMaxSpeed()).isEqualTo(4);
        assertThat(bot.getTargetSpeed()).isEqualTo(4);
    }

    @Test
    @Description("setFire() when gunHeat > 0")
    void givenMockedServer_whenCallingSetFireAndGunHeatGreaterThanZero_thenReturnFalse() {
        server.setBotGunHeat(0.1);
        var bot = startAndAwaitTick();
        assertThat(bot.setFire(3)).isFalse();
    }

    @Test
    @Description("setFire() when gunHeat = 0")
    void givenMockedServer_whenCallingSetFireAndGunHeatIsZero_thenReturnTrue() {
        server.setBotGunHeat(0);
        var bot = startAndAwaitTick();
        assertThat(bot.setFire(3)).isTrue();
    }

    @Test
    @Description("getFirepower()")
    void givenMockedServer_whenCallingGetFirepowerAfterSetFire_thenReturnSameValueAsFired() {
        server.setBotGunHeat(0);
        var bot = startAndAwaitTick();
        bot.setFire(2.5);
        assertThat(bot.getFirepower()).isEqualTo(2.5);
    }

    @Test
    @Description("setAdjustGunForBodyTurn() and isAdjustGunForBodyTurn()")
    void givenMockedServer_whenCallingSetAndGetAdjustGunForBodyTurn_thenReturnSameValueAsSet() {
        var bot = start();

        bot.setAdjustGunForBodyTurn(false);
        assertThat(bot.isAdjustGunForBodyTurn()).isFalse();

        bot.setAdjustGunForBodyTurn(true);
        assertThat(bot.isAdjustGunForBodyTurn()).isTrue();
    }

    @Test
    @Description("setAdjustRadarForBodyTurn() and isAdjustRadarForBodyTurn()")
    void givenMockedServer_whenCallingSetAndGetAdjustRadarForBodyTurn_thenReturnSameValueAsSet() {
        var bot = start();

        bot.setAdjustRadarForBodyTurn(false);
        assertThat(bot.isAdjustRadarForBodyTurn()).isFalse();

        bot.setAdjustRadarForBodyTurn(true);
        assertThat(bot.isAdjustRadarForBodyTurn()).isTrue();
    }

    @Test
    @Description("setAdjustRadarForGunTurn() and isAdjustRadarForGunTurn()")
    void givenMockedServer_whenCallingSetAndGetAdjustRadarForGunTurn_thenReturnSameValueAsSet() {
        var bot = start();

        bot.setAdjustRadarForGunTurn(false);
        assertThat(bot.isAdjustRadarForGunTurn()).isFalse();

        bot.setAdjustRadarForGunTurn(true);
        assertThat(bot.isAdjustRadarForGunTurn()).isTrue();
    }

    @Test
    @Description("addCondition()")
    void givenMockedServer_whenCallingAddCondition_thenReturnTrueFirstTimeFalseNextTime() {
        var bot = start();

        var condition1 = new Condition();
        var condition2 = new Condition(() -> true);
        var condition3 = new Condition("cond1");
        var condition4 = new Condition("cond1", () -> true);

        assertThat(bot.addCustomEvent(condition1)).isTrue(); // not added previously
        assertThat(bot.addCustomEvent(condition2)).isTrue();
        assertThat(bot.addCustomEvent(condition3)).isTrue();
        assertThat(bot.addCustomEvent(condition4)).isTrue();

        assertThat(bot.addCustomEvent(condition1)).isFalse(); // already added
        assertThat(bot.addCustomEvent(condition2)).isFalse();
        assertThat(bot.addCustomEvent(condition3)).isFalse();
        assertThat(bot.addCustomEvent(condition4)).isFalse();
    }

    @Test
    @Description("removeCondition()")
    void givenMockedServerAndAddedCondition_whenCallingRemoveCondition_thenReturnTrueFirstTimeFalseNextTime() {
        var bot = start();

        var nonAddedCondition = new Condition();

        assertThat(bot.removeCustomEvent(nonAddedCondition)).isFalse();

        var addedCondition = new Condition();
        bot.addCustomEvent(addedCondition);

        assertThat(bot.removeCustomEvent(addedCondition)).isTrue();
        assertThat(bot.removeCustomEvent(addedCondition)).isFalse();
    }

    @Test
    @Description("setStop(), setResume(), isStopped()")
    void givenMockedServer_whenCallingSetStopAndSetResume_thenIsStoppedMustReflectStoppedStatus() {
        var bot = start();

        assertThat(bot.isStopped()).isFalse();

        bot.setStop();
        assertThat(bot.isStopped()).isTrue();

        bot.setResume();
        assertThat(bot.isStopped()).isFalse();
    }

    @Test
    @Description("getBodyColor() must return default color")
    void givenMockedServer_whenCallingGetBodyColor_thenNullIsReturned() {
        var bot = start();
        assertThat(bot.getBodyColor()).isNull();
    }

    @Test
    @Description("getTurretColor() must return default color")
    void givenMockedServer_whenCallingGetTurretColor_thenNullIsReturned() {
        var bot = start();
        assertThat(bot.getTurretColor()).isNull();
    }

    @Test
    @Description("getRadarColor() must return default color")
    void givenMockedServer_whenCallingGetRadarColor_thenNullIsReturned() {
        var bot = start();
        assertThat(bot.getRadarColor()).isNull();
    }

    @Test
    @Description("getBulletColor() must return default color")
    void givenMockedServer_whenCallingGetBulletColor_thenNullIsReturned() {
        var bot = start();
        assertThat(bot.getBulletColor()).isNull();
    }

    @Test
    @Description("getScanColor() must return default color")
    void givenMockedServer_whenCallingGetScanColor_thenNullIsReturned() {
        var bot = start();
        assertThat(bot.getScanColor()).isNull();
    }

    @Test
    @Description("getTracksColor() must return default color")
    void givenMockedServer_whenCallingGetTracksColor_thenNullIsReturned() {
        var bot = start();
        assertThat(bot.getTracksColor()).isNull();
    }

    @Test
    @Description("getGunColor() must return default color")
    void givenMockedServer_whenCallingGetGunColor_thenNullIsReturned() {
        var bot = start();
        assertThat(bot.getGunColor()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "-10,  4",
            "-8.1, 4",
            "-8,   4",
            "-6,   5.5",
            "-5.5, 5.875",
            "-5,   6.25",
            "-1,   9.25",
            "-0.1, 9.925",
            "0,    10",
            "0.1,  9.925",
            "2,    8.5",
            "4,    7",
            "8,    4",
            "8.1,  4",
            "20,   4"
    })
    @Description("calcMaxTurnRate()")
    void givenSpeed_whenCallingCalcMaxTurnRate_thenReturnCorrectMaxTurnRate(double speed, double maxTurnRate) {
        var bot = start();
        assertThat(bot.calcMaxTurnRate(speed)).isEqualTo(maxTurnRate);
    }

    @ParameterizedTest
    @CsvSource({
            "-1,  19.7",
            "0,   19.7",
            "0.1, 19.7",
            "0.2, 19.4",
            "1,   17",
            "2,   14",
            "2.5, 12.5",
            "3,   11",
            "3.1, 11",
            "10,  11"
    })
    @Description("calcBulletSpeed()")
    void givenFirepower_whenCallingCalcBulletSpeed_thenReturnCorrectBulletSpeed(double firepower, double bulletSpeed) {
        var bot = start();
        assertThat(bot.calcBulletSpeed(firepower)).isEqualTo(bulletSpeed);
    }

    @ParameterizedTest
    @CsvSource({
            "-1,  1.02",
            "0,   1.02",
            "0.1, 1.02",
            "0.2, 1.04",
            "1,   1.2",
            "2,   1.4",
            "3,   1.6",
            "3.1, 1.6",
            "10,  1.6"
    })
    @Description("calcGunHeat()")
    void givenFirepower_whenCallingCalcGunHeat_thenReturnCorrectGunHeat(double firepower, double gunHeat) {
        var bot = start();
        assertThat(bot.calcGunHeat(firepower)).isEqualTo(gunHeat);
    }

    @ParameterizedTest
    @Description("getEventPriority")
    @MethodSource("eventPrioritySource")
    void givenAllEventClass_whenCallingGetEventPriority_thenReturnCorrectPriorityValueForThatEventClass(Class<BotEvent> eventClass, int eventPriority) {
        var bot = start();
        assertThat(bot.getEventPriority(eventClass)).isEqualTo(eventPriority);
    }

    @ParameterizedTest
    @Description("setEventPriority")
    @MethodSource("eventPrioritySource")
    void givenAllEventClass_whenCallingSetEventPriority_thenReturnSameEventPriority(Class<BotEvent> eventClass) {
        var bot = start();
        int eventPriority = ThreadLocalRandom.current().nextInt(-1000, 1000);
        bot.setEventPriority(eventClass, eventPriority);
        assertThat(bot.getEventPriority(eventClass)).isEqualTo(eventPriority);
    }

    private static Stream<Arguments> eventPrioritySource() {
        return Stream.of(
                Arguments.of(WonRoundEvent.class, WON_ROUND),
                Arguments.of(SkippedTurnEvent.class, SKIPPED_TURN),
                Arguments.of(TickEvent.class, TICK),
                Arguments.of(CustomEvent.class, CUSTOM),
                Arguments.of(BotDeathEvent.class, BOT_DEATH),
                Arguments.of(BulletHitWallEvent.class, BULLET_HIT_WALL),
                Arguments.of(BulletHitBulletEvent.class, BULLET_HIT_BULLET),
                Arguments.of(BulletHitBotEvent.class, BULLET_HIT_BOT),
                Arguments.of(BulletFiredEvent.class, BULLET_FIRED),
                Arguments.of(HitByBulletEvent.class, HIT_BY_BULLET),
                Arguments.of(HitWallEvent.class, HIT_WALL),
                Arguments.of(HitBotEvent.class, HIT_BOT),
                Arguments.of(ScannedBotEvent.class, SCANNED_BOT),
                Arguments.of(DeathEvent.class, DEATH)
        );
    }
}
