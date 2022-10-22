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
            System.out.println("onMessage: " + text);

            var message = gson.fromJson(text, Message.class);
            switch (message.getType()) {
                case BOT_HANDSHAKE:
                    botHandshake = gson.fromJson(text, BotHandshake.class);
                    botHandshakeLatch.countDown();
                    sendGameStartedForBot(conn);
                    break;

                case BOT_READY:
                    sendRoundStarted(conn);
                    sendTickEventForBot(conn);
                    break;

                case BOT_INTENT:
                    botIntentLatch.countDown();
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
            serverHandshake.setSessionId("123abc");
            serverHandshake.setName(MockedServer.class.getSimpleName());
            serverHandshake.setVersion("1.0.0");
            serverHandshake.setVariant("Tank Royale");
            serverHandshake.setGameTypes(Set.of("melee", "classic", "1v1"));
            send(conn, serverHandshake);
        }

        private void sendGameStartedForBot(WebSocket conn) {
            var gameStarted = new GameStartedEventForBot();
            gameStarted.setType(GAME_STARTED_EVENT_FOR_BOT);
            gameStarted.setMyId(1);
            var gameSetup = new GameSetup();
            gameSetup.setGameType("classic");
            gameSetup.setArenaWidth(800);
            gameSetup.setArenaHeight(600);
            gameSetup.setNumberOfRounds(10);
            gameSetup.setGunCoolingRate(0.1);
            gameSetup.setMaxInactivityTurns(450);
            gameSetup.setTurnTimeout(30_000);
            gameSetup.setReadyTimeout(1_000_000);
            gameStarted.setGameSetup(gameSetup);
            send(conn, gameStarted);
        }

        private void sendRoundStarted(WebSocket conn) {
            var roundStarted = new RoundStartedEvent();
            roundStarted.setType(ROUND_STARTED_EVENT);
            roundStarted.setRoundNumber(1);
            send(conn, roundStarted);
        }

        private void sendTickEventForBot(WebSocket conn) {
            var tickEvent = new TickEventForBot();
            tickEvent.setType(TICK_EVENT_FOR_BOT);
            tickEvent.setRoundNumber(1);
            tickEvent.setTurnNumber(1);
            tickEvent.setEnemyCount(1);
            var state = new BotState();
            state.setEnergy(100.0);
            state.setX(100.0);
            state.setY(100.0);
            state.setDirection(0.0);
            state.setGunDirection(0.0);
            state.setRadarDirection(0.0);
            state.setRadarSweep(0.0);
            state.setSpeed(0.0);
            state.setTurnRate(0.0);
            state.setTurnRate(0.0);
            state.setGunTurnRate(0.0);
            state.setRadarTurnRate(0.0);
            state.setGunHeat(0.0);
            tickEvent.setBotState(state);
            send(conn, tickEvent);
        }

        private void send(WebSocket conn, Message message) {
            conn.send(gson.toJson(message));
        }
    }
}