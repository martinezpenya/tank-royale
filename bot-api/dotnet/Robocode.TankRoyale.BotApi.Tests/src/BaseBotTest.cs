using System;
using System.Linq;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

public class BaseBotTest : AbstractBotTest
{
    // Start()
    [Test]
    public void GivenTestBot_whenCallingStart_thenBotConnectsToServer()
    {
        Start();
        AwaitBotHandshake();
    }

    // Go()
    [Test]
    public void GivenTestBot_whenCallingGo_thenBotIntentIsReceivedAtServer()
    {
        var bot = StartAndAwaitTickEvent();
        GoAsync(bot);
        AwaitBotIntent();
    }

    // Variant
    [Test]
    public void GivenMockedServer_whenCallingVariant_thenVariantMustContainMockedValue()
    {
        var bot = StartAndAwaitHandshake();
        Assert.That(bot.Variant, Is.EqualTo(MockedServer.Variant));
    }

    // Version
    [Test]
    public void GivenMockedServer_whenCallingVersion_thenVersionMustContainMockedValue()
    {
        var bot = StartAndAwaitHandshake();
        Assert.That(bot.Version, Is.EqualTo(MockedServer.Version));
    }

    // MyId
    [Test]
    public void GivenMockedServer_whenCallingMyId_thenMyIdMustContainMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.MyId, Is.EqualTo(MockedServer.MyId));
    }

    // GameType
    [Test]
    public void GivenMockedServer_whenCallingGameType_thenGameTypeMustContainMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.GameType, Is.EqualTo(MockedServer.GameType));
    }

    // ArenaWidth
    [Test]
    public void GivenMockedServer_whenCallingArenaWidth_thenArenaWidthIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.ArenaWidth, Is.EqualTo(MockedServer.ArenaWidth));
    }

    // ArenaHeight
    [Test]
    public void GivenMockedServer_whenCallingArenaHeight_thenArenaHeightIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.ArenaHeight, Is.EqualTo(MockedServer.ArenaHeight));
    }

    // NumberOfRounds
    [Test]
    public void GivenMockedServer_whenCallingNumberOfRounds_thenNumberOfRoundsIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.NumberOfRounds, Is.EqualTo(MockedServer.NumberOfRounds));
    }

    // GunCoolingRate
    [Test]
    public void GivenMockedServer_whenCallingGunCoolingRate_thenGunCoolingRateIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.GunCoolingRate, Is.EqualTo(MockedServer.GunCoolingRate));
    }

    // MaxInactivityTurns
    [Test]
    public void GivenMockedServer_whenCallingMaxInactivityTurns_thenMaxInactivityTurnsIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.MaxInactivityTurns, Is.EqualTo(MockedServer.MaxInactivityTurns));
    }

    // TurnTimeout
    [Test]
    public void GivenMockedServer_whenCallingTurnTimeout_thenTurnTimeoutIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.TurnTimeout, Is.EqualTo(MockedServer.TurnTimeout));
    }

    // TimeLeft
    [Test]
    [Ignore("the test runs too slow, so the time left is returned as a negative value")]
    public void GivenMockedServer_whenCallingTimeLeft_thenTimeLeftMustBeLesserThanTurnTimeout()
    {
        var bot = StartAndAwaitGameStarted();
        GoAsync(bot); // skip first turn due to initialization, which could take longer than the turn timeout
        AwaitBotIntent();
        Assert.That(bot.TimeLeft, Is.GreaterThan(0));
        Assert.That(bot.TimeLeft, Is.LessThan(MockedServer.TurnTimeout));
    }

    // RoundNumber
    [Test]
    public void GivenMockedServer_whenCallingRoundNumber_thenRoundNumberMustBeTheFirst()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.RoundNumber, Is.EqualTo(1));
    }

    // TurnNumber
    [Test]
    public void GivenMockedServer_whenCallingTurnNumber_thenTurnNumberMustBeTheFirst()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.TurnNumber, Is.EqualTo(1));
    }

    // EnemyCount
    [Test]
    public void GivenMockedServer_whenCallingEnemyCount_thenEnemyCountIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.EnemyCount, Is.EqualTo(MockedServer.BotEnemyCcount));
    }

    // Energy
    [Test]
    public void GivenMockedServer_whenCallingEnergy_thenEnergyIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.Energy, Is.EqualTo(MockedServer.BotEnergy));
    }

    // IsDisabled = false
    [Test]
    public void GivenMockedServerAndSettingEnergyToNonZero_whenCallingIsDisabled_thenDisabledValueMustBeFalse()
    {
        var bot = Start();
        Server.SetBotEnergy(0.1);
        AwaitTickEvent();
        Assert.That(bot.IsDisabled, Is.False);
    }

    // IsDisabled = true
    [Test]
    public void GivenMockedServerAndSettingEnergyToZero_whenCallingIsDisabled_thenDisabledValueMustBeTrue()
    {
        var bot = Start();
        Server.SetBotEnergy(0);
        AwaitTickEvent();
        Assert.That(bot.IsDisabled, Is.True);
    }

    // X
    [Test]
    public void GivenMockedServer_whenCallingX_thenXIsEqualToMockedValue()
    {
        var bot = StartAndAwaitTickEvent();
        Assert.That(bot.X, Is.EqualTo(MockedServer.BotX));
    }

    // Y
    [Test]
    public void GivenMockedServer_whenCallingY_thenXIsEqualToMockedValue()
    {
        var bot = StartAndAwaitTickEvent();
        Assert.That(bot.Y, Is.EqualTo(MockedServer.BotY));
    }

    // Direction
    [Test]
    public void GivenMockedServer_whenCallingDirection_thenDirectionIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.Direction, Is.EqualTo(MockedServer.BotDirection));
    }

    // GunDirection
    [Test]
    public void GivenMockedServer_whenCallingGunDirection_thenGunDirectionIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.GunDirection, Is.EqualTo(MockedServer.BotGunDirection));
    }

    // RadarDirection
    [Test]
    public void GivenMockedServer_whenCallingRadarDirection_thenRadarDirectionIsEqualToMockedValue()
    {
        var bot = StartAndAwaitGameStarted();
        Assert.That(bot.RadarDirection, Is.EqualTo(MockedServer.BotRadarDirection));
    }

    // Speed
    [Test]
    public void GivenMockedServer_whenCallingSpeed_thenSpeedIsEqualToMockedValue()
    {
        var bot = Start();
        Assert.That(bot.Speed, Is.Zero);

        AwaitTickEvent();
        Assert.That(bot.Speed, Is.EqualTo(MockedServer.BotSpeed));
    }

    // GunHeat
    [Test]
    public void GivenMockedServer_whenCallingGunHeat_thenGunHeatIsEqualToMockedValue()
    {
        var bot = Start();
        Assert.That(bot.GunHeat, Is.Zero);

        AwaitTickEvent();
        Assert.That(bot.GunHeat, Is.EqualTo(MockedServer.BotGunHeat));
    }

    // BulletStates
    [Test]
    public void GivenMockedServer_whenCallingBulletStates_thenBulletStatesIsEqualToMockedValue()
    {
        var bot = Start();
        Assert.That(bot.BulletStates.Count(), Is.Zero);

        AwaitTickEvent();

        Assert.That(bot.BulletStates, Is.Not.Null);
        Assert.That(bot.BulletStates.Count(), Is.EqualTo(2));
        Assert.That(bot.BulletStates.Any(bullet => bullet.BulletId == 1), Is.True);
        Assert.That(bot.BulletStates.Any(bullet => bullet.BulletId == 2), Is.True);
    }

    // Events
    [Test]
    public void GivenMockedServer_whenCallingGetEvents_thenEventsIsEqualToMockedValue()
    {
        var bot = Start();
        Assert.That(bot.Events.Count, Is.Zero);

        AwaitTickEvent();

        var events = bot.Events;
        Assert.That(events, Is.Not.Null);
        Assert.That(events.Count(), Is.EqualTo(2));
        Assert.That(events.Any(botEvent => botEvent is TickEvent), Is.True);
        Assert.That(events.Any(botEvent => botEvent is ScannedBotEvent), Is.True);
    }

    // ClearEvents
    [Test]
    public void GivenMockedServer_whenCallingClearEvents_thenEventsMustBeEmpty()
    {
        var bot = Start();

        bot.ClearEvents();
        Assert.That(bot.Events, Is.Empty);
    }

    // TurnRate get
    [Test]
    public void givenMockedServer_whenCallingGetTurnRate_thenTurnRateIsEqualToMockedValue()
    {
        var bot = Start();
        Assert.That(bot.TurnRate, Is.Zero);

        AwaitTickEvent();
        Assert.That(bot.TurnRate, Is.EqualTo(MockedServer.BotTurnRate));
    }

    // TurnRate set <= max turn rate
    [Test]
    public void GivenMockedServer_whenCallingSetTurnRateLowerThanMax_thenTurnRateMustBeUpdatedToNewValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.TurnRate = 7.25;
        Assert.That(bot.TurnRate, Is.EqualTo(7.25));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.TurnRate, Is.EqualTo(7.25));
    }

    // TurnRate set > max turn rate
    [Test]
    public void GivenMockedServer_whenCallingSetTurnRateLowerGreaterThanMax_thenTurnRateMustBeSetToMaxValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.TurnRate = Constants.MaxTurnRate + 1;
        Assert.That(bot.TurnRate, Is.EqualTo(Constants.MaxTurnRate));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.TurnRate, Is.EqualTo(Constants.MaxTurnRate));
    }

    // MaxTurnRate get
    [Test]
    public void GivenMockedServer_whenCallingGetMaxTurnRate_thenMaxTurnRateMustBeEqualToDefaultValue()
    {
        var bot = Start();
        Assert.That(bot.MaxTurnRate, Is.EqualTo(Constants.MaxTurnRate));
    }

    // MaxTurnRate set
    [Test]
    public void GivenMockedServer_whenCallingSetMaxTurnRate_thenMaxTurnRateMustBeUpdatedToNewValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.MaxTurnRate = 5;
        bot.TurnRate = 7;

        Assert.That(bot.MaxTurnRate, Is.EqualTo(5));
        Assert.That(bot.TurnRate, Is.EqualTo(5));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.MaxTurnRate, Is.EqualTo(5));
        Assert.That(bot.TurnRate, Is.EqualTo(5));
    }

    // GunTurnRate get
    [Test]
    public void GivenMockedServer_whenCallingGetGunTurnRate_thenGunTurnRateIsEqualToMockedValue()
    {
        var bot = Start();
        Assert.That(bot.GunTurnRate, Is.Zero);

        AwaitTickEvent();
        Assert.That(bot.GunTurnRate, Is.EqualTo(MockedServer.BotGunTurnRate));
    }

    // GunTurnRate set <= max gun turn rate
    [Test]
    public void GivenMockedServer_whenCallingSetGunTurnRateLowerThanMax_thenGunTurnRateMustBeUpdatedToNewValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.GunTurnRate = 17.25;
        Assert.That(bot.GunTurnRate, Is.EqualTo(17.25));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.GunTurnRate, Is.EqualTo(17.25));
    }

    // GunTurnRate set > max gun turn rate
    [Test]
    public void GivenMockedServer_whenCallingSetGunTurnRateLowerGreaterThanMax_thenGunTurnRateMustBeSetToMaxValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.GunTurnRate = Constants.MaxGunTurnRate + 1;
        Assert.That(bot.GunTurnRate, Is.EqualTo(Constants.MaxGunTurnRate));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.GunTurnRate, Is.EqualTo(Constants.MaxGunTurnRate));
    }
    
    // MaxGunTurnRate get
    [Test]
    public void GivenMockedServer_whenCallingGetMaxGunTurnRate_thenMaxGunTurnRateMustBeEqualToDefaultValue()
    {
        var bot = Start();
        Assert.That(bot.MaxGunTurnRate, Is.EqualTo(Constants.MaxGunTurnRate));
    }

    // MaxGunTurnRate set
    [Test]
    public void GivenMockedServer_whenCallingSetGunMaxTurnRate_thenMaxGunTurnRateMustBeUpdatedToNewValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.MaxGunTurnRate = 15;
        bot.GunTurnRate = 17;

        Assert.That(bot.MaxGunTurnRate, Is.EqualTo(15));
        Assert.That(bot.GunTurnRate, Is.EqualTo(15));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.MaxGunTurnRate, Is.EqualTo(15));
        Assert.That(bot.GunTurnRate, Is.EqualTo(15));
    }
    
    // RadarTurnRate get
    [Test]
    public void GivenMockedServer_whenCallingGetRadarTurnRate_thenRadarTurnRateIsEqualToMockedValue()
    {
        var bot = Start();
        Assert.That(bot.RadarTurnRate, Is.Zero);

        AwaitTickEvent();
        Assert.That(bot.RadarTurnRate, Is.EqualTo(MockedServer.BotRadarTurnRate));
    }

    // RadarTurnRate set <= max radar turn rate
    [Test]
    public void GivenMockedServer_whenCallingSetRadarTurnRateLowerThanMax_thenRadarTurnRateMustBeUpdatedToNewValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.RadarTurnRate = 37.25;
        Assert.That(bot.RadarTurnRate, Is.EqualTo(37.25));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.RadarTurnRate, Is.EqualTo(37.25));
    }

    // RadarTurnRate set > max radar turn rate
    [Test]
    public void GivenMockedServer_whenCallingSetRadarTurnRateLowerGreaterThanMax_thenRadarTurnRateMustBeSetToMaxValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.RadarTurnRate = Constants.MaxRadarTurnRate + 1;
        Assert.That(bot.RadarTurnRate, Is.EqualTo(Constants.MaxRadarTurnRate));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.RadarTurnRate, Is.EqualTo(Constants.MaxRadarTurnRate));
    }

    // MaxRadarTurnRate get
    [Test]
    public void GivenMockedServer_whenCallingGetMaxRadarTurnRate_thenMaxRadarTurnRateMustBeEqualToDefaultValue()
    {
        var bot = Start();
        Assert.That(bot.MaxRadarTurnRate, Is.EqualTo(Constants.MaxRadarTurnRate));
    }

    // MaxRadarTurnRate set
    [Test]
    public void GivenMockedServer_whenCallingSetMaxRadarTurnRate_thenMaxRadarTurnRateMustBeUpdatedToNewValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.MaxRadarTurnRate = 25;
        bot.RadarTurnRate = 27;

        Assert.That(bot.MaxRadarTurnRate, Is.EqualTo(25));
        Assert.That(bot.RadarTurnRate, Is.EqualTo(25));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.MaxRadarTurnRate, Is.EqualTo(25));
        Assert.That(bot.RadarTurnRate, Is.EqualTo(25));
    }

    // TargetSpeed get/set
    [Test]
    public void GivenMockedServer_whenCallingTargetSpeed_thenTargetSpeedMustBeSetValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.TargetSpeed = 5.75;
        Assert.That(bot.TargetSpeed, Is.EqualTo(5.75));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.TargetSpeed, Is.EqualTo(5.75));
    }
    
    // MaxSpeed get
    [Test]
    public void GivenMockedServer_whenCallingGetMaxSpeed_thenMaxSpeedMustBeEqualToDefaultValue()
    {
        var bot = Start();
        Assert.That(bot.MaxSpeed, Is.EqualTo(Constants.MaxSpeed));
    }

    // MaxSpeed set
    [Test]
    public void GivenMockedServer_whenCallingSetMaxSpeed_thenMaxSpeedMustBeUpdatedToNewValue()
    {
        var bot = StartAndAwaitTickEvent();
        bot.MaxSpeed = 4;
        bot.TargetSpeed = 6;

        Assert.That(bot.MaxSpeed, Is.EqualTo(4));
        Assert.That(bot.TargetSpeed, Is.EqualTo(4));

        GoAsync(bot);
        AwaitBotIntent();

        Assert.That(bot.MaxSpeed, Is.EqualTo(4));
        Assert.That(bot.TargetSpeed, Is.EqualTo(4));
    }
    
    // Fire set when gunHeat > 0
    [Test]
    public void GivenMockedServer_whenCallingSetFireAndGunHeatGreaterThanZero_thenReturnFalse()
    {
        Server.SetBotGunHeat(0.1);
        var bot = StartAndAwaitTickEvent();
        Assert.That(bot.SetFire(3), Is.False);
    }

    // Fire set when gunHeat = 0
    [Test]
    public void GivenMockedServer_whenCallingSetFireAndGunHeatIsZero_thenReturnTrue()
    {
        Server.SetBotGunHeat(0);
        var bot = StartAndAwaitTickEvent();
        Assert.That(bot.SetFire(3), Is.True);
    }
    
    // Firepower get
    [Test]
    public void GivenMockedServer_whenCallingFirepowerAfterSetFire_thenReturnSameValueAsFired()
    {
        Server.SetBotGunHeat(0);
        var bot = StartAndAwaitTickEvent();
        bot.SetFire(2.5);
        Assert.That(bot.Firepower, Is.EqualTo(2.5));
    }
    
    // AdjustGunForBodyTurn get/set
    [Test]
    public void GivenMockedServer_whenCallingAdjustGunForBodyTurn_thenReturnSameValueAsSet()
    {
        var bot = Start();

        bot.AdjustGunForBodyTurn = false;
        Assert.That(bot.AdjustGunForBodyTurn, Is.False);

        bot.AdjustGunForBodyTurn = true;
        Assert.That(bot.AdjustGunForBodyTurn, Is.True);
    }
    
    // AdjustRadarForBodyTurn get/set
    [Test]
    public void GivenMockedServer_whenCallingAdjustRadarForBodyTurn_thenReturnSameValueAsSet()
    {
        var bot = Start();

        bot.AdjustRadarForBodyTurn = false;
        Assert.That(bot.AdjustRadarForBodyTurn, Is.False);

        bot.AdjustRadarForBodyTurn = true;
        Assert.That(bot.AdjustRadarForBodyTurn, Is.True);
    }
    
    // AdjustRadarForGunTurn get/set
    [Test]
    public void GivenMockedServer_whenCallingAdjustRadarForGunTurn_thenReturnSameValueAsSet()
    {
        var bot = Start();

        bot.AdjustRadarForGunTurn = false;
        Assert.That(bot.AdjustRadarForGunTurn, Is.False);

        bot.AdjustRadarForGunTurn = true;
        Assert.That(bot.AdjustRadarForGunTurn, Is.True);
    }
    
    // AddCondition()
    [Test]
    public void GivenMockedServer_whenCallingAddCondition_thenReturnTrueFirstTimeFalseNextTime()
    {
        var bot = Start();

        var condition1 = new Condition();
        var condition2 = new Condition(() => true);
        var condition3 = new Condition("cond1");
        var condition4 = new Condition("cond1", () => true);
        
        Assert.That(bot.AddCustomEvent(condition1), Is.True); // not added previously
        Assert.That(bot.AddCustomEvent(condition2), Is.True);
        Assert.That(bot.AddCustomEvent(condition3), Is.True);
        Assert.That(bot.AddCustomEvent(condition4), Is.True);

        Assert.That(bot.AddCustomEvent(condition1), Is.False); // already added
        Assert.That(bot.AddCustomEvent(condition2), Is.False);
        Assert.That(bot.AddCustomEvent(condition3), Is.False);
        Assert.That(bot.AddCustomEvent(condition4), Is.False);
    }
    
    // RemoveCondition()
    [Test]
    public void GivenMockedServerAndAddedCondition_whenCallingRemoveCondition_thenReturnTrueFirstTimeFalseNextTime()
    {
        var bot = Start();

        var nonAddedCondition = new Condition();
        
        Assert.That(bot.RemoveCustomEvent(nonAddedCondition), Is.False);

        var addedCondition = new Condition();
        bot.AddCustomEvent((addedCondition));

        Assert.That(bot.RemoveCustomEvent(addedCondition), Is.True);
        Assert.That(bot.RemoveCustomEvent(addedCondition), Is.False);
    }

    // SetStop(), SetResume(), IsStopped
    [Test]
    public void GivenMockedServer_whenCallingSetStopAndSetResume_thenIsStoppedMustReflectStoppedStatus()
    {
        var bot = Start();
        
        Assert.That(bot.IsStopped, Is.False);

        bot.SetStop();
        Assert.That(bot.IsStopped, Is.True);
        
        bot.SetResume();
        Assert.That(bot.IsStopped, Is.False);
    }

    // BodyColor must return default color
    [Test]
    public void GivenMockedServer_whenCallingBodyColor_thenNullIsReturned() {
        var bot = Start();
        Assert.That(bot.BodyColor, Is.Null);
    }

    // TurretColor must return default color
    [Test]
    public void GivenMockedServer_whenCallingTurretColor_thenNullIsReturned() {
        var bot = Start();
        Assert.That(bot.TurretColor, Is.Null);
    }

    // RadarColor must return default color
    [Test]
    public void GivenMockedServer_whenCallingRadarColor_thenNullIsReturned() {
        var bot = Start();
        Assert.That(bot.RadarColor, Is.Null);
    }

    // BulletColor must return default color
    [Test]
    public void GivenMockedServer_whenCallingBulletColor_thenNullIsReturned() {
        var bot = Start();
        Assert.That(bot.BulletColor, Is.Null);
    }

    // ScanColor must return default color
    [Test]
    public void GivenMockedServer_whenCallingScanColor_thenNullIsReturned() {
        var bot = Start();
        Assert.That(bot.ScanColor, Is.Null);
    }

    // TracksColor must return default color
    [Test]
    public void GivenMockedServer_whenCallingTracksColor_thenNullIsReturned() {
        var bot = Start();
        Assert.That(bot.TracksColor, Is.Null);
    }
    
    // GunColor must return default color
    [Test]
    public void GivenMockedServer_whenCallingGunColor_thenNullIsReturned() {
        var bot = Start();
        Assert.That(bot.GunColor, Is.Null);
    }
    
    // CalcMaxTurnRate
    [TestCase(-10,  ExpectedResult = 4)]
    [TestCase(-8.1,  ExpectedResult = 4)]
    [TestCase(-8,  ExpectedResult = 4)]
    [TestCase(-6,  ExpectedResult = 5.5)]
    [TestCase(-5.5,  ExpectedResult = 5.875)]
    [TestCase(-5,  ExpectedResult = 6.25)]
    [TestCase(-1,  ExpectedResult = 9.25)]
    [TestCase(-0.1,  ExpectedResult = 9.925)]
    [TestCase(-0,  ExpectedResult = 10)]
    [TestCase(0.1,  ExpectedResult = 9.925)]
    [TestCase(2,  ExpectedResult = 8.5)]
    [TestCase(4,  ExpectedResult = 7)]
    [TestCase(8,  ExpectedResult = 4)]
    [TestCase(8.1,  ExpectedResult = 4)]
    [TestCase(20,  ExpectedResult = 4)]
    public double GivenSpeed_whenCallingCalcMaxTurnRate_thenReturnCorrectMaxTurnRate(double speed)
    {
        var bot = Start();
        return bot.CalcMaxTurnRate(speed);
    }
    
    // CalcBulletSpeed
    [TestCase(-1,  ExpectedResult = 19.7)]
    [TestCase(0,  ExpectedResult = 19.7)]
    [TestCase(0.1,  ExpectedResult = 19.7)]
    [TestCase(0.2,  ExpectedResult = 19.4)]
    [TestCase(1,  ExpectedResult = 17)]
    [TestCase(2,  ExpectedResult = 14)]
    [TestCase(2.5,  ExpectedResult = 12.5)]
    [TestCase(3,  ExpectedResult = 11)]
    [TestCase(3.1,  ExpectedResult = 11)]
    [TestCase(10,  ExpectedResult = 11)]
    public double GivenFirepower_whenCallingCalcBulletSpeed_thenReturnCorrectBulletSpeed(double firepower)
    {
        var bot = Start();
        return bot.CalcBulletSpeed(firepower);
    }
    
    // CalcGunHeat
    [TestCase(-1,  ExpectedResult = 1.02)]
    [TestCase(0,  ExpectedResult = 1.02)]
    [TestCase(0.1,  ExpectedResult = 1.02)]
    [TestCase(0.2,  ExpectedResult = 1.04)]
    [TestCase(1,  ExpectedResult = 1.2)]
    [TestCase(2,  ExpectedResult = 1.4)]
    [TestCase(3,  ExpectedResult = 1.6)]
    [TestCase(3.1,  ExpectedResult = 1.6)]
    [TestCase(10,  ExpectedResult = 1.6)]
    public double GivenFirepower_whenCallingCalcGunHeat_thenReturnCorrectGunHeat(double firepower)
    {
        var bot = Start();
        return bot.CalcGunHeat(firepower);
    }

    // GetEventPriority()
    [TestCase(typeof(WonRoundEvent), ExpectedResult = DefaultEventPriority.WonRound)]
    [TestCase(typeof(SkippedTurnEvent), ExpectedResult = DefaultEventPriority.SkippedTurn)]
    [TestCase(typeof(TickEvent), ExpectedResult = DefaultEventPriority.Tick)]
    [TestCase(typeof(CustomEvent), ExpectedResult = DefaultEventPriority.Custom)]
    [TestCase(typeof(BotDeathEvent), ExpectedResult = DefaultEventPriority.BotDeath)]
    [TestCase(typeof(BulletHitWallEvent), ExpectedResult = DefaultEventPriority.BulletHitWall)]
    [TestCase(typeof(BulletHitBulletEvent), ExpectedResult = DefaultEventPriority.BulletHitBullet)]
    [TestCase(typeof(BulletHitBotEvent), ExpectedResult = DefaultEventPriority.BulletHitBot)]
    [TestCase(typeof(BulletFiredEvent), ExpectedResult = DefaultEventPriority.BulletFired)]
    [TestCase(typeof(HitByBulletEvent), ExpectedResult = DefaultEventPriority.HitByBullet)]
    [TestCase(typeof(HitWallEvent), ExpectedResult = DefaultEventPriority.HitWall)]
    [TestCase(typeof(HitBotEvent), ExpectedResult = DefaultEventPriority.HitBot)]
    [TestCase(typeof(ScannedBotEvent), ExpectedResult = DefaultEventPriority.ScannedBot)]
    [TestCase(typeof(DeathEvent), ExpectedResult = DefaultEventPriority.Death)]
    public int GivenAllEventClass_whenCallingGetEventPriority_thenReturnCorrectPriorityValueForThatEventClass(Type eventType) {
        var bot = Start();
        return bot.GetEventPriority(eventType);
    }

    // SetEventPriority()
    [TestCase(typeof(WonRoundEvent))]
    [TestCase(typeof(SkippedTurnEvent))]
    [TestCase(typeof(TickEvent))]
    [TestCase(typeof(CustomEvent))]
    [TestCase(typeof(BotDeathEvent))]
    [TestCase(typeof(BulletHitWallEvent))]
    [TestCase(typeof(BulletHitBulletEvent))]
    [TestCase(typeof(BulletHitBotEvent))]
    [TestCase(typeof(BulletFiredEvent))]
    [TestCase(typeof(HitByBulletEvent))]
    [TestCase(typeof(HitWallEvent))]
    [TestCase(typeof(HitBotEvent))]
    [TestCase(typeof(ScannedBotEvent))]
    [TestCase(typeof(DeathEvent))]
    public void GivenAllEventClass_whenCallingSetEventPriority_thenReturnSameEventPriority(Type eventType) {
        var bot = Start();
        var eventPriority = new Random().Next(-1000, 1000);
        bot.SetEventPriority(eventType, eventPriority);
        Assert.That(bot.GetEventPriority(eventType), Is.EqualTo(eventPriority));
    }
}
