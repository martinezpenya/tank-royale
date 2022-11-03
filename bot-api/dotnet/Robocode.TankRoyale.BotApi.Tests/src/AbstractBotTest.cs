using System.Threading;
using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

public class AbstractBotTest
{
    protected MockedServer Server;

    protected static readonly BotInfo BotInfo = BotInfo.Builder()
        .SetName("TestBot")
        .SetVersion("1.0")
        .AddAuthor("Author 1")
        .AddAuthor("Author 2")
        .SetDescription("Short description")
        .SetHomepage("https://testbot.robocode.dev")
        .AddCountryCode("gb")
        .AddCountryCode("us")
        .AddGameType("classic")
        .AddGameType("1v1")
        .AddGameType("melee")
        .SetPlatform(".Net 6")
        .SetProgrammingLang("C# 10")
        .SetInitialPosition(InitialPosition.FromString("10, 20, 30"))
        .Build();
    
    private class TestBot : BaseBot {
        public TestBot() : base(BotInfo, MockedServer.ServerUrl)
        {
        }
    }
    
    [SetUp]
    public void SetUp()
    {
        Server = new MockedServer();
        Server.Start();
    }

    [TearDown]
    public void Teardown()
    {
        Server.Stop();
    }
    
    protected static BaseBot Start()
    {
        var bot = new TestBot();
        RunAsync(bot);
        return bot;
    }

    protected static void RunAsync(BaseBot bot)
    {
        Task.Run(bot.Start);
    }

    protected BaseBot StartAndAwaitHandshake()
    {
        var bot = Start();
        AwaitBotHandshake();
        return bot;
    }

    protected BaseBot StartAndAwaitTickEvent()
    {
        var bot = Start();
        AwaitTickEvent();
        return bot;
    }

    protected BaseBot StartAndAwaitGameStarted()
    {
        var bot = Start();
        AwaitGameStarted();
        return bot;
    }

    protected void AwaitBotHandshake()
    {
        Assert.That(Server.AwaitBotHandshake(10_000), Is.True);
    }

    protected void AwaitGameStarted()
    {
        Sleep(); // must be processed within the bot api first
        Assert.That(Server.AwaitGameStarted(10_000), Is.True);
    }

    protected void AwaitTickEvent()
    {
        Sleep(); // must be processed within the bot api first
        Assert.That(Server.AwaitTickEvent(10_000), Is.True);
    }

    protected void AwaitBotIntent()
    {
        Assert.That(Server.AwaitBotIntent(10_000), Is.True);
    }

    private static void Sleep()
    {
        Thread.Sleep(500);
    }
}