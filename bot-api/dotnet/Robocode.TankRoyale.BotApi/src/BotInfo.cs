using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;
using Microsoft.Extensions.Configuration;
using Robocode.TankRoyale.BotApi.Util;
using static Robocode.TankRoyale.BotApi.Internal.CollectionUtil;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Bot info contains the properties of a bot.
/// </summary>
public sealed class BotInfo
{
    /// <summary>
    /// Maximum number of characters accepted for the name.
    /// </summary>
    public const int MaxNameLength = 30;

    /// <summary>
    /// Maximum number of characters accepted for the version.
    /// </summary>
    public const int MaxVersionLength = 20;

    /// <summary>
    /// Maximum number of characters accepted for an author name.
    /// </summary>
    public const int MaxAuthorLength = 20;

    /// <summary>
    /// Maximum number of characters accepted for the description.
    /// </summary>
    public const int MaxDescriptionLength = 250;

    /// <summary>
    /// Maximum number of characters accepted for the link to the homepage.
    /// </summary>
    public const int MaxHomepageLength = 150;

    /// <summary>
    /// Maximum number of characters accepted for a game type.
    /// </summary>
    public const int MaxGameTypeLength = 20;

    /// <summary>
    /// Maximum number of characters accepted for a game type.
    /// </summary>
    public const int MaxPlatformLength = 20;

    /// <summary>
    /// Maximum number of characters accepted for the programming language name.
    /// </summary>
    public const int MaxProgrammingLangLength = 30;

    /// <summary>
    /// Maximum number of authors accepted.
    /// </summary>
    public const int MaxNumberOfAuthors = 5;

    /// <summary>
    /// Maximum number of country codes accepted.
    /// </summary>
    public const int MaxNumberOfCountryCodes = 5;

    /// <summary>
    /// Maximum number of game types accepted.
    /// </summary>
    public const int MaxNumberOfGameTypes = 10;

    // required fields:
    private readonly string name;
    private readonly string version;
    private readonly IList<string> authors;
    // optional fields:
    private readonly string description;
    private readonly string homepage;
    private readonly IList<string> countryCodes;
    private readonly ICollection<string> gameTypes;
    private readonly string platform;
    private readonly string programmingLang;

    /// <summary>
    /// Initializes a new instance of the BotInfo class.
    ///
    /// Note that the recommended method for creating a BotInfo class is to use the <see cref="IBuilder"/> interface provided
    /// with the static <see cref="BotInfo.Builder()"/> method.
    /// </summary>
    /// <param name="name">The name of the bot (required).</param>
    /// <param name="version">The version of the bot (required).</param>
    /// <param name="authors">The author(s) of the bot (required).</param>
    /// <param name="description">A short description of the bot (optional).</param>
    /// <param name="homepage">The URL to a web page for the bot (optional).</param>
    /// <param name="countryCodes">The country code(s) for the bot (optional).</param>
    /// <param name="gameTypes">The game type(s) that this bot can handle (optional).</param>
    /// <param name="platform">The platform used for running the bot (optional).</param>
    /// <param name="programmingLang">The programming language used for developing the bot (optional).</param>
    /// <param name="initialPosition">The initial position with starting coordinate and angle (optional).</param>
    public BotInfo(
        string name,
        string version,
        IList<string> authors,
        string description,
        string homepage,
        IList<string> countryCodes,
        ICollection<string> gameTypes,
        string platform,
        string programmingLang,
        InitialPosition initialPosition)
    {
        Name = name;
        Version = version;
        Authors = authors;
        Description = description;
        Homepage = homepage;
        CountryCodes = countryCodes;
        GameTypes = gameTypes;
        Platform = platform;
        ProgrammingLang = programmingLang;
        InitialPosition = initialPosition;
    }

    /// <summary>
    /// Returns a builder for a convenient way of building a <see cref="BotInfo"/> object using the
    /// <a href="https://en.wikipedia.org/wiki/Builder_pattern">builder pattern</a>.
    /// </summary>
    /// <example>
    /// Example of use:
    /// <code>
    /// BotInfo botInfo = BotInfo.Builder()
    ///     .SetName("Rampage")
    ///     .SetVersion("1.0")
    ///     .AddAuthor("John Doh")
    ///     .SetGameTypes(new List { GameType.Classic, GameType.Melee })
    ///     .Build();
    /// </code>
    /// </example>
    /// <returns>A builder for building a <see cref="BotInfo"/> object.</returns>
    public static IBuilder Builder() => new BuilderImpl();

