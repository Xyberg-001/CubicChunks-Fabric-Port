# Cubic Chunks Fabric Port

A port of [Cubic Chunks 3](https://github.com/OpenCubicChunks/CubicChunks3) to Fabric, for Minecraft 26.3 (branch `fabric-26.3`).

Vanilla stores a world in chunks that span its whole height, so a world can only be as tall as every chunk is. Cubic Chunks stores it in
32 x 32 x 32 cubes instead, and loads them around players in three dimensions: a world can hold tens of thousands of blocks of height,
and only the cubes near a player are in memory, whatever the height.

**It is an engine, not a world type.** Cubic Chunks loads, generates, lights, saves, sends and renders cubes; the terrain in them comes from
the mod that owns the world type, through a small API. A world can be cubic only if its world type has registered a cube generator; for any
other world type the Cubic Chunks switch on the world creation screen is greyed out, and a server makes such a world vanilla (with a line
in its log saying why).

## What it does

- **Only the overworld is cubic.** The Nether, the End and dimensions added by other mods always keep vanilla's chunks and vanilla's
  heights, in a cubic world too: their generators make chunks, not cubes.
- **Cubes in three dimensions:** loading, generation, saving (region files of cubes), lighting (sky light through the full height),
  networking, rendering, entities, mob spawning, random and scheduled ticks all work per cube. `verticalViewDistance` in
  `config/cubicchunks_common.toml` sets how many cubes up and down a player sees.
- **Per world:** each world is cubic or not, and has its own lowest and highest Y, chosen when it is made (the **Cubic Chunks** tab on
  the world creation screen; on a server, `generateNewWorldsAsCC`, `newWorldMinY` and `newWorldMaxY`) and kept in
  `<world>/cubicchunks/world.toml`. Worlds made without it stay ordinary worlds.
- **Height:** `heightLimit` in `config/cubicchunks_common.toml`. Block positions are packed into 64 bits everywhere, so height and
  horizontal reach trade off: 32768 (the default) holds about Y -32,400 to 32,400 and reaches about 8.4 million blocks out on x and z;
  8192 holds about Y -7,900 to 7,900 and reaches about 16.7 million. A new world holds Y -11,000 to 9,000 unless chosen otherwise.
- **Servers:** with Cubic Chunks installed you can still join servers that don't run it (their worlds play as vanilla). A server that runs it
  needs every player to have it, with the same `heightLimit`.
- **Works with** Sodium, Iris, Voxy and Distant Horizons. Voxy holds Y -4096 to 4095 of a cubic world and Distant Horizons a 4096-block
  window (`distantHorizonsMinY`), so terrain beyond those is missing from their far view.

## For players

Download the jar from [Releases](https://github.com/Xyberg-001/CubicChunks-Fabric-Port/releases) and install it with Fabric API, next to
a mod whose world type supports cubes. Create a world of that type and switch Cubic Chunks on in its tab: its overworld becomes cubic,
while the Nether and the End stay as they always are. It is a beta: keep backups of worlds you care about.

## For mod developers

Your mod keeps its own world type and chunk generator; Cubic Chunks asks it for the terrain of each cube.

**1. Compile against the API.** The API jar (`cubicchunks-<version>-api.jar`, attached to each release, or `./gradlew apiJar`) holds only
the `io.github.opencubicchunks.cubicchunks.api` package, which is the public API; everything else in Cubic Chunks may change at any time.

```groovy
dependencies {
    modCompileOnly files("libs/cubicchunks-1.0.0+26.3-api.jar")
}
```

In `fabric.mod.json`, `"suggests": { "cubicchunks": "*" }` keeps Cubic Chunks optional (or put it under `depends` if your mod needs it).

**2. Register a cube generator** for your chunk generator's class at startup. Only the overworld is cubic, so it is your world type's
overworld generator that counts: a world is cubic only when its overworld's chunk generator has a cube generator registered, and the
levels of every other dimension (the Nether, the End, your own) stay vanilla chunks, with `CubicApi.isCubic` false for them. Keep the
Cubic Chunks classes in a class of their own that is only touched when the mod is there, so your mod still loads without it:

```java
// in your ModInitializer
if (FabricLoader.getInstance().isModLoaded("cubicchunks")) MyCubicSupport.register();

final class MyCubicSupport {
    static void register() {
        CubicApi.registerCubeGenerator(MyChunkGenerator.class,
                (level, chunkGenerator) -> new MyCubeGenerator((MyChunkGenerator) chunkGenerator));
    }
}
```

**3. Fill cubes.** `generate` gets one 32 x 32 x 32 cube at a time, on a world generation thread. Positions are world positions; writes
outside the cube are ignored, so you can paint whole columns and keep what falls inside:

```java
final class MyCubeGenerator implements CubeGenerator {
    private final MyChunkGenerator terrain;

    MyCubeGenerator(MyChunkGenerator terrain) {
        this.terrain = terrain;
    }

    @Override
    public CompletableFuture<?> generate(CubeTerrain cube) {
        BlockState stone = Blocks.STONE.defaultBlockState();
        for (int x = cube.minX(); x < cube.minX() + CubeTerrain.SIZE; x++) {
            for (int z = cube.minZ(); z < cube.minZ() + CubeTerrain.SIZE; z++) {
                int ground = terrain.groundHeight(x, z);       // your own height function
                cube.fill(x, cube.minY(), ground, z, stone);   // clipped to the cube
            }
        }
        // biomes: cube.fillBiomes(resolver, sampler) with your biome source's resolver
        return CompletableFuture.completedFuture(null);        // or a future that completes later (after a download)
    }
}
```

### API reference

**`CubeGenerator`** (implemented by your mod)

| Method | What it is for |
|---|---|
| `generate(CubeTerrain)` | The cube's terrain and biomes. Must not touch other cubes. The returned future may complete later, on any thread. |
| `decorate(CubeTerrain)` | Trees, ores, buildings, block entities and entities, once the cubes around it have their terrain. Writes only its own cube: something crossing cubes (a tree on an edge) is made by each cube keeping its own part, so the answer must be the same for every cube asked. |
| `usesStructures()` | `true` to have vanilla structure starts and references worked out per column with your chunk generator (so `/locate`, maps and structure mob spawns work). You place the structures' blocks yourself in `decorate`, reading the starts with `CubicApi.column`. |
| `surface(x, z)` | What a column looks like from afar (surface Y and blocks, water), for Distant Horizons. `null` if you can't tell without generating. Must be thread safe. |

**`CubeTerrain`** (the cube being generated): `minX/minY/minZ/maxY`, `setBlock`, `fill` (part of a column), `markForPostProcessing`
(stairs, fences and walls fitted to their neighbours later), `setBlockEntity`, `addEntity`, `scheduleBlockTick`, `scheduleFluidTick`,
`fillBiomes`. Writes go straight into the cube's sections, without neighbour updates or light.

**`CubicApi`**

| Method | What it does |
|---|---|
| `isCubic(level)` | Whether a level is cubic. |
| `minY(level)`, `maxY(level)` | The heights a level's world holds. Use these rather than `Level.getMinY/getMaxY`, which report the dimension type's. |
| `borderReach()` | How far x and z reach with Cubic Chunks installed (in every world). |
| `supportsCubes(chunkGenerator)` | Whether worlds of a chunk generator can be cubic (a cube generator is registered for it). |
| `cubeGenerator(level)`, `usesStructures(level)` | A cubic level's cube generator, and whether it places structures. |
| `column(level, chunkX, chunkZ, status)` | A column (it holds the structure starts and references) if it has reached the status; never waits. |
| `addCubeListener(listener)` | Told when cubes become full (their blocks can be read) and when they unload, on the server thread. |
| `isCubeLoaded`, `isCubeEmpty` | Whether a cube is loaded and full, or all air; from any thread, never waiting. |
| `readCube(level, x, y, z)` | A cube's blocks from memory or disk (waits for the disk, so not on the server thread). |
| `loadCube`, `releaseCube` | Load (and generate) a cube and hold it with a ticket, as a pre-generation sweep does; server thread. |
| `pendingCubeWrites(level)` | Cube and column writes still queued for the disk. |

**`CubicClientApi.isNewWorldCubic(state)`**: whether the world being set up on the world creation screen will be cubic, so your own world
settings can follow the Cubic Chunks tab.

## Building

JDK 25. Clone with `--recursive` (the `CubicChunksCore` submodule), then `./gradlew jar` (the mod) and `./gradlew apiJar` (the API jar);
both land in `build/libs`. `./gradlew runClientGameTest` runs the client game tests.

---

*The upstream project's README follows.*

## Not yet usable or functional, don't try.

Rewrite of the previous rewrite, targeting NeoForge/MC 1.21.6. 

This Minecraft mod extends Minecraft height and depth to be nearly infinite (at least a million blocks).

For the most up-to-date information about this mod and its related mods, as well as the newest downloads, please join us on the [**Cubic Chunks Discord**](https://discord.gg/kMfWg9m).

### Cubic Chunks (CC) - Links:

Github - [Cubic Chunks - 1.21.6 and above](https://github.com/OpenCubicChunks/CubicChunks3)  
Github - [Cubic Chunks - 1.12.2 and lower](https://github.com/OpenCubicChunks/CubicChunks)

### Cloning the repository

Note: you need git installed to do the following:
```
git clone --recursive
```
You need a git submodule for the project to compile.
If you don't yet have the submodule but already cloned the repository:
```
git submodule update --init --recursive
```

To get latest version of the submodule:
```
git submodule update --recursive --remote
```

### .git-blame-ignore-revs
Configure commits to be ignored for git blame:

```shell
git config blame.ignoreRevsFile .git-blame-ignore-revs
```

### Contributing

#### PR Guidelines
- All mixin methods and fields _**must**_ have a `cc_` prefix, except for those in test mixins. // todo automate this check in gh workflows.
- If a class is significantly modified with mixins, it _**must**_ have documentation explaining:
	- The original vanilla behaviour, can mention important fields/methods.
		- If the vanilla class has sufficient javadoc (through parchment), this can be skipped. Our javadoc should link to the parchment javadoc.
	- The goal of all mixins targeting the class.
- Any merged mixins _**must**_ have tests associated with them. 
	- If a mixin is "untestable" its test class should have a comment explaining *why* it's untestable. 
		- Optionally a to-do (project task? issue?) suggesting integration tests when possible.
	- All non-trivial mixins _**must**_ have a comment explaining their purpose.
	-  //todo Investigate code coverage for mixin tests ([jacoco?](https://docs.gradle.org/current/userguide/jacoco_plugin.html) [other link maybe it's bad](https://igorski.co/generating-junit-test-coverage-using-gradle-and-jacoco/)) .
- Any non-mixin class _**must**_ have tests associated with it.
	- The tests should reasonably cover all expected usage of the class (its external api).
	- Any method(s) that can be reasonably unit tested _**must**_ be.
- Anything annotated with `@SuppressWarnings` _**must**_ have either a TODO comment, or a comment explaining why the warning is suppressed.
- _**Must**_ pass spotless check and checkstyle. Spotless formats automatically if you run `./gradlew spotlessApply`; checkstyle violations must be fixed manually.
- _**Must**_ build.
- All tests _**must**_ pass (no regressions).
(TODO more contributing docs)
