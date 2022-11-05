package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;

import static dev.robocode.tankroyale.botapi.Constants.MAX_SPEED;
import static java.lang.Math.abs;

public final class BotInternals implements IStopResumeListener {

    private final Bot bot;
    private final BaseBotInternals baseBotInternals;

    private Thread thread;

    private double previousDirection;
    private double previousGunDirection;
    private double previousRadarDirection;

    private double distanceRemaining;
    private double turnRemaining;
    private double gunTurnRemaining;
    private double radarTurnRemaining;

    private boolean isOverDriving;

    private double savedPreviousDirection;
    private double savedPreviousGunDirection;
    private double savedPreviousRadarDirection;

    private double savedDistanceRemaining;
    private double savedTurnRemaining;
    private double savedGunTurnRemaining;
    private double savedRadarTurnRemaining;

    private final Object movementMonitor = new Object();
    private final Object turnMonitor = new Object();
    private final Object gunTurnMonitor = new Object();
    private final Object radarTurnMonitor = new Object();

    public BotInternals(Bot bot, BaseBotInternals baseBotInternals) {
        this.bot = bot;
        this.baseBotInternals = baseBotInternals;

        baseBotInternals.setStopResumeHandler(this);

        BotEventHandlers botEventHandlers = baseBotInternals.getBotEventHandlers();
        botEventHandlers.onGameAborted.subscribe(e -> onGameAborted(), 100);
        botEventHandlers.onNextTurn.subscribe(this::onNextTurn, 90);
        botEventHandlers.onRoundEnded.subscribe(e -> onRoundEnded(), 90);
        botEventHandlers.onGameEnded.subscribe(this::onGameEnded, 90);
        botEventHandlers.onDisconnected.subscribe(this::onDisconnected, 90);
        botEventHandlers.onHitWall.subscribe(e -> onHitWall(), 90);
        botEventHandlers.onHitBot.subscribe(this::onHitBot, 90);
        botEventHandlers.onDeath.subscribe(this::onDeath, 90);
    }

    private void onNextTurn(TickEvent e) {
        if (e.getTurnNumber() == 1) {
            onFirstTurn();
        }
        processTurn();
    }

    private void onFirstTurn() {
        stopThread(); // sanity before starting a new thread (later)
        clearRemaining();
        startThread();
    }

    private void clearRemaining() {
        setDistanceRemaining(0);
        setTurnRemaining(0);
        setGunTurnRemaining(0);
        setRadarTurnRemaining(0);

        previousDirection = bot.getDirection();
        previousGunDirection = bot.getGunDirection();
        previousRadarDirection = bot.getRadarDirection();
    }

    private void onGameAborted() {
        stopThread();
    }

    private void onRoundEnded() {
        stopThread();
    }

    private void onGameEnded(GameEndedEvent e) {
        stopThread();
    }

    private void onDisconnected(DisconnectedEvent e) {
        stopThread();
    }

    private void processTurn() {
        // No movement is possible, when the bot has become disabled
        if (bot.isDisabled()) {
            clearRemaining();
        } else {
            updateTurnRemaining();
            updateGunTurnRemaining();
            updateRadarTurnRemaining();
            updateMovement();
        }
    }

    private void startThread() {
        thread = new Thread(() -> {
            baseBotInternals.setRunning(true);
            try {
                baseBotInternals.enableEventHandling(true);
                bot.run();

                // Skip every turn after the run method has exited
                while (baseBotInternals.isRunning()) {
                    bot.go();
                }
            } finally {
                baseBotInternals.enableEventHandling(false); // prevent event queue max limit to be reached
            }
        });
        thread.start();
    }