    /// <summary>
    /// The name, e.g., "MyBot". This field must always be provided with the bot info.
    /// </summary>
    /// <value>The name of the bot.</value>
    public string Name
    {
        get => name;
        private init
        {
            if (string.IsNullOrWhiteSpace(value))
                throw new ArgumentException("Name cannot be null, empty or blank");
            if (value.Length > MaxNameLength)
                throw new ArgumentException("Name length exceeds the maximum of " + MaxNameLength + " characters");
            name = value.Trim();
        }
    }

    /// <summary>
    /// The version, e.g., "1.0". This field must always be provided with the bot info.
    /// </summary>
    /// <value>The version of the bot.</value>
    public string Version
    {
        get => version;
        private init
        {
            if (string.IsNullOrWhiteSpace(value))
                throw new ArgumentException("Version cannot be null, empty or blank");
            if (value.Length > MaxVersionLength)
                throw new ArgumentException("Version length exceeds the maximum of " + MaxVersionLength + " characters");
            version = value.Trim();
        }
    }

    /// <summary>
    /// List of author(s) of the bot, e.g., "John Doe (johndoe@somewhere.io)".
    /// At least one author must be provided.
    /// </summary>
    /// <value>The author(s) of the bot.</value>
    public IList<string> Authors
    {
        get => authors;
        private init
        {
            if (value.IsNullOrEmptyOrContainsOnlyBlanks())
                throw new ArgumentException("Authors cannot be null or empty or contain blanks");
            if (value.Count > MaxNumberOfAuthors)
                throw new ArgumentException("Number of authors exceeds the maximum of " + MaxNumberOfAuthors);

            authors = value.ToListWithNoBlanks();

            if (authors.Any(author => author.Length > MaxAuthorLength))
                throw new ArgumentException("Author length exceeds the maximum of " + MaxAuthorLength +
                                            " characters");
        }
    }

    /// <summary>
    /// Short description of the bot, preferably a one-liner.
    /// This field is optional.
    /// </summary>
    /// <value>A short description of the bot.</value>
    public string Description
    {
        get => description;
        private init
        {
            if (value is { Length: > MaxDescriptionLength })
                throw new ArgumentException("Description length exceeds the maximum of " + MaxDescriptionLength + " characters");
            description = ToNullIfBlankElseTrim(value);
        }
    }

    /// <summary>
    /// The URL of a web page for the bot.
    /// This field is optional.
    /// </summary>
    /// <value>The URL of a web page for the bot.</value>
    public string Homepage
    {
        get => homepage;
        private init
        {
            if (value is { Length: > MaxHomepageLength })
                throw new ArgumentException("Homepage length exceeds the maximum of " + MaxHomepageLength + " characters");
            homepage = ToNullIfBlankElseTrim(value);
        }
    }

    /// <summary>
    /// The country code(s) defined by ISO 3166-1 alpha-2, e.g. "us":
    /// https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2.
    /// If no country code is provided, the locale of the system is being used instead.
    /// </summary>
    /// <value>The country code(s) for the bot.</value>
    public IList<string> CountryCodes
    {
        get => countryCodes;
        private init
        {
            if (value.Count > MaxNumberOfCountryCodes)
                throw new ArgumentException("Number of country codes exceeds the maximum of " + MaxNumberOfCountryCodes);

            countryCodes = value.ToListWithNoBlanks().ConvertAll(cc => cc.ToUpper());

            foreach (var countryCode in countryCodes)
                if (!CountryCode.IsCountryCodeValid(countryCode))
                    countryCodes = new List<string> { CountryCode.GetLocalCountryCode() };

            if (CountryCodes.Any()) return;
            var list = new List<string>
            {
                // Get local country code
                CountryCode.GetLocalCountryCode()
            };
            countryCodes = list;
        }
    }

