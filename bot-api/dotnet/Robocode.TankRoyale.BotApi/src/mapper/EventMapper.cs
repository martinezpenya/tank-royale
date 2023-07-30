using System;
using System.Collections.Generic;
using System.Text.Json;
using System.Text.Json.Serialization;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Mapper;

public static class EventMapper
{
    public static TickEvent Map(string json, int myBotId)
    {
        Console.WriteLine("JSON: " + json);

        var options = new JsonSerializerOptions
        {
            Converters = { new EventJsonConverter() },
        };
        
        var tickEvent = JsonSerializer.Deserialize<Schema.TickEventForBot>(json, options);
        if (tickEvent == null)
            throw new BotException("TickEventForBot is missing in JSON message from server");

        var events = tickEvent.Events;

        return new TickEvent(
            tickEvent.TurnNumber,
            tickEvent.RoundNumber,
            tickEvent.EnemyCount,
            BotStateMapper.Map(tickEvent.BotState),
            BulletStateMapper.Map(tickEvent.BulletStates),
            Map(events, myBotId)
        );
    }

    private static IEnumerable<BotEvent> Map(IEnumerable<Schema.Event> events, int myBotId)
    {
        var gameEvents = new HashSet<BotEvent>();
        foreach (var evt in events)
        {
            gameEvents.Add(Map(evt, myBotId));
        }

        return gameEvents;
    }

    private static BotEvent Map(Schema.Event evt, int myBotId)
    {
        Console.WriteLine("Event: " + evt);

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
            return new DeathEvent(source.TurnNumber);

        return new BotDeathEvent(source.TurnNumber, source.VictimId);
    }

    private static HitBotEvent Map(Schema.BotHitBotEvent source) =>
        new(
            source.TurnNumber,
            source.VictimId,
            source.Energy,
            source.X,
            source.Y,
            source.Rammed
        );

    private static HitWallEvent Map(Schema.BotHitWallEvent source) =>
        new(source.TurnNumber);

    private static BulletFiredEvent Map(Schema.BulletFiredEvent source) =>
        new(
            source.TurnNumber,
            BulletStateMapper.Map(source.Bullet)
        );

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

    private static BulletHitBulletEvent Map(Schema.BulletHitBulletEvent source) =>
        new(
            source.TurnNumber,
            BulletStateMapper.Map(source.Bullet),
            BulletStateMapper.Map(source.HitBullet)
        );

    private static BulletHitWallEvent Map(Schema.BulletHitWallEvent source) =>
        new(
            source.TurnNumber,
            BulletStateMapper.Map(source.Bullet)
        );

    private static ScannedBotEvent Map(Schema.ScannedBotEvent source) =>
        new(
            source.TurnNumber,
            source.ScannedByBotId,
            source.ScannedBotId,
            source.Energy,
            source.X,
            source.Y,
            source.Direction,
            source.Speed
        );

    public static SkippedTurnEvent Map(Schema.SkippedTurnEvent source) =>
        new(source.TurnNumber);

    private static WonRoundEvent Map(Schema.WonRoundEvent source) =>
        new(source.TurnNumber);


    private class EventJsonConverter : JsonConverter<Schema.Event>
    {
        public override bool CanConvert(Type typeToConvert) =>
            typeof(Schema.Event).IsAssignableFrom(typeToConvert);

        public override Schema.Event Read(ref Utf8JsonReader reader,
            Type typeToConvert, JsonSerializerOptions options)
        {
        }

        public override void Write(Utf8JsonWriter writer,
            Schema.Event member, JsonSerializerOptions options)
        {
            throw new NotImplementedException();
        }
    }
}