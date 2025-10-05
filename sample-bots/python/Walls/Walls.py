import asyncio

from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitBotEvent


# ------------------------------------------------------------------
# Walls
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# This robot navigates around the perimeter of the battlefield with
# the gun pointed inward.
# ------------------------------------------------------------------
class Walls(Bot):
    def __init__(self) -> None:
        super().__init__()
        self._peek: bool = False  # Don't turn if there's a bot there
        self._move_amount: float = 0.0  # How much to move

    async def run(self) -> None:
        # Set colors
        self.body_color = Color.BLACK
        self.turret_color = Color.BLACK
        self.radar_color = Color.ORANGE
        self.bullet_color = Color.CYAN
        self.scan_color = Color.CYAN

        # Initialize move_amount to the maximum possible for the arena
        self._move_amount = max(self.get_arena_width(), self.get_arena_height())
        # Initialize peek to false
        self._peek = False

        # Turn to face a wall
        await self.turn_right(self.get_direction() % 90)
        await self.forward(self._move_amount)

        # Turn the gun to turn right 90 degrees.
        self._peek = True
        await self.turn_gun_left(90)
        await self.turn_left(90)

        # Main loop
        while self.is_running():
            # Peek before we turn when forward() completes.
            self._peek = True
            # Move up the wall
            await self.forward(self._move_amount)
            # Don't peek now
            self._peek = False
            # Turn to the next wall
            await self.turn_left(90)

    async def on_hit_bot(self, e: HitBotEvent) -> None:
        # If he's in front of us, back up a bit; else move ahead a bit.
        bearing = self.bearing_to(float(e.x), float(e.y))
        if -90 < bearing < 90:
            await self.back(100)
        else:
            await self.forward(100)

    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        del e
        await self.fire(2)
        # Ensure we generate another scan event if we're peeking
        if self._peek:
            await self.rescan()


async def main() -> None:
    bot = Walls()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