    /// <summary>
    /// The game type(s) accepted by the bot, e.g., "classic", "melee", "1v1". This field must always be
    /// provided with the bot info. The game types define which game types the bot can participate
    /// in. See <see cref="GameType"/> for using predefined game type.
    /// </summary>
    /// <value>The game type(s) that this bot can handle.</value>
    public ICollection<string> GameTypes
    {
        get => gameTypes;
        private init
        {
            if (value.IsNullOrEmptyOrContainsOnlyBlanks())
            {
                gameTypes = new List<string>();
            }
            else
            {
                if (value.Count > MaxNumberOfGameTypes)
                    throw new ArgumentException("Number of game types exceeds the maximum of " + MaxNumberOfGameTypes);

                if (value.Any(gameType => gameType.Length > MaxGameTypeLength))
                    throw new ArgumentException("Game type length exceeds the maximum of " + MaxGameTypeLength +
                                                " characters");

                gameTypes = value.ToListWithNoBlanks();
            }
        }
    }

    /// <summary>
    /// The platform used for running the bot, e.g., ".Net 6.0".
    /// This field is optional.
    /// </summary>
    /// <value>The platform used for running the bot.</value>
    public string Platform
    {
        get => platform;
        private init
        {
            if (string.IsNullOrWhiteSpace(value))
                value = PlatformUtil.GetPlatformName();
            else if (value.Length > MaxPlatformLength)
                throw new ArgumentException("Platform length exceeds the maximum of " + MaxPlatformLength + " characters");
            platform = ToNullIfBlankElseTrim(value);
        }
    }

    /// <summary>
    /// The programming language used for developing the bot, e.g., "C# 8.0" or "F#".
    /// This field is optional.
    /// </summary>
    /// <value>The programming language used for developing the bot.</value>
    public string ProgrammingLang
    {
        get => programmingLang;
        private init
        {
            if (value is { Length: > MaxProgrammingLangLength })
                throw new ArgumentException("ProgrammingLang length exceeds the maximum of " + MaxProgrammingLangLength + " characters");
            programmingLang = ToNullIfBlankElseTrim(value);
        }
    }

    /// <summary>
    /// The initial starting position used for debugging only, which must be enabled at the server.
    /// </summary>
    /// <value>The initial starting position used for debugging only.</value>
    public InitialPosition InitialPosition { get; }

    /// <summary>
    /// Reads the bot info from a file at the specified base path.
    /// The file is assumed to be in JSON format.
    /// </summary>
    /// <example>
    /// Example file in JSON format:
    /// <code>
    /// {
    ///   "name": "MyBot",
    ///   "version": "1.0",
    ///   "authors": "John Doe",
    ///   "description": "Short description",
    ///   "url": "https://somewhere.net/MyBot",
    ///   "countryCodes": "us",
    ///   "gameTypes": "classic, melee, 1v1",
    ///   "platform": ".Net 6.0",
    ///   "programmingLang": "C# 10.0",
    ///   "initialPosition": "50,50, 90"
    /// }
    /// </code>
    /// Note that these fields are required as these are used to identify the bot:
    /// <ul>
    ///   <li>name</li>
    ///   <li>version</li>
    ///   <li>authors</li>
    /// </ul>
    /// These value can take multiple values separated by a comma:
    /// <ul>
    ///   <li>authors, e.g. "John Doe, Jane Doe"</li>
    ///   <li>countryCodes, e.g. "se, no, dk"</li>
    ///   <li>gameTypes, e.g. "classic, melee, 1v1"</li>
    /// </ul>
    /// The <c>initialPosition</c> variable is optional and should <em>only</em> be used for debugging.
    ///
    /// The <c>gameTypes</c> is optional, but can be used to limit which game types the bot is capable of
    /// participating in.
    /// </example>
    /// <param name="filePath">Is the file path, e.g. "bot-settings.json</param>
    /// <param name="basePath">Is the base path, e.g. <c>Directory.GetCurrentDirectory()</c>.
    /// If null, the current directory will automatically be used as base path</param>
    /// <returns> A BotInfo instance containing the bot properties read from the configuration.</returns>
    public static BotInfo FromFile(string filePath, string basePath = null)
    {
        basePath ??= Directory.GetCurrentDirectory();
        var configBuilder = new ConfigurationBuilder().SetBasePath(basePath).AddJsonFile(filePath);
        var config = configBuilder.Build();

        return FromConfiguration(config);
    }

