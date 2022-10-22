package test_utils;

import com.google.gson.Gson;
import dev.robocode.tankroyale.schema.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static dev.robocode.tankroyale.schema.Message.Type.*;

public final class MockedServer {

    public static final int PORT = 7913;

    public static String SESSION_ID = "123abc";
    public static String NAME = MockedServer.class.getSimpleName();
    public static String VERSION = "1.0.0";
    public static String VARIANT = "Tank Royale";
    public static Set<String> GAME_TYPES = Set.of("melee", "classic", "1v1");
    public static int MY_ID = 1;
    public static String GAME_TYPE = "classic";
    public static int ARENA_WIDTH = 800;
    public static int ARENA_HEIGHT = 600;
    public static int NUMBER_OF_ROUNDS = 10;
    public static double GUN_COOLING_RATE = 0.1;
    public static int MAX_INACTIVITY_TURNS = 450;
    public static int TURN_TIMEOUT = 30_000;
    public static int READY_TIMEOUT = 1_000_000;

    public static int BOT_ENEMY_COUNT = 7;
    public static double BOT_ENERGY = 99.7;
    public static double BOT_X = 44.5;
    public static double BOT_Y = 721.34;
    public static double BOT_DIRECTION = 120.1;
    public static double BOT_GUN_DIRECTION = 3.45;
    public static double BOT_RADAR_DIRECTION = 653.3;
    public static double BOT_RADAR_SWEEP = 13.5;
    public static double BOT_SPEED = 8.0;
    public static double BOT_TURN_RATE = 5.1;
    public static double BOT_GUN_TURN_RATE = 18.9;
    public static double BOT_RADAR_TURN_RATE = 34.1;
    public static double BOT_GUN_HEAT = 7.6;

    private WebSocketServerImpl server = new WebSocketServerImpl();

    private BotHandshake botHandshake;

    private CountDownLatch openedLatch = new CountDownLatch(1);
    private CountDownLatch botHandshakeLatch = new CountDownLatch(1);
    private CountDownLatch botIntentLatch = new CountDownLatch(1);

    private Gson gson;

    public static URI getServerUrl() {
        try {
            return new URI("ws://localhost:" + PORT);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void start() {
        init();
        server.start();
    }

    public void stop() {
        try {
            server.stop();
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        openedLatch = new CountDownLatch(1);
        botHandshakeLatch = new CountDownLatch(1);
        botIntentLatch = new CountDownLatch(1);

        gson = new Gson();

        server = new WebSocketServerImpl();
    }

    public boolean awaitConnection(int milliSeconds) {
        try {
            return openedLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("awaitConnection() was interrupted");
        }
        return false;
    }

    public boolean awaitBotHandshake(int milliSeconds) {
        try {
            return botHandshakeLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("awaitBotHandshake() was interrupted");
        }
        return false;
    }

    public boolean awaitBotIntent(int milliSeconds) {
        try {
            return botIntentLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("awaitBotIntent() was interrupted");
        }
        return false;
    }


    public BotHandshake getBotHandshake() {
        return botHandshake;
    }

    private class WebSocketServerImpl extends WebSocketServer {

        public WebSocketServerImpl() {
            super(new InetSocketAddress(PORT));
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            openedLatch.countDown();
            sendServerHandshake(conn);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        }

        @Override
        public void onMessage(WebSocket conn, String text) {
//            System.out.println("onMessage: " + text);

            var message = gson.fromJson(text, Message.class);
            switch (message.getType()) {
                case BOT_HANDSHAKE:
                    botHandshake = gson.fromJson(text, BotHandshake.class);
                    botHandshakeLatch.countDown();
                    sendGameStartedForBot(conn);
                    break;

                case BOT_READY:
                    sendRoundStarted(conn);
                    sendTickEventForBot(conn, 1);
                    break;

                case BOT_INTENT:
                    botIntentLatch.countDown();
                    try { Thread.sleep(10); } catch (InterruptedException ignore) {}
                    sendTickEventForBot(conn, 2);
                    break;
            }
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            throw new IllegalStateException("MockedServer error", ex);
        }

        private void sendServerHandshake(WebSocket conn) {
            var serverHandshake = new ServerHandshake();
            serverHandshake.setType(SERVER_HANDSHAKE);
            serverHandshake.setSessionId(SESSION_ID);
            serverHandshake.setName(NAME);
            serverHandshake.setVersion(VERSION);
            serverHandshake.setVariant(VARIANT);
            serverHandshake.setGameTypes(GAME_TYPES);
            send(conn, serverHandshake);
        }

        private void sendGameStartedForBot(WebSocket conn) {
            var gameStarted = new GameStartedEventForBot();
            gameStarted.setType(GAME_STARTED_EVENT_FOR_BOT);
            gameStarted.setMyId(MY_ID);
            var gameSetup = new GameSetup();
            gameSetup.setGameType("classic");
            gameSetup.setArenaWidth(ARENA_WIDTH);
            gameSetup.setArenaHeight(ARENA_HEIGHT);
            gameSetup.setNumberOfRounds(NUMBER_OF_ROUNDS);
            gameSetup.setGunCoolingRate(GUN_COOLING_RATE);
            gameSetup.setMaxInactivityTurns(MAX_INACTIVITY_TURNS);
            gameSetup.setTurnTimeout(TURN_TIMEOUT);
            gameSetup.setReadyTimeout(READY_TIMEOUT);
            gameStarted.setGameSetup(gameSetup);
            send(conn, gameStarted);
        }

        private void sendRoundStarted(WebSocket conn) {
            var roundStarted = new RoundStartedEvent();
            roundStarted.setType(ROUND_STARTED_EVENT);
            roundStarted.setRoundNumber(1);
            send(conn, roundStarted);
        }

        private void sendTickEventForBot(WebSocket conn, int turnNumber) {
            var tickEvent = new TickEventForBot();
            tickEvent.setType(TICK_EVENT_FOR_BOT);
            tickEvent.setRoundNumber(1);
            tickEvent.setTurnNumber(turnNumber);
            tickEvent.setEnemyCount(BOT_ENEMY_COUNT);
            var state = new BotState();
            state.setEnergy(BOT_ENERGY);
            state.setX(BOT_X);
            state.setY(BOT_Y);
            state.setDirection(BOT_DIRECTION);
            state.setGunDirection(BOT_GUN_DIRECTION);
            state.setRadarDirection(BOT_RADAR_DIRECTION);
            state.setRadarSweep(BOT_RADAR_SWEEP);
            state.setSpeed(BOT_SPEED);
            state.setTurnRate(BOT_TURN_RATE);
            state.setGunTurnRate(BOT_GUN_TURN_RATE);
            state.setRadarTurnRate(BOT_RADAR_TURN_RATE);
            state.setGunHeat(BOT_GUN_HEAT);
            tickEvent.setBotState(state);
            send(conn, tickEvent);
        }

        private void send(WebSocket conn, Message message) {
            conn.send(gson.toJson(message));
        }
    }
}