# Cubic Worlds

Tall worlds for Minecraft, continued from Cubic Chunks 3 (Fabric, Minecraft 26.3). The overworld is stored and loaded in 32-block
cubes instead of full-height chunks, so a world can be tens of thousands of blocks tall and deep. It is an engine: the terrain comes from
a world type that supports it.

## Features

- Overworlds up to Y ±32,000 (the Nether and the End stay vanilla).
- Cubes load around players in all three directions: only those near you are in memory, however tall the world is.
- Each world chooses on creation whether it is cubic and its lowest and highest Y (the **Cubic Worlds** tab).
- Lighting, saving, mobs, block ticks and rendering all work per cube.
- Join servers without Cubic Worlds as usual; a server with it needs every player to have it.
- Works with Sodium, Iris, Voxy and Distant Horizons (their far view holds 4,096 blocks of height).

## Commands

For operators.

| Command | What it does |
|---|---|
| `/cubicworlds forceload add <pos> [radius]` | Keeps the cubes around a block loaded (the cubic `/forceload`). |
| `/cubicworlds forceload remove <pos> [radius]` | Lets them go again. |
| `/cubicworlds loaded` | How many cubes and columns are loaded. |
| `/cubicworlds light <pos>` | The sky and block light at a block. |
| `/cubicworlds column <x> <z>` | What a column holds. |
| `/cubicworlds blockentities <pos> [radius]` | The block entities in the cubes around a block, by type. |

## Settings

In `config/cubicworlds_common.toml`:

| Setting | Default | What it does |
|---|---|---|
| `heightLimit` | 32768 | Y ±32,000 and 8.4 million blocks out, or 8192 for Y ±7,900 and 16.7 million out. A server and its players must match. |
| `verticalViewDistance` | 8 | How many cubes up and down you see. |
| `newWorldMinY` / `newWorldMaxY` | -11000 / 9000 | The heights a new world gets. |
| `generateNewWorldsAsCC` | false | Whether a server's new world is cubic (and the tab's default). |
| `distantHorizonsMinY` | -2048 | Where the 4,096 blocks Distant Horizons shows begin. |

## For mod developers

Register a cube generator for your world type and Cubic Worlds asks it for each cube's terrain. Compile against
`cubicworlds-fabric-<version>-api.jar` from [Releases](https://github.com/Xyberg-001/CubicWorlds/releases). Guide and
reference: [docs/API.md](docs/API.md).

---

Continued from [OpenCubicChunks/CubicChunks3](https://github.com/OpenCubicChunks/CubicChunks3) (MIT) under its own name; not an official
Cubic Chunks project. Report problems [here](https://github.com/Xyberg-001/CubicWorlds/issues).