    /// <summary>
    /// Reads the bot info from a configuration.
    ///
    /// See <see cref="FromFile(String, String)"/> for an example file.
    /// </summary>
    /// <param name="configuration">Is the configuration</param>
    /// <returns> A BotInfo instance containing the bot properties read from the configuration.</returns>
    public static BotInfo FromConfiguration(IConfiguration configuration)
    {
        var name = configuration["name"];
        ThrowExceptionIfFieldIsBlank("name" , name);

        var version = configuration["version"];
        ThrowExceptionIfFieldIsBlank("version", version);

        var authors = configuration.GetSection("authors").Get<string[]>();
        ThrowExceptionIfJsonFieldIsNullOrEmpty("authors", authors);

        var gameTypes = configuration["gameTypes"];

        var countryCodes = configuration["countryCodes"] ?? "";
        return new BotInfo(
            name,
            version,
            authors,
            configuration["description"],
            configuration["url"],
            configuration.GetSection("countryCodes").Get<string[]>() ?? Array.Empty<string>(),
            configuration.GetSection("countryCodes").Get<string[]>().ToHashSet(),
            configuration["platform"],
            configuration["programmingLang"],
            InitialPosition.FromString(configuration["initialPosition"])
        );
    }

    private static string ToNullIfBlankElseTrim(string value)
    {
        return value == null ? null : string.IsNullOrWhiteSpace(value) ? null : value.Trim();
    }