    @SuppressWarnings("removal") // avoid waring in gradle build
    private void stopThread() {
        if (!isRunning())
            return;

        baseBotInternals.setRunning(false);

        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(100);
                if (thread.isAlive()) {
                    thread.stop();
                }
            } catch (InterruptedException ignore) {
            } finally {
                thread = null;
            }
        }
    }

    private void onHitWall() {
        setDistanceRemaining(0);
    }

    private void onHitBot(HitBotEvent e) {
        if (e.isRammed()) {
            setDistanceRemaining(0);
        }
    }

    private void onDeath(DeathEvent e) {
        stopThread();
    }

    public boolean isRunning() {
        return baseBotInternals.isRunning();
    }

    public void setTurnRate(double turnRate) {
        if (Double.isNaN(turnRate)) {
            throw new IllegalArgumentException("turnRate cannot be NaN");
        }
        baseBotInternals.getBotIntent().setTurnRate(turnRate);
        setTurnRemaining(toInfiniteValue(turnRate));
    }

    public void setGunTurnRate(double gunTurnRate) {
        if (Double.isNaN(gunTurnRate)) {
            throw new IllegalArgumentException("gunTurnRate cannot be NaN");
        }
        baseBotInternals.getBotIntent().setGunTurnRate(gunTurnRate);
        setGunTurnRemaining(toInfiniteValue(gunTurnRate));
    }

    public void setRadarTurnRate(double radarTurnRate) {
        if (Double.isNaN(radarTurnRate)) {
            throw new IllegalArgumentException("radarTurnRate cannot be NaN");
        }
        baseBotInternals.getBotIntent().setRadarTurnRate(radarTurnRate);
        setRadarTurnRemaining(toInfiniteValue(radarTurnRate));
    }

    private static double toInfiniteValue(double turnRate) {
        if (turnRate > 0) {
            return Double.POSITIVE_INFINITY;
        }
        if (turnRate < 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return 0;
    }

    private void setDistanceRemaining(double newDistanceRemaining) {
        synchronized (movementMonitor) {
            distanceRemaining = newDistanceRemaining;
        }
    }

    public double getDistanceRemaining() {
        synchronized (movementMonitor) {
            return distanceRemaining;
        }
    }

    private void setTurnRemaining(double newTurnRemaining) {
        synchronized (turnMonitor) {
            turnRemaining = newTurnRemaining;
        }
    }

    public double getTurnRemaining() {
        synchronized (turnMonitor) {
            return turnRemaining;
        }
    }

    private void setGunTurnRemaining(double newGunTurnRemaining) {
        synchronized (gunTurnMonitor) {
            gunTurnRemaining = newGunTurnRemaining;
        }
    }

    public double getGunTurnRemaining() {
        synchronized (gunTurnMonitor) {
            return gunTurnRemaining;
        }
    }

    private void setRadarTurnRemaining(double newRadarTurnRemaining) {
        synchronized (radarTurnMonitor) {
            radarTurnRemaining = newRadarTurnRemaining;
        }
    }

    public double getRadarTurnRemaining() {
        synchronized (radarTurnMonitor) {
            return radarTurnRemaining;
        }
    }

    public void setTargetSpeed(double targetSpeed) {
        if (Double.isNaN(targetSpeed)) {
            throw new IllegalArgumentException("targetSpeed cannot be NaN");
        }
        double distanceRemaining;
        if (targetSpeed > 0) {
            distanceRemaining = Double.POSITIVE_INFINITY;
        } else if (targetSpeed < 0) {
            distanceRemaining = Double.NEGATIVE_INFINITY;
        } else {
            distanceRemaining = 0;
        }
        setDistanceRemaining(distanceRemaining);

        baseBotInternals.getBotIntent().setTargetSpeed(targetSpeed);
    }

    public void setForward(double distance) {
        if (Double.isNaN(distance)) {
            throw new IllegalArgumentException("distance cannot be NaN");
        }
        double speed = baseBotInternals.getNewTargetSpeed(bot.getSpeed(), distance);
        baseBotInternals.getBotIntent().setTargetSpeed(speed);

        setDistanceRemaining(distance);
    }

    public void forward(double distance) {
        if (bot.isStopped()) {
            bot.go(); // skip turn by doing nothing in the turn
        } else {
            setForward(distance);
            do {
                bot.go();
            } while (isRunning() && (getDistanceRemaining() != 0 || bot.getSpeed() != 0));
        }
    }

    public void setTurnLeft(double degrees) {
        if (Double.isNaN(degrees)) {
            throw new IllegalArgumentException("degrees cannot be NaN");
        }
        setTurnRemaining(degrees);
        baseBotInternals.getBotIntent().setTurnRate(degrees);
    }

    public void turnLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go(); // skip turn by doing nothing in the turn
        } else {
            setTurnLeft(degrees);
            do {
                bot.go();
            } while (isRunning() && getTurnRemaining() != 0);
        }
    }

    public void setTurnGunLeft(double degrees) {
        if (Double.isNaN(degrees)) {
            throw new IllegalArgumentException("degrees cannot be NaN");
        }
        setGunTurnRemaining(degrees);
        baseBotInternals.getBotIntent().setGunTurnRate(degrees);
    }

    public void turnGunLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go(); // skip turn by doing nothing in the turn
        } else {
            setTurnGunLeft(degrees);
            do {
                bot.go();
            } while (isRunning() && getGunTurnRemaining() != 0);
        }
    }

    public void setTurnRadarLeft(double degrees) {
        if (Double.isNaN(degrees)) {
            throw new IllegalArgumentException("degrees cannot be NaN");
        }
        setRadarTurnRemaining(degrees);
        baseBotInternals.getBotIntent().setRadarTurnRate(degrees);
    }

    public void turnRadarLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go(); // skip turn by doing nothing in the turn
        } else {
            setTurnRadarLeft(degrees);
            do {
                bot.go();
            } while (isRunning() && getRadarTurnRemaining() != 0);
        }
    }

    public void fire(double firepower) {
        if (bot.setFire(firepower)) {
            bot.go();
        }
    }

    public void rescan() {
        baseBotInternals.setScannedBotEventInterruptible();
        bot.setRescan();
        bot.go();
    }

    public void waitFor(Condition condition) {
        do {
            bot.go();
        } while (isRunning() && !condition.test());
    }

    public void stop() {
        baseBotInternals.setStop();
        bot.go();
    }

    public void resume() {
        baseBotInternals.setResume();
        bot.go();
    }

    public void onStop() {
        savedPreviousDirection = previousDirection;
        savedPreviousGunDirection = previousGunDirection;
        savedPreviousRadarDirection = previousRadarDirection;

        savedDistanceRemaining = getDistanceRemaining();
        savedTurnRemaining = getTurnRemaining();
        savedGunTurnRemaining = getGunTurnRemaining();
        savedRadarTurnRemaining = getRadarTurnRemaining();
    }

    public void onResume() {
        previousDirection = savedPreviousDirection;
        previousGunDirection = savedPreviousGunDirection;
        previousRadarDirection = savedPreviousRadarDirection;

        setDistanceRemaining(savedDistanceRemaining);
        setTurnRemaining(savedTurnRemaining);
        setGunTurnRemaining(savedGunTurnRemaining);
        setRadarTurnRemaining(savedRadarTurnRemaining);
    }

    private void updateTurnRemaining() {
        synchronized (turnMonitor) {

            double delta = bot.calcDeltaAngle(bot.getDirection(), previousDirection);
            previousDirection = bot.getDirection();

            if (abs(turnRemaining) <= abs(delta)) {
                turnRemaining = 0;
            } else {
                turnRemaining -= delta;
                if (isNearZero(turnRemaining)) {
                    turnRemaining = 0;
                }
            }
            baseBotInternals.getBotIntent().setTurnRate(turnRemaining);
        }
    }

    private void updateGunTurnRemaining() {
        synchronized (gunTurnMonitor) {

            double delta = bot.calcDeltaAngle(bot.getGunDirection(), previousGunDirection);
            previousGunDirection = bot.getGunDirection();

            if (abs(gunTurnRemaining) <= abs(delta)) {
                gunTurnRemaining = 0;
            } else {
                gunTurnRemaining -= delta;
                if (isNearZero(gunTurnRemaining)) {
                    gunTurnRemaining = 0;
                }
            }
            baseBotInternals.getBotIntent().setGunTurnRate(gunTurnRemaining);
        }
    }

    private void updateRadarTurnRemaining() {
        synchronized (radarTurnMonitor) {

            double delta = bot.calcDeltaAngle(bot.getRadarDirection(), previousRadarDirection);
            previousRadarDirection = bot.getRadarDirection();

            if (abs(radarTurnRemaining) <= abs(delta)) {
                radarTurnRemaining = 0;
            } else {
                radarTurnRemaining -= delta;
                if (isNearZero(radarTurnRemaining)) {
                    radarTurnRemaining = 0;
                }
            }
            baseBotInternals.getBotIntent().setRadarTurnRate(radarTurnRemaining);
        }
    }

    private void updateMovement() {
        synchronized (movementMonitor) {

            if (Double.isInfinite(distanceRemaining)) {
                baseBotInternals.getBotIntent().setTargetSpeed(
                        (double) (distanceRemaining == Double.POSITIVE_INFINITY ? MAX_SPEED : -MAX_SPEED));

            } else {
                double distance = distanceRemaining;

                // This is Nat Pavasant's method described here:
                // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
                double newSpeed = baseBotInternals.getNewTargetSpeed(bot.getSpeed(), distance);
                baseBotInternals.getBotIntent().setTargetSpeed(newSpeed);

                // If we are over-driving our distance, and we are now at speed=0 then we stopped
                if (isNearZero(newSpeed) && isOverDriving) {
                    distance = 0;
                    isOverDriving = false;
                }

                // the overdrive flag
                if (Math.signum(distance * newSpeed) != -1) {
                    isOverDriving = baseBotInternals.getDistanceTraveledUntilStop(newSpeed) > abs(distance);
                }

                distanceRemaining = distance - newSpeed;
            }
        }
    }

    private boolean isNearZero(double value) {
        return (abs(value) < .00001);
    }
}
