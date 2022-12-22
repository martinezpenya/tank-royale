using System;
using System.Threading;
using Robocode.TankRoyale.BotApi.Events;
using static System.Double;

namespace Robocode.TankRoyale.BotApi.Internal;

internal sealed class BotInternals : IStopResumeListener
{
    private readonly IBot bot;
    private readonly BaseBotInternals baseBotInternals;

    private Thread thread;

    private bool overrideTurnRate;
    private bool overrideGunTurnRate;
    private bool overrideRadarTurnRate;
    private bool overrideTargetSpeed;

    private double previousDirection;
    private double previousGunDirection;
    private double previousRadarDirection;

    private bool isOverDriving;

    private double savedPreviousDirection;
    private double savedPreviousGunDirection;
    private double savedPreviousRadarDirection;

    private double savedDistanceRemaining;
    private double savedTurnRemaining;
    private double savedGunTurnRemaining;
    private double savedRadarTurnRemaining;

    public BotInternals(IBot bot, BaseBotInternals baseBotInternals)
    {
        this.bot = bot;
        this.baseBotInternals = baseBotInternals;

        baseBotInternals.SetStopResumeHandler(this);

        var botEventHandlers = baseBotInternals.BotEventHandlers;
        botEventHandlers.OnGameAborted.Subscribe(OnGameAborted, 100);
        botEventHandlers.OnNextTurn.Subscribe(OnNextTurn, 90);
        botEventHandlers.OnRoundEnded.Subscribe(OnRoundEnded, 90);
        botEventHandlers.OnGameEnded.Subscribe(OnGameEnded, 90);
        botEventHandlers.OnDisconnected.Subscribe(OnDisconnected, 90);
        botEventHandlers.OnHitWall.Subscribe(OnHitWall, 90);
        botEventHandlers.OnHitBot.Subscribe(OnHitBot, 90);
        botEventHandlers.OnDeath.Subscribe(OnDeath, 90);
    }

    private void OnNextTurn(TickEvent evt)
    {
        if (evt.TurnNumber == 1)
            OnFirstTurn();

        ProcessTurn();
    }

    private void OnFirstTurn()
    {
        StopThread(); // sanity before starting a new thread (later)
        ClearRemaining();
        StartThread();
    }

    private void ClearRemaining()
    {
        DistanceRemaining = 0;
        TurnRemaining = 0;
        GunTurnRemaining = 0;
        RadarTurnRemaining = 0;

        previousDirection = bot.Direction;
        previousGunDirection = bot.GunDirection;
        previousRadarDirection = bot.RadarDirection;
    }

    private void OnGameAborted(object dummy)
    {
        StopThread();
    }

    private void OnRoundEnded(RoundEndedEvent evt)
    {
        StopThread();
    }

    private void OnGameEnded(GameEndedEvent evt)
    {
        StopThread();
    }

    private void OnDisconnected(DisconnectedEvent evt)
    {
        StopThread();
    }

    private void ProcessTurn()
    {
        // No movement is possible, when the bot has become disabled
        if (bot.IsDisabled)
        {
            ClearRemaining();
        }
        else
        {
            UpdateTurnRemaining();
            UpdateGunTurnRemaining();
            UpdateRadarTurnRemaining();
            UpdateMovement();
        }
    }

    private void StartThread()
    {
        thread = new Thread(() =>
        {
            baseBotInternals.IsRunning = true;
            try
            {
                baseBotInternals.EnableEventHandling(true); // prevent event queue max limit to be reached
                bot.Run();

                // Skip every turn after the run method has exited
                while (baseBotInternals.IsRunning)
                {
                    bot.Go();
                }
            }
            finally
            {
                baseBotInternals.EnableEventHandling(false); // prevent event queue max limit to be reached
            }
        });
        thread.Start();
    }

    private void StopThread()
    {
        if (!IsRunning) return;

        baseBotInternals.IsRunning = false;

        if (thread == null) return;

        thread.Interrupt();
        try
        {
            thread.Join(100);
            if (thread.IsAlive)
            {
#pragma warning disable SYSLIB0006
                thread.Abort();
#pragma warning restore SYSLIB0006
            }
        }
        catch (ThreadInterruptedException)
        {
            // ignore
        }
        finally
        {
            thread = null;
        }
    }