    private static void ThrowExceptionIfFieldIsBlank(string fieldName, string value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            throw new ArgumentException($"The required JSON field '{fieldName}' is missing or blank");
        }
    }

    private static void ThrowExceptionIfJsonFieldIsNullOrEmpty(string fieldName, IList<string> value)
    {
        if (value == null || value.IsNullOrEmptyOrContainsOnlyBlanks())
        {
            throw new ArgumentException($"The required JSON field '{fieldName}' is missing or empty");
        }
    }


    /// <summary>
    /// Builder interface for providing a builder for building <see cref="BotInfo"/> objects, and which supports method
    /// chaining.
    /// </summary>
    public interface IBuilder
    {
        /// <summary>
        /// Builds and returns the <see cref="BotInfo"/> instance based on the data set and added to this builder so far.
        /// This method is typically the last method to call on the builder in order to extract the result of building.
        /// </summary>
        /// <returns>A <see cref="BotInfo"/> instance.</returns>
        BotInfo Build();

        /// <summary>
        /// Copies all fields from a <see cref="BotInfo"/> instance into this builder.
        /// </summary>
        /// <param name="botInfo">The <see cref="BotInfo"/> instance to copy.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        IBuilder Copy(BotInfo botInfo);

        /// <summary>
        /// Sets the bot name. (required)
        ///
        /// Note that the maximum length of the name is <see cref="MaxNameLength"/> characters.
        /// </summary>
        /// <example>"Rampage"</example>
        /// <param name="name">The name of the bot.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        IBuilder SetName(string name);

        /// <summary>
        /// Sets the bot version. (required)
        ///
        /// Note that the maximum length of the version is <see cref="MaxVersionLength"/> characters.
        /// </summary>
        /// <example>"1.0"</example>
        /// <param name="version">The version of the bot.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        IBuilder SetVersion(string version);

        /// <summary>
        /// Sets the names(s) of the author(s) of the bot. (required)
        ///
        /// Note that the maximum length of an author name is <see cref="MaxAuthorLength"/> characters, and the maximum
        /// number of names is <see cref="MaxNumberOfAuthors"/>.
        /// </summary>
        /// <example>"John Doe"</example>
        /// <param name="authors">A list containing the names(s) of the author(s). A <c>null</c> removes all authors.
        /// </param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        /// <seealso cref="AddAuthor"/>
        IBuilder SetAuthors(IEnumerable<string> authors);

        /// <summary>
        /// Adds an author of the bot. (required)
        ///
        /// See <see cref="SetAuthors"/> for more details.
        /// </summary>
        /// <param name="author">The name of an author to add.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        /// <seealso cref="SetAuthors"/>
        IBuilder AddAuthor(string author);

        /// <summary>
        /// Sets a short description of the bot. (optional)
        ///
        /// Note that the maximum length of the description is <see cref="BotInfo.MaxDescriptionLength"/> characters.
        /// Line-breaks (line-feed / new-line character) are supported, but only expect up to 3 lines to be displayed on
        /// a UI.
        /// </summary>
        /// <example>
        /// <code>
        /// "The rampage bot will try to ram bots that are very close.\n
        /// Sneaks around the corners and shoot at the bots that come too near."
        /// </code>
        /// </example>
        /// <param name="description">A short description of the bot.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        IBuilder SetDescription(string description);

        /// <summary>
        /// Sets a link to the homepage for the bot. (optional)
        ///
        /// Note that the maximum length of a link is <see cref="MaxHomepageLength"/> characters.
        /// </summary>
        /// <example>"https://fictive-homepage.net/Rampage"</example>
        /// <param name="homepage">A link to a homepage for the bot.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        IBuilder SetHomepage(string homepage);

        /// <summary>
        /// Sets the country codes for the bot. (optional)
        /// 
        /// Note that the maximum length of each country code is 2 (alpha-2) from the ISO 3166 international standard,
        /// and the maximum number of country codes is <see cref="BotInfo.MaxNumberOfCountryCodes"/>.
        /// </summary>
        /// <example>"dk"</example>
        /// <note>
        /// Note that if no country code is specified, or the none of the country codes provided is valid, then the
        /// default a list containing a single country code will automatically be used containing the current locale
        /// country code. The current local country code will be extracted using <c>Thread.CurrentThread.CurrentCulture</c>
        /// and <c>RegionInfo(cultureInfo.LCID)</c>.
        /// </note>
        /// <param name="countryCodes">A list containing the country codes. A <c>null</c> removes all country codes.
        /// </param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        /// <seealso cref="AddCountryCode"/>
        IBuilder SetCountryCodes(IEnumerable<string> countryCodes);

        /// <summary>
        /// Adds a country code for the bot. (optional)
        ///
        /// See <see cref="SetCountryCodes"/> for more details.
        /// </summary>
        /// <param name="countryCode">The country code to add.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        /// <seealso cref="SetCountryCodes"/>
        IBuilder AddCountryCode(string countryCode);

        /// <summary>
        /// Sets the game types that this bot is capable of participating in. (required)
        ///
        /// The standard game types <a href="https://robocode-dev.github.io/tank-royale/articles/game_types.html">
        /// are listed here</a>
        ///
        /// Note that more game types might be added in the future.
        ///
        /// The <see cref="GameType"/> class contains the string for the current predefined game types, which can be used
        /// when setting the game types of this method.
        ///
        /// Note that the maximum length of a game type is <see cref="BotInfo.MaxGameTypeLength"/>, and the maximum
        /// number of game types is <see cref="BotInfo.MaxNumberOfGameTypes"/>.
        /// </summary>
        /// <example>Example of game type: "classic"</example>
        /// <example>
        /// Example of usage:
        /// <code>
        /// BotInfo.Builder()
        ///     .SetGameTypes(new List { GameType.Classic, GameType.Melee, "future-type" })
        ///     ...
        /// </code>
        /// </example>
        /// <param name="gameTypes">A list of game types that the bot is capable of participating in. A <c>null</c>
        /// removes all game types.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        /// <seealso cref="AddGameType"/>
        IBuilder SetGameTypes(ICollection<string> gameTypes);

        /// <summary>
        /// Adds a game type that this bot is capable of participating in. (required)
        ///
        /// See <see cref="SetGameTypes"/> for more details.
        /// </summary>
        /// <example>
        /// <code>
        /// BotInfo.Builder()
        ///     .AddGameType(GameType.Classic)
        ///     .AddGameType(GameType.Melee)
        ///     ...
        /// </code>
        /// </example>
        /// <param name="gameType">A game type that the bot is capable of participating in.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        /// <seealso cref="SetGameTypes"/>
        IBuilder AddGameType(string gameType);

        /// <summary>
        /// Sets the name of the platform that this bot is build for. (optional)
        ///
        /// Note that the maximum length of the name of the platform is <see cref="BotInfo.MaxPlatformLength"/>.
        ///
        /// If the platform is set to <c>null</c> or a blank string, then this default string will be used based on this
        /// code: <c>Assembly.GetEntryAssembly()?.GetCustomAttribute&lt;TargetFrameworkAttribute&gt;()?.FrameworkName</c>.
        /// </summary>
        /// <param name="platform">The name of the platform that this bot is build for.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        IBuilder SetPlatform(string platform);

        /// <summary>
        /// Sets the name of the programming language used for developing this bot. (optional)
        ///
        /// Note that the maximum length of the name of the programming language is
        /// <see cref="BotInfo.MaxProgrammingLangLength"/>.
        /// </summary>
        /// <param name="programmingLang">The name of the programming language used for developing this bot.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        IBuilder SetProgrammingLang(string programmingLang);

        /// <summary>
        /// Sets the initial position of this bot. (optional)
        ///
        /// Note that initial positions must be enabled/allowed with the game (server) in order to take effect.
        /// </summary>
        /// <param name="initialPosition">The initial position of this bot.</param>
        /// <returns>This <see cref="BotInfo"/> instance provided for method chaining.</returns>
        IBuilder SetInitialPosition(InitialPosition initialPosition);
    }

    private sealed class BuilderImpl : IBuilder
    {
        private string name;
        private string version;
        private IList<string> authors = new List<string>();
        private string description;
        private string homepage;
        private IList<string> countryCodes = new List<string>();
        private ICollection<string> gameTypes = new HashSet<string>();
        private string platform;
        private string programmingLang;
        private InitialPosition initialPosition;

        public BotInfo Build()
        {
            return new BotInfo(name, version, authors, description, homepage, countryCodes, gameTypes, platform,
                programmingLang, initialPosition);
        }

        public IBuilder Copy(BotInfo botInfo)
        {
            name = botInfo.Name;
            version = botInfo.Version;
            authors = botInfo.Authors.ToList();
            description = botInfo.Description;
            homepage = botInfo.Homepage;
            countryCodes = botInfo.CountryCodes.ToList();
            gameTypes = botInfo.GameTypes;
            platform = botInfo.Platform;
            programmingLang = botInfo.ProgrammingLang;
            initialPosition = botInfo.InitialPosition;
            return this;
        }

        public IBuilder SetName(string newName)
        {
            name = newName;
            return this;
        }

        public IBuilder SetVersion(string newVersion)
        {
            version = newVersion;
            return this;
        }

        public IBuilder SetAuthors(IEnumerable<string> newAuthors)
        {
            authors = ToMutableList(newAuthors);
            return this;
        }

        public IBuilder AddAuthor(string newAuthor)
        {
            authors.Add(newAuthor);
            return this;
        }

        public IBuilder SetDescription(string newDescription)
        {
            description = newDescription;
            return this;
        }

        public IBuilder SetHomepage(string newHomepage)
        {
            homepage = newHomepage;
            return this;
        }

        public IBuilder SetCountryCodes(IEnumerable<string> newCountryCodes)
        {
            countryCodes = ToMutableList(newCountryCodes);
            return this;
        }

        public IBuilder AddCountryCode(string newCountryCode)
        {
            countryCodes.Add(newCountryCode);
            return this;
        }

        public IBuilder SetGameTypes(ICollection<string> newGameTypes)
        {
            gameTypes = ToMutableSet(newGameTypes);
            return this;
        }

        public IBuilder AddGameType(string newGameType)
        {
            gameTypes.Add(newGameType);
            return this;
        }

        public IBuilder SetPlatform(string newPlatform)
        {
            platform = newPlatform;
            return this;
        }

        public IBuilder SetProgrammingLang(string newProgrammingLang)
        {
            programmingLang = newProgrammingLang;
            return this;
        }

        public IBuilder SetInitialPosition(InitialPosition newInitialPosition)
        {
            initialPosition = newInitialPosition;
            return this;
        }
    }
}