using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Threading;
using System.Web;
using Robocode.TankRoyale.BotApi.Mapper;
using Robocode.TankRoyale.BotApi.Util;
using static Robocode.TankRoyale.BotApi.Events.DefaultEventPriority;
using static System.Double;

namespace Robocode.TankRoyale.BotApi.Internal;

public sealed class BaseBotInternals
{
    private const string DefaultServerUrl = "ws://localhost:7654";

    private const string NotConnectedToServerMsg =
        "Not connected to a game server. Make sure OnConnected() event handler has been called first";

    private const string GameNotRunningMsg =
        "Game is not running. Make sure OnGameStarted() event handler has been called first";

    private const string TickNotAvailableMsg =
        "Game is not running or tick has not occurred yet. Make sure OnTick() event handler has been called first";

    private readonly string serverSecret;
    private WebSocketClient socket;
    private Schema.ServerHandshake serverHandshake;
    private readonly EventWaitHandle closedEvent = new ManualResetEvent(false);

    private readonly IBaseBot baseBot;
    private readonly BotInfo botInfo;

    private int myId;
    private GameSetup gameSetup;

    private Events.TickEvent tickEvent;
    private long? ticksStart;

    private readonly EventQueue eventQueue;

    private readonly object nextTurnMonitor = new();

    private bool isRunning;
    private readonly object isRunningLock = new();

    private IStopResumeListener stopResumeListener;

    private double maxSpeed;
    private double maxTurnRate;
    private double maxGunTurnRate;
    private double maxRadarTurnRate;

    private double? savedTargetSpeed;
    private double? savedTurnRate;
    private double? savedGunTurnRate;
    private double? savedRadarTurnRate;

    private readonly double absDeceleration;

    private bool eventHandlingDisabled;

    private readonly StringWriter stdOutStringWriter = new();
    private readonly StringWriter stdErrStringWriter = new();

    private readonly IDictionary<Type, int> eventPriorities = new Dictionary<Type, int>();

