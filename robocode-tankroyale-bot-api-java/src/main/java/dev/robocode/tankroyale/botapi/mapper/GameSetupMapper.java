package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.GameSetup;

/** Utility class for mapping a game setup. */
public final class GameSetupMapper {

  public static GameSetup map(final dev.robocode.tankroyale.schema.GameSetup source) {
    return new GameSetup(
        source.getGameType(),
        source.getArenaWidth(),
        source.getArenaHeight(),
        source.getNumberOfRounds(),
        source.getGunCoolingRate(),
        source.getMaxInactivityTurns(),
        source.getTurnTimeout(),
        source.getReadyTimeout());
  }
}
