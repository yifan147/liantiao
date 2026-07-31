# DoubleJump

A Paper 1.21.1 server plugin that adds double jump functionality.

## Features

- Players can perform a second jump while in mid-air
- Double jump is disabled when sneaking (shift)
- Resets automatically when landing on the ground
- Not available in Creative or Spectator mode

## Usage

1. Drop the built `DoubleJump-1.0.0.jar` into your server's `plugins/` folder
2. Restart the server
3. Players can double jump by pressing the jump key again while falling

## Building

```bash
./gradlew build
```

The built jar will be at `build/libs/DoubleJump-1.0.0.jar`.