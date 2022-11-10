package dev.robocode.tankroyale.botapi;


import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import test_utils.MockedServer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BotTest extends AbstractBotTest {

    protected static class TestBot extends Bot {
        TestBot() {
            super(botInfo, MockedServer.getServerUrl());
        }
    }

    @Test
    @Description("setTurnRate()")
    void givenMockedServer_whenCallingSetTurnRate_thenTurnRateMustBeUpdatedToNewValue() {
        var bot = start();
        assertThat(bot.getTurnRate()).isZero();
        awaitTickEvent();

        bot.setTurnRate(7.5);
        assertThat(bot.getTurnRate()).isEqualTo(7.5);

        awaitTickEvent();
        assertThat(bot.getTurnRate()).isEqualTo(7.5);
    }

    @Test
    @Description("setGunTurnRate()")
    void givenMockedServer_whenCallingSetGunTurnRate_thenGunTurnRateMustBeUpdatedToNewValue() {
        var bot = start();
        assertThat(bot.getGunTurnRate()).isZero();
        awaitTickEvent();

        bot.setGunTurnRate(17.25);
        assertThat(bot.getGunTurnRate()).isEqualTo(17.25);

        awaitTickEvent();
        assertThat(bot.getGunTurnRate()).isEqualTo(17.25);
    }

    @Test
    @Description("setRadarTurnRate()")
    void givenMockedServer_whenCallingSetRadarTurnRate_thenRadarTurnRateMustBeUpdatedToNewValue() {
        var bot = start();
        assertThat(bot.getRadarTurnRate()).isZero();
        awaitTickEvent();

        bot.setRadarTurnRate(32.125);
        assertThat(bot.getRadarTurnRate()).isEqualTo(32.125);

        awaitTickEvent();
        assertThat(bot.getRadarTurnRate()).isEqualTo(32.125);
    }

    @Test
    @Description("isRunning()")
    void givenMockedServer_whenCallingIsRunning_thenReturnTrueWhenBotIsRunning() {
        var bot = start();
        assertThat(bot.isRunning()).isFalse();

        awaitTickEvent();
        assertThat(bot.isRunning()).isTrue();
    }

    @Test
    @Description("setTargetSpeed()")
    void givenMockedServer_whenCallingSetTargetSpeed_thenTargetSpeedMustBeUpdatedToNewValue() {
        var bot = start();
        assertThat(bot.getTargetSpeed()).isZero();
        awaitTickEvent();

        bot.setTargetSpeed(7.25);
        assertThat(bot.getTargetSpeed()).isEqualTo(7.25);

        awaitTickEvent();
        assertThat(bot.getTargetSpeed()).isEqualTo(7.25);
    }

    @Test
    @Description("setForward()")
    void givenMockedServer_whenCallingSetForward_thenForwardMustBeUpdatedToNewValue() {
        var bot = start();
        assertThat(bot.getSpeed()).isZero();
        awaitTickEvent();

        bot.setForward(100);
        assertThat(bot.getDistanceRemaining()).isEqualTo(100);
        var traveledDistance = bot.getSpeed();

        awaitBotIntent();
        awaitTickEvent();

        assertThat(bot.getDistanceRemaining()).isEqualTo(100 - traveledDistance);
        traveledDistance += bot.getSpeed();

        awaitBotIntent();
        awaitTickEvent();

        assertThat(bot.getDistanceRemaining()).isEqualTo(100 - traveledDistance);
    }

    protected static Bot start() {
        var bot = new TestBot();
        runAsync(bot);
        return bot;
    }
}
