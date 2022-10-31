using System;
using System.Collections.Generic;
using System.Threading;
using Fleck;
using Newtonsoft.Json;
using Robocode.TankRoyale.BotApi.Util;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Tests.Test_utils;

public class MockedServer
{
    public const int Port = 7913;

    public static string SessionId = "123abc";
    public static string Name = nameof(MockedServer);
    public static string Version = "1.0.0";
    public static string Variant = "Tank Royale";
    public static ISet<string> GameTypes = new HashSet<string> { "melee", "classic", "1v1" };
    public static int MyId = 1;
    public static string GameType = "classic";
    public static int ArenaWidth = 800;
    public static int ArenaHeight = 600;
    public static int NumberOfRounds = 10;
    public static double GunCoolingRate = 0.1;
    public static int MaxInactivityTurns = 450;
    public static int TurnTimeout = 30_000;
    public static int ReadyTimeout = 1_000_000;

    public static int BotEnemyCcount = 7;
    public static double BotEnergy = 99.7;
    public static double BotX = 44.5;
    public static double BotY = 721.34;
    public static double BotDirection = 120.1;
    public static double BotGunDirection = 3.45;
    public static double BotRadarDirection = 653.3;
    public static double BotRadarSweep = 13.5;
    public static double BotSpeed = 8.0;
    public static double BotTurnRate = 5.1;
    public static double BotGunTurnRate = 18.9;
    public static double BotRadarTurnRate = 34.1;
    public static double BotGunHeat = 7.6;

    private double _botEnergy = BotEnergy;
    private double _botGunHeat = BotGunHeat;

    private WebSocketServer _server;

    private BotHandshake _botHandshake;
    private BotIntent _botIntent;

    private int _turnNumber = 1;

    private readonly EventWaitHandle _openedEvent = new AutoResetEvent(false);
    private readonly EventWaitHandle _botHandshakeEvent = new AutoResetEvent(false);
    private readonly EventWaitHandle _gameStartedEvent = new AutoResetEvent(false);
    private readonly EventWaitHandle _tickEvent = new AutoResetEvent(false);
    private readonly EventWaitHandle _botIntentEvent = new AutoResetEvent(false);

    public static Uri ServerUrl => new($"ws://127.0.0.1:{Port}");

    public void Start()
    {
        _server = new WebSocketServer(ServerUrl.AbsoluteUri, false);
        _server.Start(conn =>
        {
            conn.OnOpen = () => OnOpen(conn);
            conn.OnMessage = message => OnMessage(conn, message);
            conn.OnError = OnError;
        });
    }

    public void Stop()
    {
        _server.Dispose();
        // TODO: Sleep for 100 millis?
    }

    public void SetBotEnergy(double botEnergy)
    {
        _botEnergy = botEnergy;
    }

    public void SetBotGunHeat(double botGunHeat)
    {
        _botGunHeat = botGunHeat;
    }

    public bool AwaitConnection(int milliSeconds)
    {
        try
        {
            return _openedEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitConnection: Exception occurred: " + ex);
        }

        return false;
    }

    public bool AwaitBotHandshake(int milliSeconds)
    {
        try
        {
            return _botHandshakeEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitBotHandshake: Exception occurred: " + ex);
        }

        return false;
    }

    public bool AwaitGameStarted(int milliSeconds)
    {
        try
        {
            return _gameStartedEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitGameStarted: Exception occurred: " + ex);
        }

        return false;
    }

    public bool AwaitTickEvent(int milliSeconds)
    {
        try
        {
            return _tickEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitTickEvent: Exception occurred: " + ex);
        }

        return false;
    }

    public bool AwaitBotIntent(int milliSeconds)
    {
        try
        {
            return _botIntentEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitBotIntentEvent: Exception occurred: " + ex);
        }

        return false;
    }

    public BotHandshake GetBotHandshake() => _botHandshake;


    private void OnOpen(IWebSocketConnection conn)
    {
        _openedEvent.Set();
        SendServerHandshake(conn);
    }

