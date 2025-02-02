﻿using System;
using System.Text.RegularExpressions;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Color represented in RGB format.
/// </summary>
/// <see href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</see>
public class Color
{
    private const string NumericRgb = "^#[0-9a-fA-F]{3,6}$";

    private const string ThreeHexDigits = "^[0-9a-fA-F]{3}$";
    private const string SixHexDigits = "^[0-9a-fA-F]{6}$";

    public static readonly Color White = FromHex("FFFFFF");
    public static readonly Color Silver = FromHex("C0C0C0");
    public static readonly Color Gray = FromHex("808080");
    public static readonly Color Black = FromHex("000000");
    public static readonly Color Red = FromHex("FF0000");
    public static readonly Color Maroon = FromHex("800000");
    public static readonly Color Yellow = FromHex("FFFF00");
    public static readonly Color Olive = FromHex("808000");
    public static readonly Color Lime = FromHex("00FF00");
    public static readonly Color Green = FromHex("008000");
    public static readonly Color Cyan = FromHex("00FFFF");
    public static readonly Color Teal = FromHex("008080");
    public static readonly Color Blue = FromHex("0000FF");
    public static readonly Color Navy = FromHex("000080");
    public static readonly Color Fuchsia = FromHex("FF00FF");
    public static readonly Color Purple = FromHex("800080");
    public static readonly Color Orange = FromHex("FF8000");

    /// <summary>
    /// Creates a Color from RGB values.
    /// </summary>
    /// <param name="redValue">The red color component of the RGB color in the range [0 - 255]</param>
    /// <param name="greenValue">The green color component of the RGB color in the range [0 - 255]</param>
    /// <param name="blueValue">The red blue component of the RGB color in the range [0 - 255]</param>
    /// <exception cref="ArgumentException"/>
    /// <see href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</see>
    ///
    public Color(int redValue, int greenValue, int blueValue)
    {
        if (redValue is < 0 or > 255)
        {
            throw new ArgumentException("The 'red' color component must be in the range 0 - 255");
        }

        if (greenValue is < 0 or > 255)
        {
            throw new ArgumentException("The 'green' color component must be in the range 0 - 255");
        }

        if (blueValue is < 0 or > 255)
        {
            throw new ArgumentException("The 'blue' color component must be in the range 0 - 255");
        }

        RedValue = redValue;
        GreenValue = greenValue;
        BlueValue = blueValue;
    }

    /// <summary>
    /// The red color component of the color.
    /// </summary>
    /// <value>The red color component of the RGB color in the range [0 - 255]</value>
    public int RedValue { get; }

    /// <summary>
    /// The blue color component of the color.
    /// </summary>
    /// <value>The blue color component of the RGB color in the range [0 - 255]</value>
    public int GreenValue { get; }

    /// <summary>
    /// The green color component of the color.
    /// </summary>
    /// <value>The green color component of the RGB color in the range [0 - 255]</value>
    public int BlueValue { get; }

    /// <summary>
    /// Returns the color as a hex triplet of six hexadecimal digits representing an RGB color, e.g. "0099CC".
    /// </summary>
    /// <returns>The color as a hex triplet of six hexadecimal digits representing an RGB color, e.g. "0099CC".</returns>
    public string ToHex()
    {
        return ToHex(RedValue) + ToHex(GreenValue) + ToHex(BlueValue);
    }

    private static string ToHex(int value)
    {
        return "" + (value >> 4).ToString("X") + (value & 0xF).ToString("X");
    }

    /// <summary>
    /// Creates a color from a string. Currently, only numeric RGB values are supported.
    /// This method works the same was as {@link #fromHex} except that is required as hash sign before the hex value.
    ///
    /// An example of a numeric RGB value is "#09C" or "#0099CC", which both represents the same color.
    /// </summary>
    /// <param name="str">A string containing either a three or six hexadecimal RGB values like "#09C" or "#0099CC".
    /// </param>
    /// <returns>The created Color; <c>null</c> if the input parameter is <c>null</c>.</returns>
    /// <exception cref="ArgumentException"/>
    /// <see href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</see>
    /// <see href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</see>
    public static Color FromString(string str)
    {
        if (str == null) return null;
        str = str.Trim();
        if (Regex.Match(str, NumericRgb).Success)
        {
            return FromHex(str[1..]);
        }

        throw new ArgumentException(
            "You must supply the string in numeric RGB format #[0-9a-fA-F], e.g. \"#09C\" or \"#0099CC\"");
    }

    /// <summary>
    /// Creates a color from a hex triplet. A hex triplet is either three or six hexadecimal digits that represents an
    /// RGB Color.
    ///
    /// An example of a hex triplet is "09C" or "0099CC", which both represents the same color.
    /// </summary>
    /// <param name="hex">A string containing either a three or six hexadecimal numbers like "09C" or "0099CC".</param>
    /// <returns>The created Color.</returns>
    /// <exception cref="ArgumentException"/>
    /// <see href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</see>
    /// <see href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</see>
    public static Color FromHex(string hex)
    {
        hex = hex.Trim();
        if (Regex.Match(hex, ThreeHexDigits).Success)
        {
            return FromThreeHexDigits(hex);
        }

        if (Regex.Match(hex, SixHexDigits).Success)
        {
            return FromSixHexDigits(hex);
        }

        throw new ArgumentException("You must supply 3 or 6 hex digits [0-9a-fA-F]");
    }

    private static Color FromThreeHexDigits(string threeHexDigits)
    {
        var r = int.Parse(threeHexDigits[..1], System.Globalization.NumberStyles.HexNumber);
        var g = int.Parse(threeHexDigits[1..2], System.Globalization.NumberStyles.HexNumber);
        var b = int.Parse(threeHexDigits[2..3], System.Globalization.NumberStyles.HexNumber);
        r = r << 4 | r;
        g = g << 4 | g;
        b = b << 4 | b;
        return new Color(r, g, b);
    }

    private static Color FromSixHexDigits(string sixHexDigits)
    {
        var r = int.Parse(sixHexDigits[..2], System.Globalization.NumberStyles.HexNumber);
        var g = int.Parse(sixHexDigits[2..4], System.Globalization.NumberStyles.HexNumber);
        var b = int.Parse(sixHexDigits[4..6], System.Globalization.NumberStyles.HexNumber);
        return new Color(r, g, b);
    }

    private bool Equals(Color other)
    {
        return RedValue == other.RedValue && GreenValue == other.GreenValue && BlueValue == other.BlueValue;
    }

    public override bool Equals(object obj)
    {
        if (ReferenceEquals(null, obj)) return false;
        if (ReferenceEquals(this, obj)) return true;
        return obj.GetType() == GetType() && Equals((Color)obj);
    }

    public override int GetHashCode()
    {
        return HashCode.Combine(RedValue, GreenValue, BlueValue);
    }

    public override string ToString()
    {
        return ToHex();
    }
}