    private void OnHitWall(HitWallEvent evt)
    {
        DistanceRemaining = 0;
    }

    private void OnHitBot(HitBotEvent evt)
    {
        if (evt.IsRammed)
            DistanceRemaining = 0;
    }

    private void OnDeath(DeathEvent evt)
    {
        StopThread();
    }

    internal bool IsRunning => baseBotInternals.IsRunning;

    public void SetTurnRate(double turnRate)
    {
        overrideTurnRate = false;
        TurnRemaining = ToInfiniteValue(turnRate);
        baseBotInternals.TurnRate = turnRate;
    }

    public void SetGunTurnRate(double gunTurnRate)
    {
        overrideGunTurnRate = false;
        GunTurnRemaining = ToInfiniteValue(gunTurnRate);
        baseBotInternals.GunTurnRate = gunTurnRate;
    }

    public void SetRadarTurnRate(double radarTurnRate)
    {
        overrideRadarTurnRate = false;
        RadarTurnRemaining = ToInfiniteValue(radarTurnRate);
        baseBotInternals.RadarTurnRate = radarTurnRate;
    }

    private static double ToInfiniteValue(double turnRate)
    {
        return turnRate switch
        {
            > 0 => PositiveInfinity,
            < 0 => NegativeInfinity,
            _ => 0
        };
    }

    internal double DistanceRemaining { get; private set; }

    internal double TurnRemaining { get; private set; }

    internal double GunTurnRemaining { get; private set; }

    internal double RadarTurnRemaining { get; private set; }

    internal void SetTargetSpeed(double targetSpeed)
    {
        overrideTargetSpeed = false;
        DistanceRemaining = targetSpeed switch
        {
            NaN => throw new ArgumentException("targetSpeed cannot be NaN"),
            > 0 => PositiveInfinity,
            < 0 => NegativeInfinity,
            _ => 0
        };

        baseBotInternals.TargetSpeed = targetSpeed;
    }

    internal void SetForward(double distance)
    {
        overrideTargetSpeed = true;
        if (IsNaN(distance))
            throw new ArgumentException("distance cannot be NaN");
        GetAndSetNewTargetSpeed(distance);
        DistanceRemaining = distance;
    }

