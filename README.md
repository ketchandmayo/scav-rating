# Scavenger Rating Addon

A Fabric addon for the Scavenger mod that adds global speedrun leaderboards! Compete with players worldwide to get the fastest completion times.

## Features
- **Global Leaderboards**: View the top 10 fastest times for any item and modifier combination.
- **In-Game UI**: Access the leaderboard directly from the game.
- **Anti-Cheat System**: Automatically invalidates runs if a player switches to Creative/Spectator mode or uses operator commands during the run.
- **Localization**: Full support for English and Russian languages.

## Requirements
- Minecraft 1.21.11
- Fabric Loader
- Fabric API
- Scavenger mod

> **Note:** A NeoForge version is coming soon!

## Installation
1. Download the latest release from Modrinth or the [Releases](https://github.com/ketchandmayo/scav-rating/releases) page.
2. Drop the `.jar` file into your `.minecraft/mods` folder.
3. Make sure you also have the original Scavenger mod and Fabric API installed.

## Building from source
```bash
git clone https://github.com/ketchandmayo/scav-rating.git
./gradlew build
```

## Backend Server
This mod communicates with a custom backend server to store leaderboard data. The backend is written in Go and uses SQLite.

## License
GPLv3
