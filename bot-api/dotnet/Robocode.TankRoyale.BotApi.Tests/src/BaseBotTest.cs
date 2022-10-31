using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using static Robocode.TankRoyale.BotApi.Tests.Test_utils.EnvironmentVariables;

namespace Robocode.TankRoyale.BotApi.Tests;

public class BaseBotTest
{
    private MockedServer _server;

    private static readonly BotInfo botInfo = BotInfo.Builder()
        .SetName("TestBot")
        .SetVersion("1.0")
        .AddAuthor("Author")
        .Build();

    class TestBot : BaseBot
    {
        public TestBot() : base(botInfo, MockedServer.ServerUrl) {}
    }

    [SetUp]
    public void SetUp()
    {
        SetAllEnvVarsToDefaultValues();
        _server = new MockedServer();
        _server.Start();
    }

    [TearDown]
    public void Teardown()
    {
        _server.Stop();
    }

    // Start()
    [Test]
    public void GivenTestBot_whenCallingStart_thenBotConnectsToServer()
    {
        Start();
        Assert.That(_server.AwaitConnection(1000), Is.True);
    }
    
    // Go()
    [Test]
    public void GivenTestBot_whenCallingGo_thenBotIntentIsReceivedAtServer()
    {
        var bot = StartAndAwaitTickEvent();
        bot.Go();
        AwaitBotIntent();
    }


    private static BaseBot Start()
    {
        var bot = new TestBot();
        new Thread(bot.Start).Start();
        return bot;
    }
    
    private static BaseBot StartAndGo() {
        var bot = Start();
        bot.Go();
        return bot;
    }
    
    private BaseBot StartAndAwaitHandshake() {
        var bot = Start();
        AwaitBotHandshake();
        return bot;
    }
    
    private BaseBot StartAndAwaitTickEvent() {
        var bot = Start();
        AwaitTickEvent();
        return bot;
    }
    
    private BaseBot StartAndAwaitGameStarted() {
        var bot = Start();
        AwaitGameStarted();
        return bot;
    }
    
    private void AwaitBotHandshake() {
        _server.AwaitBotHandshake(500);
    }

    private void AwaitGameStarted() {
        Assert.That(_server.AwaitGameStarted(500), Is.True);
        Sleep(); // must be processed within the bot api first
    }

    private void AwaitTickEvent()
    {
        Assert.That(_server.AwaitTickEvent(500), Is.True);
        Sleep(); // must be processed within the bot api first
    }

    private void AwaitBotIntent()
    {
        Assert.That(_server.AwaitBotIntent(1000), Is.True);
    }

    private void Sleep() {
        Thread.Sleep(1000);
    }
}