    private void OnMessage(IWebSocketConnection conn, string messageJson)
    {
        Console.WriteLine("OnMessage: " + messageJson);

        var message = JsonConvert.DeserializeObject<Message>(messageJson);
        if (message == null) return;

        var msgType = (MessageType)Enum.Parse(typeof(MessageType), message.Type);
        switch (msgType)
        {
            case MessageType.BotHandshake:
                _botHandshake = JsonConvert.DeserializeObject<BotHandshake>(messageJson);
                _botHandshakeEvent.Set();

                SendGameStartedForBot(conn);
                _gameStartedEvent.Set();
                break;

            case MessageType.BotReady:
                SendRoundStarted(conn);

                SendTickEventForBot(conn, _turnNumber++);
                _tickEvent.Set();
                break;

            case MessageType.BotIntent:
                _botIntentEvent.Set();

                _botIntent = JsonConvert.DeserializeObject<BotIntent>(messageJson);
                Thread.Sleep(5);

                SendTickEventForBot(conn, _turnNumber++);
                break;
        }
    }

    private static void OnError(Exception ex)
    {
        throw new InvalidOperationException("MockedServer error", ex);
    }

    private void SendServerHandshake(IWebSocketConnection conn)
    {
        var serverHandshake = new ServerHandshake
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.ServerHandshake),
            SessionId = SessionId,
            Name = Name,
            Version = Version,
            Variant = Variant,
            GameTypes = GameTypes
        };
        Send(conn, serverHandshake);
    }

    private void SendGameStartedForBot(IWebSocketConnection conn)
    {
        var gameStarted = new GameStartedEventForBot
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.GameStartedEventForBot),
            MyId = MyId
        };
        var gameSetup = new Schema.GameSetup
        {
            GameType = GameType,
            ArenaWidth = ArenaWidth,
            ArenaHeight = ArenaHeight,
            NumberOfRounds = NumberOfRounds,
            GunCoolingRate = GunCoolingRate,
            MaxInactivityTurns = MaxInactivityTurns,
            TurnTimeout = TurnTimeout,
            ReadyTimeout = ReadyTimeout
        };
        gameStarted.GameSetup = gameSetup;
        Send(conn, gameStarted);
    }

    private void SendRoundStarted(IWebSocketConnection conn)
    {
        var roundStarted = new RoundStartedEvent
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.RoundStartedEvent),
            RoundNumber = 1
        };
        Send(conn, roundStarted);
    }

    private void SendTickEventForBot(IWebSocketConnection conn, int turnNumber)
    {
        var tickEvent = new TickEventForBot()
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.TickEventForBot),
            RoundNumber = 1,
            TurnNumber = turnNumber,
            EnemyCount = BotEnemyCcount
        };

        var turnRate = BotTurnRate;
        var gunTurnRate = BotGunTurnRate;
        var radarTurnRate = BotRadarTurnRate;

        if (_botIntent != null)
        {
            turnRate = _botIntent.TurnRate ?? BotTurnRate;
            gunTurnRate = _botIntent.GunTurnRate ?? BotGunTurnRate;
            radarTurnRate = _botIntent.RadarTurnRate ?? BotRadarTurnRate;
        }

        var state = new Schema.BotState
        {
            Energy = _botEnergy,
            X = BotX,
            Y = BotY,
            Direction = BotDirection,
            GunDirection = BotGunDirection,
            RadarDirection = BotRadarDirection,
            RadarSweep = BotRadarSweep,
            Speed = BotSpeed,
            TurnRate = turnRate,
            GunTurnRate = gunTurnRate,
            RadarTurnRate = radarTurnRate,
            GunHeat = _botGunHeat
        };
        tickEvent.BotState = state;

        var bulletState1 = CreateBulletState(1);
        var bulletState2 = CreateBulletState(2);
        tickEvent.BulletStates = new HashSet<Schema.BulletState>
        {
            bulletState1, bulletState2
        };

        var scannedEvent = new ScannedBotEvent()
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.ScannedBotEvent),
            Direction = 45,
            X = 134.56,
            Y = 256.7,
            Energy = 56.9,
            Speed = 9.6,
            TurnNumber = 1,
            ScannedBotId = 2,
            ScannedByBotId = 1
        };
        tickEvent.Events = new HashSet<Event> { scannedEvent };

        Send(conn, tickEvent);
    }

    private void Send(IWebSocketConnection conn, Object obj)
    {
        conn.Send(JsonConvert.SerializeObject(obj));
    }

    private static Schema.BulletState CreateBulletState(int id)
    {
        var bulletState = new Schema.BulletState()
        {
            BulletId = id,
            X = 0,
            Y = 0,
            OwnerId = 0,
            Direction = 0,
            Power = 0
        };
        return bulletState;
    }
}