    internal BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, Uri serverUrl, string serverSecret)
    {
        this.baseBot = baseBot;
        this.botInfo = botInfo ?? EnvVars.GetBotInfo();

        BotEventHandlers = new BotEventHandlers(baseBot);
        eventQueue = new EventQueue(this, BotEventHandlers);

        absDeceleration = Math.Abs(Constants.Deceleration);

        maxSpeed = Constants.MaxSpeed;
        maxTurnRate = Constants.MaxTurnRate;
        maxGunTurnRate = Constants.MaxGunTurnRate;
        maxRadarTurnRate = Constants.MaxRadarTurnRate;

        this.serverSecret = serverSecret ?? ServerSecretFromSetting;

        Init(serverUrl ?? ServerUrlFromSetting);
    }

    private void Init(Uri serverUrl)
    {
        RedirectStdOutAndStdErr();
        InitializeWebSocketClient(serverUrl);
        InitializeEventPriorities();
        SubscribeToEvents();
    }

    private void RedirectStdOutAndStdErr()
    {
        if (!EnvVars.IsBotBooted()) return;
        Console.SetOut(stdOutStringWriter);
        Console.SetError(stdErrStringWriter);
    }

    private void InitializeWebSocketClient(Uri serverUrl)
    {
        socket = new WebSocketClient(serverUrl);
        socket.OnConnected += HandleConnected;
        socket.OnDisconnected += HandleDisconnected;
        socket.OnError += HandleConnectionError;
        socket.OnTextMessage += HandleTextMessage;
    }

    private void InitializeEventPriorities()
    {
        eventPriorities[typeof(Events.TickEvent)] = Tick;
        eventPriorities[typeof(Events.WonRoundEvent)] = WonRound;
        eventPriorities[typeof(Events.SkippedTurnEvent)] = SkippedTurn;
        eventPriorities[typeof(Events.CustomEvent)] = Custom;
        eventPriorities[typeof(Events.BotDeathEvent)] = BotDeath;
        eventPriorities[typeof(Events.BulletFiredEvent)] = BulletFired;
        eventPriorities[typeof(Events.BulletHitWallEvent)] = BulletHitWall;
        eventPriorities[typeof(Events.BulletHitBulletEvent)] = BulletHitBullet;
        eventPriorities[typeof(Events.BulletHitBotEvent)] = BulletHitBot;
        eventPriorities[typeof(Events.HitByBulletEvent)] = HitByBullet;
        eventPriorities[typeof(Events.HitWallEvent)] = HitWall;
        eventPriorities[typeof(Events.HitBotEvent)] = HitBot;
        eventPriorities[typeof(Events.ScannedBotEvent)] = ScannedBot;
        eventPriorities[typeof(Events.DeathEvent)] = Death;
    }

    private void SubscribeToEvents()
    {
        BotEventHandlers.OnRoundStarted.Subscribe(OnRoundStarted, 100);
        BotEventHandlers.OnNextTurn.Subscribe(OnNextTurn, 100);
        BotEventHandlers.OnBulletFired.Subscribe(OnBulletFired, 100);
    }

    public bool IsRunning
    {
        get
        {
            lock (isRunningLock)
            {
                return isRunning;
            }
        }
        set
        {
            lock (isRunningLock)
            {
                isRunning = value;
            }
        }
    }

    public void EnableEventHandling(bool enable)
    {
        eventHandlingDisabled = !enable;
    }

    public void SetStopResumeHandler(IStopResumeListener listener)
    {
        stopResumeListener = listener;
    }

    private static Schema.BotIntent NewBotIntent()
    {
        var botIntent = new Schema.BotIntent
        {
            Type = EnumUtil.GetEnumMemberAttrValue(Schema.MessageType.BotIntent) // must be set
        };
        return botIntent;
    }

    private void ResetMovement()
    {
        BotIntent.TurnRate = null;
        BotIntent.GunTurnRate = null;
        BotIntent.RadarTurnRate = null;
        BotIntent.TargetSpeed = null;
        BotIntent.Firepower = null;
    }

    internal BotEventHandlers BotEventHandlers { get; }

    internal IList<Events.BotEvent> Events => eventQueue.Events;

    internal void ClearEvents()
    {
        eventQueue.ClearEvents();
    }

    internal void SetInterruptible(bool interruptible)
    {
        eventQueue.SetInterruptible(interruptible);
    }

    internal void SetScannedBotEventInterruptible()
    {
        eventQueue.SetInterruptible(typeof(Events.ScannedBotEvent), true);
    }

    internal ISet<Events.Condition> Conditions { get; } = new HashSet<Events.Condition>();

    private void OnRoundStarted(Events.RoundStartedEvent e)
    {
        ResetMovement();
        eventQueue.Clear();
        IsStopped = false;
        eventHandlingDisabled = false;
    }

    private void OnNextTurn(Events.TickEvent e)
    {
        lock (nextTurnMonitor)
        {
            // Unblock methods waiting for the next turn
            Monitor.PulseAll(nextTurnMonitor);
        }
    }

    private void OnBulletFired(Events.BulletFiredEvent e)
    {
        BotIntent.Firepower = 0; // Reset firepower so the bot stops firing continuously
    }

    internal void Start()
    {
        IsRunning = true;
        Connect();
        closedEvent.WaitOne();
    }

    private void Connect()
    {
        var serverUri = socket.ServerUri;
        SanitizeUrl(serverUri);
        try
        {
            socket.Connect();
        }
        catch (Exception)
        {
            throw new BotException($"Could not connect to web socket for URL: {serverUri}");
        }
    }

    private static void SanitizeUrl(Uri uri)
    {
        var scheme = uri.Scheme;
        if (!new List<string> { "ws", "wss" }.Any(s => s.Contains(scheme)))
        {
            throw new BotException($"Wrong scheme used with server URL: {uri}");
        }
    }

    internal void Execute()
    {
        if (!IsRunning)
            return;

        var turnNumber = CurrentTick.TurnNumber;

        DispatchEvents(turnNumber);
        SendIntent();
        WaitForNextTurn(turnNumber);
    }

    private void SendIntent()
    {
        TransferStdOutToBotIntent();
        socket.SendTextMessage(JsonSerializer.Serialize(BotIntent));
    }

    private void TransferStdOutToBotIntent()
    {
        var stdOutText = stdOutStringWriter.ToString();
        BotIntent.StdOut = stdOutText.Length > 0
            ? HttpUtility.JavaScriptStringEncode(stdOutText.Replace("\r", ""))
            : null;
        stdOutStringWriter.GetStringBuilder().Clear();

        var stdErrText = stdErrStringWriter.ToString();
        BotIntent.StdErr = stdErrText.Length > 0
            ? HttpUtility.JavaScriptStringEncode(stdErrText.Replace("\r", ""))
            : null;
        stdErrStringWriter.GetStringBuilder().Clear();
    }

    private void WaitForNextTurn(int turnNumber)
    {
        lock (nextTurnMonitor)
        {
            while (IsRunning && turnNumber >= CurrentTick.TurnNumber)
            {
                try
                {
                    Monitor.Wait(nextTurnMonitor);
                }
                catch (ThreadInterruptedException)
                {
                    return; // stop waiting, thread has been interrupted (stopped)
                }
            }
        }
    }

    private void DispatchEvents(int turnNumber)
    {
        try
        {
            eventQueue.DispatchEvents(turnNumber);
        }
        catch (InterruptEventHandlerException)
        {
            // Do nothing (event handler was stopped by this exception)
        }
        catch (Exception e)
        {
            Console.Error.WriteLine(e);
        }
    }

    internal string Variant => ServerHandshake.Variant;

    internal string Version => ServerHandshake.Version;

    internal int MyId => myId;

    internal GameSetup GameSetup => gameSetup ?? throw new BotException(GameNotRunningMsg);

    internal Schema.BotIntent BotIntent { get; } = NewBotIntent();

    internal Events.TickEvent CurrentTick => tickEvent ?? throw new BotException(TickNotAvailableMsg);

    private long TicksStart
    {
        get
        {
            if (ticksStart == null) throw new BotException(TickNotAvailableMsg);
            return (long)ticksStart;
        }
    }

    internal int TimeLeft
    {
        get
        {
            var passesMicroSeconds = (DateTime.Now.Ticks - TicksStart) / 10;
            return (int)(gameSetup.TurnTimeout - passesMicroSeconds);
        }
    }

    internal bool SetFire(double firepower)
    {
        if (IsNaN(firepower)) throw new ArgumentException("firepower cannot be NaN");

        if (baseBot.Energy < firepower || CurrentTick.BotState.GunHeat > 0)
            return false; // cannot fire yet
        BotIntent.Firepower = firepower;
        return true;
    }

    internal double GunHeat => tickEvent == null ? 0 : tickEvent.BotState.GunHeat;

    internal double Speed => tickEvent == null ? 0 : tickEvent.BotState.Speed;

    internal double TurnRate
    {
        get
        {
            // if the turn rate was modified during the turn
            if (BotIntent.TurnRate != null)
            {
                return (double)BotIntent.TurnRate;
            }

            return tickEvent == null ? 0 : tickEvent.BotState.TurnRate;
        }
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("TurnRate cannot be NaN");
            }

            BotIntent.TurnRate = Math.Clamp(value, -maxTurnRate, maxTurnRate);
        }
    }

    internal double GunTurnRate
    {
        get
        {
            // if the turn rate was modified during the turn
            if (BotIntent.GunTurnRate != null)
            {
                return (double)BotIntent.GunTurnRate;
            }

            return tickEvent == null ? 0 : tickEvent.BotState.GunTurnRate;
        }
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("GunTurnRate cannot be NaN");
            }

            BotIntent.GunTurnRate = Math.Clamp(value, -maxGunTurnRate, maxGunTurnRate);
        }
    }

    internal double RadarTurnRate
    {
        get
        {
            // if the turn rate was modified during the turn
            if (BotIntent.RadarTurnRate != null)
            {
                return (double)BotIntent.RadarTurnRate;
            }

            return tickEvent == null ? 0 : tickEvent.BotState.RadarTurnRate;
        }
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("RadarTurnRate cannot be NaN");
            }

            BotIntent.RadarTurnRate = Math.Clamp(value, -maxRadarTurnRate, maxRadarTurnRate);
        }
    }

    internal double TargetSpeed
    {
        get => BotIntent.TargetSpeed ?? 0d;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("TargetSpeed cannot be NaN");
            }

            BotIntent.TargetSpeed = Math.Clamp(value, -maxSpeed, maxSpeed);
        }
    }

    internal double MaxTurnRate
    {
        get => maxTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("MaxTurnRate cannot be NaN");
            }

            maxTurnRate = Math.Clamp(value, 0, Constants.MaxTurnRate);
        }
    }

    internal double MaxGunTurnRate
    {
        get => maxGunTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("MaxGunTurnRate cannot be NaN");
            }

            maxGunTurnRate = Math.Clamp(value, 0, Constants.MaxGunTurnRate);
        }
    }

    internal double MaxRadarTurnRate
    {
        get => maxRadarTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("MaxRadarTurnRate cannot be NaN");
            }

            maxRadarTurnRate = Math.Clamp(value, 0, Constants.MaxRadarTurnRate);
        }
    }

    internal double MaxSpeed
    {
        get => maxSpeed;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("MaxSpeed cannot be NaN");
            }

            maxSpeed = Math.Clamp(value, 0, Constants.MaxSpeed);
        }
    }

    /// <summary>
    /// Returns the new speed based on the current speed and distance to move.
    ///
    /// <param name="speed">Is the current speed</param>
    /// <param name="distance">Is the distance to move</param>
    /// <return>The new speed</return>
    /// </summary>

    // Credits for this algorithm goes to Patrick Cupka (aka Voidious),
    // Julian Kent (aka Skilgannon), and Positive for the original version:
    // https://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
    internal double GetNewTargetSpeed(double speed, double distance)
    {
        if (distance < 0)
            return -GetNewTargetSpeed(-speed, -distance);

        var targetSpeed = IsPositiveInfinity(distance) ? maxSpeed : Math.Min(GetMaxSpeed(distance), maxSpeed);

        return speed >= 0
            ? Math.Clamp(targetSpeed, speed - absDeceleration, speed + Constants.Acceleration)
            : Math.Clamp(targetSpeed, speed - Constants.Acceleration, speed + GetMaxDeceleration(-speed));
    }

    private double GetMaxSpeed(double distance)
    {
        var decelerationTime =
            Math.Max(1, Math.Ceiling((Math.Sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));
        if (IsPositiveInfinity(decelerationTime))
            return Constants.MaxSpeed;

        var decelerationDistance = (decelerationTime / 2) * (decelerationTime - 1) * absDeceleration;
        return ((decelerationTime - 1) * absDeceleration) + ((distance - decelerationDistance) / decelerationTime);
    }

    private double GetMaxDeceleration(double speed)
    {
        var decelerationTime = speed / absDeceleration;
        var accelerationTime = 1 - decelerationTime;

        return Math.Min(1, decelerationTime) * absDeceleration +
               Math.Max(0, accelerationTime) * Constants.Acceleration;
    }

    internal double GetDistanceTraveledUntilStop(double speed)
    {
        speed = Math.Abs(speed);
        double distance = 0;
        while (speed > 0)
            distance += (speed = GetNewTargetSpeed(speed, 0));

        return distance;
    }

    internal bool AddCondition(Events.Condition condition)
    {
        return Conditions.Add(condition);
    }

    internal bool RemoveCondition(Events.Condition condition)
    {
        return Conditions.Remove(condition);
    }

    internal void SetStop()
    {
        if (IsStopped) return;

        IsStopped = true;

        savedTargetSpeed = BotIntent.TargetSpeed;
        savedTurnRate = BotIntent.TurnRate;
        savedGunTurnRate = BotIntent.GunTurnRate;
        savedRadarTurnRate = BotIntent.RadarTurnRate;

        BotIntent.TargetSpeed = 0;
        BotIntent.TurnRate = 0;
        BotIntent.GunTurnRate = 0;
        BotIntent.RadarTurnRate = 0;

        stopResumeListener?.OnStop();
    }

    internal void SetResume()
    {
        if (!IsStopped) return;

        BotIntent.TargetSpeed = savedTargetSpeed;
        BotIntent.TurnRate = savedTurnRate;
        BotIntent.GunTurnRate = savedGunTurnRate;
        BotIntent.RadarTurnRate = savedRadarTurnRate;

        stopResumeListener?.OnResume();
        IsStopped = false; // must be last step
    }

    internal bool IsStopped { get; private set; }

    internal int GetPriority(Type eventType)
    {
        if (!eventPriorities.ContainsKey(eventType))
        {
            throw new InvalidOperationException("Could not get event priority for the type: " + eventType.Name);
        }

        return eventPriorities[eventType];
    }

    internal void SetPriority(Type eventType, int priority)
    {
        eventPriorities[eventType] = priority;
    }

    internal Color BodyColor
    {
        get => tickEvent?.BotState.BodyColor;
        set => BotIntent.BodyColor = ToIntentColor(value);
    }

    internal Color TurretColor
    {
        get => tickEvent?.BotState.TurretColor;
        set => BotIntent.TurretColor = ToIntentColor(value);
    }

    internal Color RadarColor
    {
        get => tickEvent?.BotState.RadarColor;
        set => BotIntent.RadarColor = ToIntentColor(value);
    }

    internal Color BulletColor
    {
        get => tickEvent?.BotState.BulletColor;
        set => BotIntent.BulletColor = ToIntentColor(value);
    }

    internal Color ScanColor
    {
        get => tickEvent?.BotState.ScanColor;
        set => BotIntent.ScanColor = ToIntentColor(value);
    }

    internal Color TracksColor
    {
        get => tickEvent?.BotState.TracksColor;
        set => BotIntent.TracksColor = ToIntentColor(value);
    }

    internal Color GunColor
    {
        get => tickEvent?.BotState.GunColor;
        set => BotIntent.GunColor = ToIntentColor(value);
    }

    private static string ToIntentColor(Color color) => color == null ? null : "#" + color.ToHex();

    internal IEnumerable<BulletState> BulletStates => tickEvent?.BulletStates ?? ImmutableHashSet<BulletState>.Empty;

    private Schema.ServerHandshake ServerHandshake
    {
        get
        {
            if (serverHandshake == null)
            {
                throw new BotException(NotConnectedToServerMsg);
            }

            return serverHandshake;
        }
    }

    private static Uri ServerUrlFromSetting
    {
        get
        {
            var uri = EnvVars.GetServerUrl() ?? DefaultServerUrl;
            if (!Uri.IsWellFormedUriString(uri, UriKind.Absolute))
            {
                throw new BotException("Incorrect syntax for server uri: " + uri + ". Default is: " +
                                       DefaultServerUrl);
            }

            return new Uri(uri);
        }
    }

    private static string ServerSecretFromSetting => EnvVars.GetServerSecret();

    private void HandleConnected()
    {
        BotEventHandlers.FireConnectedEvent(new Events.ConnectedEvent(socket.ServerUri));
    }

    private void HandleDisconnected(bool remote, int? statusCode, string reason)
    {
        BotEventHandlers.FireDisconnectedEvent(
            new Events.DisconnectedEvent(socket.ServerUri, remote, statusCode, reason));

        closedEvent.Set();
    }

    private void HandleConnectionError(Exception cause)
    {
        BotEventHandlers.FireConnectionErrorEvent(new Events.ConnectionErrorEvent(socket.ServerUri,
            new Exception(cause.Message)));
    }

    private void HandleTextMessage(string json)
    {
        var message = JsonSerializer.Deserialize<Schema.Message>(json);
        try
        {
            var type = message.Type;
            if (string.IsNullOrWhiteSpace(type)) return;

            var msgType = (Schema.MessageType)Enum.Parse(typeof(Schema.MessageType), type);
            switch (msgType)
            {
                case Schema.MessageType.TickEventForBot:
                    HandleTick(json);
                    break;
                case Schema.MessageType.RoundStartedEvent:
                    HandleRoundStarted(json);
                    break;
                case Schema.MessageType.RoundEndedEventForBot:
                    HandleRoundEnded(json);
                    break;
                case Schema.MessageType.GameStartedEventForBot:
                    HandleGameStarted(json);
                    break;
                case Schema.MessageType.GameEndedEventForBot:
                    HandleGameEnded(json);
                    break;
                case Schema.MessageType.SkippedTurnEvent:
                    HandleSkippedTurn(json);
                    break;
                case Schema.MessageType.ServerHandshake:
                    HandleServerHandshake(json);
                    break;
                case Schema.MessageType.GameAbortedEvent:
                    HandleGameAborted();
                    break;
                default:
                    throw new BotException($"Unsupported WebSocket message type: {type}");
            }
        }
        catch (KeyNotFoundException)
        {
            Console.Error.WriteLine(message);

            throw new BotException($"'type' is missing on the JSON message: {json}");
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine(ex);
        }
    }

    private void HandleTick(string json)
    {
        Console.Error.WriteLine("HandleTick #1");

        if (eventHandlingDisabled) return;

        ticksStart = DateTime.Now.Ticks;

        if (BotIntent.Rescan == true)
            BotIntent.Rescan = false;

        var newTickEvent = EventMapper.Map(json, myId);
        eventQueue.AddEventsFromTick(newTickEvent);

        tickEvent = newTickEvent;

        // Trigger next turn (not tick-event!)
        BotEventHandlers.FireNextTurn(tickEvent);
        
        Console.Error.WriteLine("HandleTick #2");
    }

    private void HandleRoundStarted(string json)
    {
        var roundStartedEvent = JsonSerializer.Deserialize<Schema.RoundStartedEvent>(json);

        BotEventHandlers.FireRoundStartedEvent(new Events.RoundStartedEvent(roundStartedEvent.RoundNumber));
    }

    private void HandleRoundEnded(string json)
    {
        var roundEndedEventForBot = JsonSerializer.Deserialize<Schema.RoundEndedEventForBot>(json);

        var botResults = ResultsMapper.Map(roundEndedEventForBot.Results);
        BotEventHandlers.FireRoundEndedEvent(new Events.RoundEndedEvent(roundEndedEventForBot.RoundNumber,
            roundEndedEventForBot.TurnNumber, botResults));
    }

    private void HandleGameStarted(string json)
    {
        var gameStartedEventForBot = JsonSerializer.Deserialize<Schema.GameStartedEventForBot>(json);

        myId = gameStartedEventForBot.MyId;
        gameSetup = GameSetupMapper.Map(gameStartedEventForBot.GameSetup);

        // Send ready signal
        var ready = new Schema.BotReady
        {
            Type = EnumUtil.GetEnumMemberAttrValue(Schema.MessageType.BotReady)
        };

        var msg = JsonSerializer.Serialize(ready);
        socket.SendTextMessage(msg);

        BotEventHandlers.FireGameStartedEvent(new Events.GameStartedEvent(myId, gameSetup));
    }

    private void HandleGameEnded(string json)
    {
        // Send the game ended event
        var gameEndedEventForBot = JsonSerializer.Deserialize<Schema.GameEndedEventForBot>(json);

        var results = ResultsMapper.Map(gameEndedEventForBot.Results);
        BotEventHandlers.FireGameEndedEvent(new Events.GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results));
    }

    private void HandleGameAborted()
    {
        BotEventHandlers.FireGameAbortedEvent();
    }

    private void HandleServerHandshake(string json)
    {
        Console.Error.WriteLine("HandleServerHandshake #1");
    
        serverHandshake = JsonSerializer.Deserialize<Schema.ServerHandshake>(json);

        // Reply by sending bot handshake
        var botHandshake = BotHandshakeFactory.Create(serverHandshake?.SessionId, botInfo, serverSecret);
        botHandshake.Type = EnumUtil.GetEnumMemberAttrValue(Schema.MessageType.BotHandshake);
        var text = JsonSerializer.Serialize(botHandshake);

        Console.Error.WriteLine("HandleServerHandshake #2");

        socket.SendTextMessage(text);
    }

    private void HandleSkippedTurn(string json)
    {
        if (eventHandlingDisabled) return;

        var skippedTurnEvent = JsonSerializer.Deserialize<Schema.SkippedTurnEvent>(json);
        BotEventHandlers.FireSkippedTurnEvent(EventMapper.Map(skippedTurnEvent));
    }
}