    internal void Forward(double distance)
    {
        if (bot.IsStopped)
            bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetForward(distance);
            do
            {
                bot.Go();
            } while (IsRunning && (DistanceRemaining != 0 || bot.Speed != 0));
        }
    }

    internal void SetTurnLeft(double degrees)
    {
        overrideTurnRate = true;
        TurnRemaining = degrees;
        baseBotInternals.TurnRate = degrees;
    }

    internal void TurnLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnLeft(degrees);
            do
            {
                bot.Go();
            } while (IsRunning && TurnRemaining != 0);
        }
    }

    internal void SetTurnGunLeft(double degrees)
    {
        overrideGunTurnRate = true;
        GunTurnRemaining = degrees;
        baseBotInternals.GunTurnRate = degrees;
    }

    internal void TurnGunLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnGunLeft(degrees);
            do
            {
                bot.Go();
            } while (IsRunning && GunTurnRemaining != 0);
        }
    }

    internal void SetTurnRadarLeft(double degrees)
    {
        overrideRadarTurnRate = true;
        RadarTurnRemaining = degrees;
        baseBotInternals.RadarTurnRate = degrees;
    }

    internal void TurnRadarLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnRadarLeft(degrees);
            do
            {
                bot.Go();
            } while (IsRunning && RadarTurnRemaining != 0);
        }
    }

    internal void Fire(double firepower)
    {
        if (bot.SetFire(firepower))
            bot.Go();
    }

    internal void Rescan()
    {
        baseBotInternals.SetScannedBotEventInterruptible();
        bot.SetRescan();
        bot.Go();
    }

    internal void WaitFor(Condition condition)
    {
        do
        {
            bot.Go();
        } while (IsRunning && !condition.Test());
    }

    internal void Stop()
    {
        baseBotInternals.SetStop();
        bot.Go();
    }

    internal void Resume()
    {
        baseBotInternals.SetResume();
        bot.Go();
    }

    public void OnStop()
    {
        savedPreviousDirection = previousDirection;
        savedPreviousGunDirection = previousGunDirection;
        savedPreviousRadarDirection = previousRadarDirection;

        savedDistanceRemaining = DistanceRemaining;
        savedTurnRemaining = TurnRemaining;
        savedGunTurnRemaining = GunTurnRemaining;
        savedRadarTurnRemaining = RadarTurnRemaining;
    }

    public void OnResume()
    {
        previousDirection = savedPreviousDirection;
        previousGunDirection = savedPreviousGunDirection;
        previousRadarDirection = savedPreviousRadarDirection;

        DistanceRemaining = savedDistanceRemaining;
        TurnRemaining = savedTurnRemaining;
        GunTurnRemaining = savedGunTurnRemaining;
        RadarTurnRemaining = savedRadarTurnRemaining;
    }

    private void UpdateTurnRemaining()
    {
        if (!overrideTurnRate)
            return;

        var delta = bot.CalcDeltaAngle(bot.Direction, previousDirection);
        previousDirection = bot.Direction;

        if (Math.Abs(TurnRemaining) <= Math.Abs(delta))
            TurnRemaining = 0;
        else
        {
            TurnRemaining -= delta;
            if (IsNearZero(TurnRemaining))
                TurnRemaining = 0;
        }

        baseBotInternals.TurnRate = TurnRemaining;
    }

    private void UpdateGunTurnRemaining()
    {
        if (!overrideGunTurnRate)
            return;

        var delta = bot.CalcDeltaAngle(bot.GunDirection, previousGunDirection);
        previousGunDirection = bot.GunDirection;

        if (Math.Abs(GunTurnRemaining) <= Math.Abs(delta))
            GunTurnRemaining = 0;
        else
        {
            GunTurnRemaining -= delta;
            if (IsNearZero(GunTurnRemaining))
                GunTurnRemaining = 0;
        }

        baseBotInternals.GunTurnRate = GunTurnRemaining;
    }

    private void UpdateRadarTurnRemaining()
    {
        if (!overrideRadarTurnRate)
            return;

        var delta = bot.CalcDeltaAngle(bot.RadarDirection, previousRadarDirection);
        previousRadarDirection = bot.RadarDirection;

        if (Math.Abs(RadarTurnRemaining) <= Math.Abs(delta))
            RadarTurnRemaining = 0;
        else
        {
            RadarTurnRemaining -= delta;
            if (IsNearZero(RadarTurnRemaining))
                RadarTurnRemaining = 0;
        }

        baseBotInternals.RadarTurnRate = RadarTurnRemaining;
    }

    private void UpdateMovement()
    {
        if (!overrideTargetSpeed)
        {
            if (Math.Abs(DistanceRemaining) < Math.Abs(bot.Speed))
            {
                DistanceRemaining = 0;
            }
            else
            {
                DistanceRemaining -= bot.Speed;
            }
        }
        else if (IsInfinity(DistanceRemaining))
        {
            baseBotInternals.TargetSpeed =
                IsPositiveInfinity(DistanceRemaining) ? Constants.MaxSpeed : -Constants.MaxSpeed;
        }
        else
        {
            var distance = DistanceRemaining;

            // This is Nat Pavasant's method described here:
            // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
            var newSpeed = GetAndSetNewTargetSpeed(distance);

            // If we are over-driving our distance and we are now at velocity=0 then we stopped
            if (IsNearZero(newSpeed) && isOverDriving)
            {
                DistanceRemaining = 0;
                distance = 0;
                isOverDriving = false;
            }

            // the overdrive flag
            if (Math.Sign(distance * newSpeed) != -1)
                isOverDriving = baseBotInternals.GetDistanceTraveledUntilStop(newSpeed) > Math.Abs(distance);

            DistanceRemaining = distance - newSpeed;
        }
    }

    private double GetAndSetNewTargetSpeed(double distance)
    {
        var speed = baseBotInternals.GetNewTargetSpeed(bot.Speed, distance);
        baseBotInternals.TargetSpeed = speed;
        return speed;
    }

    private static bool IsNearZero(double value) => Math.Abs(value) < .00001;
}