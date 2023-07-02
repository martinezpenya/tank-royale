using System.Collections.Generic;
using System.Text.Json;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.Schema;
using BotDeathEvent = Robocode.TankRoyale.BotApi.Events.BotDeathEvent;
using BulletFiredEvent = Robocode.TankRoyale.BotApi.Events.BulletFiredEvent;
using BulletHitBotEvent = Robocode.TankRoyale.BotApi.Events.BulletHitBotEvent;
using BulletHitBulletEvent = Robocode.TankRoyale.BotApi.Events.BulletHitBulletEvent;
using BulletHitWallEvent = Robocode.TankRoyale.BotApi.Events.BulletHitWallEvent;
using HitByBulletEvent = Robocode.TankRoyale.BotApi.Events.HitByBulletEvent;
using ScannedBotEvent = Robocode.TankRoyale.BotApi.Events.ScannedBotEvent;
using SkippedTurnEvent = Robocode.TankRoyale.BotApi.Events.SkippedTurnEvent;
using WonRoundEvent = Robocode.TankRoyale.BotApi.Events.WonRoundEvent;

namespace Robocode.TankRoyale.BotApi.Mapper;

public static class EventMapper
{
    public static TickEvent Map(string json, int myBotId)
    {
        var tickEvent = JsonSerializer.Deserialize<Schema.TickEventForBot>(json);
        if (tickEvent == null)
            throw new BotException("TickEventForBot is missing in JSON message from server");

        var jsonTickEvent = JsonSerializer.Deserialize<Dictionary<string, object>>(json);
        if (jsonTickEvent == null)
            throw new BotException("TickEventForBot dictionary is missing in JSON message from server");

        var events = (ICollection<Event>)jsonTickEvent["events"];
        return new TickEvent(
            tickEvent.TurnNumber,
            tickEvent.RoundNumber,
            tickEvent.EnemyCount,
            BotStateMapper.Map(tickEvent.BotState),
            BulletStateMapper.Map(tickEvent.BulletStates),
            Map(events, myBotId)
        );
    }

    private static IEnumerable<BotEvent> Map(ICollection<Event> events, int myBotId)
    {
        var gameEvents = new HashSet<BotEvent>();
        foreach (var evt in events)
        {
            gameEvents.Add(Map(evt, myBotId));
        }

        return gameEvents;
    }

    private static BotEvent Map(Event evt, int myBotId)
    {
        return evt.Type switch
        {
            "BotDeathEvent" => Map((Schema.BotDeathEvent)evt, myBotId),
            "BotHitBotEvent" => Map((Schema.BotHitBotEvent)evt),
            "BotHitWallEvent" => Map((Schema.BotHitWallEvent)evt),
            "BulletFiredEvent" => Map((Schema.BulletFiredEvent)evt),
            "BulletHitBotEvent" => Map((Schema.BulletHitBotEvent)evt, myBotId),
            "BulletHitBulletEvent" => Map((Schema.BulletHitBulletEvent)evt),
            "BulletHitWallEvent" => Map((Schema.BulletHitWallEvent)evt),
            "ScannedBotEvent" => Map((Schema.ScannedBotEvent)evt),
            "SkippedTurnEvent" => Map((Schema.SkippedTurnEvent)evt),
            "WonRoundEvent" => Map((Schema.WonRoundEvent)evt),
            _ => throw new BotException("No mapping exists for event type: " + evt.Type)
        };
    }

    private static BotEvent Map(Schema.BotDeathEvent source, int myBotId)
    {
        if (source.VictimId == myBotId)
        {
            return new DeathEvent(source.TurnNumber);
        }

        return new BotDeathEvent(source.TurnNumber, source.VictimId);
    }

    private static HitBotEvent Map(Schema.BotHitBotEvent source)
    {
        return new HitBotEvent(
            source.TurnNumber,
            source.VictimId,
            source.Energy,
            source.X,
            source.Y,
            source.Rammed
        );
    }

    private static HitWallEvent Map(Schema.BotHitWallEvent source)
    {
        return new HitWallEvent(source.TurnNumber);
    }

    private static BulletFiredEvent Map(Schema.BulletFiredEvent source)
    {
        return new BulletFiredEvent(
            source.TurnNumber,
            BulletStateMapper.Map(source.Bullet)
        );
    }

    private static BotEvent Map(Schema.BulletHitBotEvent source, int myBotId)
    {
        var bullet = BulletStateMapper.Map(source.Bullet);
        if (source.VictimId == myBotId)
        {
            return new HitByBulletEvent(
                source.TurnNumber,
                bullet,
                source.Damage,
                source.Energy);
        }

        return new BulletHitBotEvent(
            source.TurnNumber,
            source.VictimId,
            bullet,
            source.Damage,
            source.Energy);
    }

    private static BulletHitBulletEvent Map(Schema.BulletHitBulletEvent source)
    {
        return new BulletHitBulletEvent(
            source.TurnNumber,
            BulletStateMapper.Map(source.Bullet),
            BulletStateMapper.Map(source.HitBullet)
        );
    }

    private static BulletHitWallEvent Map(Schema.BulletHitWallEvent source)
    {
        return new BulletHitWallEvent(
            source.TurnNumber,
            BulletStateMapper.Map(source.Bullet)
        );
    }

    private static ScannedBotEvent Map(Schema.ScannedBotEvent source)
    {
        return new ScannedBotEvent(
            source.TurnNumber,
            source.ScannedByBotId,
            source.ScannedBotId,
            source.Energy,
            source.X,
            source.Y,
            source.Direction,
            source.Speed
        );
    }

    public static SkippedTurnEvent Map(Schema.SkippedTurnEvent source)
    {
        return new SkippedTurnEvent(
            source.TurnNumber
        );
    }

    private static WonRoundEvent Map(Schema.WonRoundEvent source)
    {
        return new WonRoundEvent(
            source.TurnNumber
        );
    }
}