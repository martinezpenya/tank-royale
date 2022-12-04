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
        awaitCondition(bot::isRunning, 1000);
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
    void givenMockedServer_whenCallingSetForward_thenDistanceRemainingMustBeUpdatedToNewValue() {
        server.setSpeedIncrement(-2);
        server.setSpeedMinLimit(0);

        var bot = start();
        assertThat(bot.getSpeed()).isZero();
        awaitTick(bot);

        bot.setForward(100);
        assertThat(bot.getDistanceRemaining()).isEqualTo(100);

        assertThat(awaitDistanceRemainingChanged(bot)).isTrue();
        double traveledDistance = bot.getTargetSpeed();
        assertThat(bot.getDistanceRemaining()).isEqualTo(100 - traveledDistance);

        assertThat(awaitDistanceRemainingChanged(bot)).isTrue();
        traveledDistance += bot.getTargetSpeed();
        assertThat(bot.getDistanceRemaining()).isEqualTo(100 - traveledDistance);

        assertThat(awaitDistanceRemainingChanged(bot)).isTrue();
        traveledDistance += bot.getTargetSpeed();
        assertThat(bot.getDistanceRemaining()).isEqualTo(100 - traveledDistance);
    }

    @Test
    @Description("forward()")
    void givenMockedServer_whenCallingForward_thenDistanceRemainingMustEventuallyReachZero() {
        server.setSpeedIncrement(-2);
        server.setSpeedMinLimit(0);

        var bot = start();
        awaitTick(bot);

        new Thread(() -> {
            for (int i = 0; i <= 4; i++) {
                assertThat(awaitDistanceRemainingChanged(bot)).isTrue();
                sleep(5);
//                System.out.println(bot.getDistanceRemaining() + ", " + bot.getSpeed());
            }
        }).start();

        bot.forward(8 + 6 + 4 + 2);
        assertThat(bot.getDistanceRemaining()).isZero();
        assertThat(bot.getSpeed()).isZero();
    }

    @Test
    @Description("setBack()")
    void givenMockedServer_whenCallingSetBack_thenDistanceRemainingMustBeUpdatedToNewValue() {
        server.setSpeedIncrement(-2);
        server.setSpeedMinLimit(0);

        var bot = start();
        assertThat(bot.getSpeed()).isZero();
        awaitTick(bot);

        bot.setBack(100);

        assertThat(awaitDistanceRemainingChanged(bot)).isTrue();
        var traveledDistance = bot.getTargetSpeed();
        assertThat(bot.getDistanceRemaining()).isEqualTo(-100 - traveledDistance);

        assertThat(awaitDistanceRemainingChanged(bot)).isTrue();
        traveledDistance += bot.getTargetSpeed();
        assertThat(bot.getDistanceRemaining()).isEqualTo(-100 - traveledDistance);

        assertThat(awaitDistanceRemainingChanged(bot)).isTrue();
        traveledDistance += bot.getTargetSpeed();
        assertThat(bot.getDistanceRemaining()).isEqualTo(-100 - traveledDistance);
    }

    @Test
    @Description("back()")
    void givenMockedServer_whenCallingBack_thenDistanceRemainingMustEventuallyReachZero() {
        server.setSpeed(-8);
        server.setSpeedIncrement(1);
        server.setSpeedMaxLimit(0);

        var bot = start();
        awaitTick(bot);

        new Thread(() -> {
            for (int i = 0; i <= 8; i++) {
                awaitDistanceRemainingChanged(bot);
                sleep(5);
//                System.out.println(bot.getDistanceRemaining() + ", " + bot.getSpeed());
            }
        }).start();

        bot.back(8 + 7 + 6 + 5 + 4 + 3 + 2 + 1);
        assertThat(bot.getDistanceRemaining()).isZero();
        assertThat(bot.getSpeed()).isZero();
    }

    @Test
    @Description("getDistanceRemaining()")
    void givenTestBot_whenCallingGetDistanceRemaining_thenReturnTheDistanceRemainingJustSet() {
        var bot = start();

        bot.setForward(100);
        assertThat(bot.getDistanceRemaining()).isEqualTo(100);

        bot.setForward(-201);
        assertThat(bot.getDistanceRemaining()).isEqualTo(-201);
    }

    @Test
    @Description("setTurnLeft()")
    void givenTestBot_whenCallingSetTurnLeft_thenTurnRemainingMustBeEqualToTheSetValue() {
        var bot = start();

        bot.setTurnLeft(97);
        assertThat(bot.getTurnRemaining()).isEqualTo(97);

        bot.setTurnLeft(-45);
        assertThat(bot.getTurnRemaining()).isEqualTo(-45);
    }

    @Test
    @Description("turnLeft()")
    void givenTestBot_whenCallingTurnLeft_thenBotMustHaveTurnThisValue() {
        final int degreesToTurn = 7 * 10;
        server.setTurnIncrement(10);
        server.setDirectionMaxLimit(MockedServer.BOT_DIRECTION + degreesToTurn);

        var bot = start();
        awaitTick(bot);

        new Thread(() -> {
            for (int i = 0; i <= 7; i++) {
                awaitDirectionChanged(bot);
                sleep(5);
//                System.out.println(bot.getDirection() + ", " + bot.getTurnRate());
            }
        }).start();

        double startDirection = bot.getDirection();
        bot.turnLeft(degreesToTurn);

        assertThat(bot.getDirection()).isEqualTo(bot.normalizeAbsoluteAngle(startDirection + degreesToTurn));
        assertThat(bot.getTurnRemaining()).isZero();
    }

    @Test
    @Description("setTurnRight()")
    void givenTestBot_whenCallingSetTurnRight_thenTurnRemainingMustBeEqualToTheSetValue() {
        var bot = start();

        bot.setTurnRight(88);
        assertThat(bot.getTurnRemaining()).isEqualTo(-88);

        bot.setTurnRight(-44);
        assertThat(bot.getTurnRemaining()).isEqualTo(44);
    }

    @Test
    @Description("turnRight()")
    void givenTestBot_whenCallingTurnRight_thenBotMustHaveTurnedThisValue() {
        var bot = start();

        final int degreesToTurn = 6 * 8;
        server.setTurnIncrement(-8);
        server.setDirectionMinLimit(MockedServer.BOT_DIRECTION - degreesToTurn);

        awaitTick(bot);

        new Thread(() -> {
            for (int i = 0; i <= 6; i++) {
                awaitDirectionChanged(bot);
                sleep(5);
//                System.out.println(bot.getDirection() + ", " + bot.getTurnRate());
            }
        }).start();

        double startDirection = bot.getDirection();
        bot.turnRight(degreesToTurn);

        assertThat(bot.getDirection()).isEqualTo(startDirection - degreesToTurn);
        assertThat(bot.getTurnRemaining()).isZero();
    }

    @Test
    @Description("getTurnRemaining()")
    void givenTestBot_whenCallingGetTurnRemaining_thenReturnTheTurnRemainingJustSet() {
        var bot = start();

        bot.setTurnLeft(77);
        assertThat(bot.getTurnRemaining()).isEqualTo(77);

        bot.setTurnLeft(-124);
        assertThat(bot.getTurnRemaining()).isEqualTo(-124);
    }

    @Test
    @Description("setTurnGunLeft()")
    void givenTestBot_whenCallingSetTurnGunLeft_thenGunTurnRemainingMustBeEqualToTheSetValue() {
        var bot = start();

        bot.setTurnLeft(88);
        assertThat(bot.getTurnRemaining()).isEqualTo(88);

        bot.setTurnLeft(-53);
        assertThat(bot.getTurnRemaining()).isEqualTo(-53);
    }

    @Test
    @Description("turnGunLeft()")
    void givenTestBot_whenCallingTurnGunLeft_thenGunMustHaveTurnedThisValue() {
        final int degreesToTurn = 6 * 8;
        server.setGunTurnIncrement(8);
        server.setGunDirectionMaxLimit(MockedServer.BOT_GUN_DIRECTION + degreesToTurn);

        var bot = start();
        awaitTick(bot);

        new Thread(() -> {
            for (int i = 0; i <= 6; i++) {
                awaitGunDirectionChanged(bot);
                sleep(5);
//                System.out.println(bot.getGunDirection() + ", " + bot.getGunTurnRate());
            }
        }).start();

        double startDirection = bot.getGunDirection();
        bot.turnGunLeft(degreesToTurn);

        assertThat(bot.getGunDirection()).isEqualTo(bot.normalizeAbsoluteAngle(startDirection + degreesToTurn));
        assertThat(bot.getGunTurnRemaining()).isZero();
    }

    @Test
    @Description("setTurnGunRight()")
    void givenTestBot_whenCallingSetTurnGunRight_thenGunTurnRemainingMustBeEqualToTheSetValue() {
        var bot = start();

        bot.setTurnGunRight(52);
        assertThat(bot.getGunTurnRemaining()).isEqualTo(-52);

        bot.setTurnGunRight(-109);
        assertThat(bot.getGunTurnRemaining()).isEqualTo(109);
    }

    @Test
    @Description("turnGunRight()")
    void givenTestBot_whenCallingTurnGunRight_thenGunMustHaveTurnedThisValue() {
        var bot = start();

        final int degreesToTurn = 8 * 7;
        server.setGunTurnIncrement(-7);
        server.setGunDirectionMinLimit(MockedServer.BOT_GUN_DIRECTION - degreesToTurn);

        awaitTick(bot);

        new Thread(() -> {
            for (int i = 0; i <= 8; i++) {
                awaitGunDirectionChanged(bot);
                sleep(5);
//                System.out.println(bot.getGunDirection() + ", " + bot.getGunTurnRate());
            }
        }).start();

        double startDirection = bot.getGunDirection();
        bot.turnGunRight(degreesToTurn);

        assertThat(bot.getGunDirection()).isEqualTo(startDirection - degreesToTurn);
        assertThat(bot.getGunTurnRemaining()).isZero();
    }

    @Test
    @Description("getGunTurnRemaining()")
    void givenTestBot_whenCallingGetGunTurnRemaining_thenReturnTheGunTurnRemainingJustSet() {
        var bot = start();

        bot.setTurnGunLeft(77);
        assertThat(bot.getGunTurnRemaining()).isEqualTo(77);

        bot.setTurnGunLeft(-124);
        assertThat(bot.getGunTurnRemaining()).isEqualTo(-124);
    }

    @Test
    @Description("setTurnRadarLeft()")
    void givenTestBot_whenCallingSetTurnRadarLeft_thenRadarTurnRemainingMustBeEqualToTheSetValue() {
        var bot = start();

        bot.setTurnRadarLeft(106);
        assertThat(bot.getRadarTurnRemaining()).isEqualTo(106);

        bot.setTurnRadarLeft(-63);
        assertThat(bot.getRadarTurnRemaining()).isEqualTo(-63);
    }

    @Test
    @Description("turnRadarLeft()")
    void givenTestBot_whenCallingTurnRadarLeft_thenRadarMustHaveTurnedThisValue() {
        final int degreesToTurn = 5 * 7;
        server.setRadarTurnIncrement(7);
        server.setRadarDirectionMaxLimit(MockedServer.BOT_RADAR_DIRECTION + degreesToTurn);

        var bot = start();
        awaitTick(bot);

        new Thread(() -> {
            for (int i = 0; i <= 5; i++) {
                awaitRadarDirectionChanged(bot);
                sleep(5);
//                System.out.println(bot.getRadarDirection() + ", " + bot.getRadarTurnRate());
            }
        }).start();

        double startDirection = bot.getRadarDirection();
        bot.turnRadarLeft(degreesToTurn);

        assertThat(bot.getRadarDirection()).isEqualTo(bot.normalizeAbsoluteAngle(startDirection + degreesToTurn));
        assertThat(bot.getRadarTurnRemaining()).isZero();
    }

    @Test
    @Description("setTurnRadarRight()")
    void givenTestBot_whenCallingSetTurnRadarRight_thenRadarTurnRemainingMustBeEqualToTheSetValue() {
        var bot = start();

        bot.setTurnRadarRight(130);
        assertThat(bot.getRadarTurnRemaining()).isEqualTo(-130);

        bot.setTurnRadarRight(-21);
        assertThat(bot.getRadarTurnRemaining()).isEqualTo(21);
    }

    @Test
    @Description("turnRadarRight()")
    void givenTestBot_whenCallingTurnRadarRight_thenRadarMustHaveTurnedThisValue() {
        var bot = start();

        final int degreesToTurn = 9 * 7;
        server.setRadarTurnIncrement(-7);
        server.setRadarDirectionMinLimit(MockedServer.BOT_RADAR_DIRECTION - degreesToTurn);

        awaitTick(bot);

        new Thread(() -> {
            for (int i = 0; i <= 9; i++) {
                awaitRadarDirectionChanged(bot);
                sleep(5);
//                System.out.println(bot.getRadarDirection() + ", " + bot.getRadarTurnRate());
            }
        }).start();

        double startDirection = bot.getRadarDirection();
        bot.turnRadarRight(degreesToTurn);

        assertThat(bot.getRadarDirection()).isEqualTo(startDirection - degreesToTurn);
        assertThat(bot.getRadarTurnRemaining()).isZero();
    }

    @Test
    @Description("getRadarTurnRemaining()")
    void givenTestBot_whenCallingGetRadarTurnRemaining_thenReturnTheRadarTurnRemainingJustSet() {
        var bot = start();

        bot.setTurnRadarLeft(56);
        assertThat(bot.getRadarTurnRemaining()).isEqualTo(56);

        bot.setTurnRadarLeft(-123);
        assertThat(bot.getRadarTurnRemaining()).isEqualTo(-123);
    }

    @Test
    @Description("fire()")
    void testFire() {
        server.setGunHeat(0);

        var bot = start();
        awaitTick(bot);

        new Thread(() -> {
            awaitBotIntent();
            awaitTick(bot);
            awaitCondition(() -> bot.getFirepower() != 0, 1000);
            sleep(5);
        }).start();

        bot.fire(2.25);
        assertThat(bot.getFirepower()).isEqualTo(2.25);
    }

    protected static Bot start() {
        var bot = new TestBot();
        startAsync(bot);
        return bot;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean awaitDistanceRemainingChanged(Bot bot) {
        final double initialDistanceRemaining = bot.getDistanceRemaining();
        awaitBotIntent();
        awaitTick(bot);
        return awaitCondition(() -> initialDistanceRemaining != bot.getDistanceRemaining(), 1000);
    }

    private void awaitDirectionChanged(Bot bot) {
        final double initialDirection = bot.getDirection();
        awaitBotIntent();
        awaitTick(bot);
        awaitCondition(() -> initialDirection != bot.getDirection(), 1000);
   }

    private void awaitGunDirectionChanged(Bot bot) {
        final double initialGunDirection = bot.getGunDirection();
        awaitBotIntent();
        awaitTick(bot);
        awaitCondition(() -> initialGunDirection != bot.getGunDirection(), 1000);
    }

    private void awaitRadarDirectionChanged(Bot bot) {
        final double initialRadarDirection = bot.getRadarDirection();
        awaitBotIntent();
        awaitTick(bot);
        awaitCondition(() -> initialRadarDirection != bot.getRadarDirection(), 1000);
    }
}
