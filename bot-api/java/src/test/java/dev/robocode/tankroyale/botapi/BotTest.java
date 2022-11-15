package dev.robocode.tankroyale.botapi;


import jdk.jfr.Description;
import org.junit.jupiter.api.Disabled;
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
        awaitTick(bot);

        bot.setTurnRate(7.5);
        assertThat(bot.getTurnRate()).isEqualTo(7.5);

        awaitTick(bot);
        assertThat(bot.getTurnRate()).isEqualTo(7.5);
    }

    @Test
    @Description("setGunTurnRate()")
    void givenMockedServer_whenCallingSetGunTurnRate_thenGunTurnRateMustBeUpdatedToNewValue() {
        var bot = start();
        assertThat(bot.getGunTurnRate()).isZero();
        awaitTick(bot);

        bot.setGunTurnRate(17.25);
        assertThat(bot.getGunTurnRate()).isEqualTo(17.25);

        awaitTick(bot);
        assertThat(bot.getGunTurnRate()).isEqualTo(17.25);
    }

    @Test
    @Description("setRadarTurnRate()")
    void givenMockedServer_whenCallingSetRadarTurnRate_thenRadarTurnRateMustBeUpdatedToNewValue() {
        var bot = start();
        assertThat(bot.getRadarTurnRate()).isZero();
        awaitTick(bot);

        bot.setRadarTurnRate(32.125);
        assertThat(bot.getRadarTurnRate()).isEqualTo(32.125);

        awaitTick(bot);
        assertThat(bot.getRadarTurnRate()).isEqualTo(32.125);
    }

    @Test
    @Description("isRunning()")
    void givenMockedServer_whenCallingIsRunning_thenReturnTrueWhenBotIsRunning() {
        var bot = start();
        assertThat(bot.isRunning()).isFalse();

        awaitTick(bot);
        assertThat(bot.isRunning()).isTrue();
    }

    @Test
    @Description("setTargetSpeed()")
    void givenMockedServer_whenCallingSetTargetSpeed_thenTargetSpeedMustBeUpdatedToNewValue() {
        var bot = start();
        assertThat(bot.getTargetSpeed()).isZero();
        awaitTick(bot);

        bot.setTargetSpeed(7.25);
        assertThat(bot.getTargetSpeed()).isEqualTo(7.25);

        awaitTick(bot);
        assertThat(bot.getTargetSpeed()).isEqualTo(7.25);
    }

    @Test
    @Description("setForward()")
    void givenMockedServer_whenCallingSetForward_thenDistanceRemainingMustBeUpdatedToNewValue() throws InterruptedException {
        var bot = start();
        assertThat(bot.getSpeed()).isZero();
        awaitTick(bot);

        bot.setForward(100);
        assertThat(bot.getDistanceRemaining()).isEqualTo(100);
        var traveledDistance = bot.getSpeed();

        awaitDistanceRemainChanged(bot);

        assertThat(bot.getDistanceRemaining()).isEqualTo(100 - traveledDistance);
        traveledDistance += bot.getSpeed();

        awaitDistanceRemainChanged(bot);

        assertThat(bot.getDistanceRemaining()).isEqualTo(100 - traveledDistance);
    }

    @Test
    @Description("forward()")
    @Disabled
    void forward() {
        var bot = start();
        awaitTick(bot);

        new Thread(() -> {

            for (int i = 0; i < 19; i++) {
                awaitDistanceRemainChanged(bot);

                if (bot.getDistanceRemaining() < 10) {
                    server.setBotSpeed(0);
                }

                System.out.println(bot.getDistanceRemaining() + ", " + bot.getSpeed());
            }

        }).start();

        bot.forward(100);

        assertThat(bot.getDistanceRemaining()).isZero();
    }


    protected static Bot start() {
        var bot = new TestBot();
        startAsync(bot);
        return bot;
    }

    private void awaitDistanceRemainChanged(Bot bot) {
        awaitBotIntent();
        awaitTick(bot);

        double distanceRemain = bot.getDistanceRemaining();

        do {
            Thread.yield();
        } while (bot.getDistanceRemaining() == distanceRemain);
    }
}
