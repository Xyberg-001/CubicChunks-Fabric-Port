# Modrinth upload kit — Cubic Worlds 1.0.0

Everything to paste into Modrinth's project settings and "Upload version" forms.
The description below is the project body (Modrinth renders Markdown).

## Project form

| Field | Value |
|---|---|
| Name | Cubic Worlds |
| URL (slug) | `cubic-worlds` (free as of 2026-10-12) |
| Summary | Tall overworlds stored in 32-block cubes, continued from Cubic Chunks 3: tens of thousands of blocks tall and deep. An engine for world types that support it. |
| Project type | Mod |
| Categories | Library (primary), Worldgen |
| Client side | Required |
| Server side | Required |
| License | MIT, matching `LICENSE` and `fabric.mod.json` |
| Source code | https://github.com/Xyberg-001/CubicWorlds |
| Issues | https://github.com/Xyberg-001/CubicWorlds/issues |
| External system interactions | None: no internet access, and it reads and writes no files outside the Minecraft folder (its config, and its world data in each world's `cubicchunks` folder). |
| **Contains AI-generated content** | **Tick it.** Modrinth rule 6.1 requires it: a substantial part of the code and this description were produced with an AI assistant. |
| **Contains derivative content** | **Tick it** (required for forks): Cubic Chunks 3, https://github.com/OpenCubicChunks/CubicChunks3; a fork continued under its own name, ported to Minecraft 26.3, with the API and renderer support added; MIT notice and credit kept. |
| Icon | `releases/modrinth/icon.png` (512×512, 220 KiB; limit 256 KiB). The jar carries the same image. |
| Gallery | Real in-game screenshots only (e.g. a cubic world far above or below the vanilla height limit, the Cubic Worlds tab on world creation). |

## Version form

| Field | Value |
|---|---|
| Files | `releases/cubicworlds-fabric-1.0.0+26.3.jar` (primary); `releases/cubicworlds-fabric-1.0.0+26.3-api.jar` (additional file) |
| Version number | `1.0.0` |
| Version title | Cubic Worlds 1.0.0 |
| Release channel | Beta (the release notes call 1.0.0 a beta) |
| Loaders | Fabric |
| Game versions | 26.3 |
| Dependencies | Fabric API — required |

Changelog:

```
The first release of Cubic Worlds, continued from Cubic Chunks 3. Beta: keep backups of
worlds you care about.

- Overworlds up to Y ±32,000, stored and loaded in 32-block cubes (the Nether and the End
  stay vanilla).
- Cubes load around players in all three directions.
- Each world chooses on creation whether it is cubic and its lowest and highest Y (the
  Cubic Worlds tab).
- Lighting, saving, mobs, block ticks and rendering work per cube.
- Works with Sodium, Iris, Voxy and Distant Horizons.
- API for world types: register a cube generator (the -api jar, docs/API.md).
```

---

## Description (paste from here)

**Cubic Worlds** makes the overworld tall. It is stored and loaded in 32-block cubes instead of full-height chunks, so a world can be tens of thousands of blocks tall and deep. Only the overworld is cubic; the Nether and the End stay vanilla.

It is an **engine**: it makes no terrain of its own. The terrain comes from a world type that supports it; vanilla world types stay vanilla.

Continued from [Cubic Chunks 3](https://github.com/OpenCubicChunks/CubicChunks3) (MIT) under its own name; not an official Cubic Chunks project.

### Features
- Overworlds up to Y ±32,000.
- Cubes load around players in all three directions: only those near you are in memory, however tall the world is.
- Each world chooses on creation whether it is cubic and its lowest and highest Y (the **Cubic Worlds** tab).
- Lighting, saving, mobs, block ticks and rendering all work per cube.
- Join servers without Cubic Worlds as usual; a server with it needs every player to have it.
- Works with Sodium, Iris, Voxy and Distant Horizons (their far view holds 4,096 blocks of height).

### Getting started
1. Install Fabric API, Cubic Worlds and a world type that supports it.
2. **Create New World**, pick that world type, and switch on the **Cubic Worlds** tab.

Keep backups of worlds you care about: this is a beta.

### Commands

For operators.

| Command | What it does |
|---|---|
| `/cubicworlds forceload add <pos> [radius]` | Keeps the cubes around a block loaded (the cubic `/forceload`). |
| `/cubicworlds forceload remove <pos> [radius]` | Lets them go again. |
| `/cubicworlds loaded` | How many cubes and columns are loaded. |
| `/cubicworlds light <pos>` | The sky and block light at a block. |
| `/cubicworlds column <x> <z>` | What a column holds. |
| `/cubicworlds blockentities <pos> [radius]` | The block entities in the cubes around a block, by type. |

### Settings

In `config/cubicworlds_common.toml`:

| Setting | Default | What it does |
|---|---|---|
| `heightLimit` | 32768 | Y ±32,000 and 8.4 million blocks out, or 8192 for Y ±7,900 and 16.7 million out. A server and its players must match. |
| `verticalViewDistance` | 8 | How many cubes up and down you see. |
| `newWorldMinY` / `newWorldMaxY` | -11000 / 9000 | The heights a new world gets. |
| `generateNewWorldsAsCC` | false | Whether a server's new world is cubic (and the tab's default). |
| `distantHorizonsMinY` | -2048 | Where the 4,096 blocks Distant Horizons shows begin. |

### For mod developers
Register a cube generator for your world type and Cubic Worlds asks it for each cube's terrain. Compile against the API jar (an additional file on each version). Guide: [docs/API.md](https://github.com/Xyberg-001/CubicWorlds/blob/fabric-26.3/docs/API.md).

### Licence
MIT. Report problems on [GitHub](https://github.com/Xyberg-001/CubicWorlds/issues).
