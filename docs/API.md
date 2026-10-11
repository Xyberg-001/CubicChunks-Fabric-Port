# Cubic Chunks Fabric Port: API for mod developers

Your mod keeps its own world type and chunk generator; Cubic Chunks asks it for the terrain of each cube.

**1. Compile against the API.** The API jar (`cubicchunks-fabric-<version>-api.jar`, attached to each release, or `./gradlew apiJar`) holds only
the `io.github.opencubicchunks.cubicchunks.api` package, which is the public API; everything else in Cubic Chunks may change at any time.

```groovy
dependencies {
    modCompileOnly files("libs/cubicchunks-fabric-1.0.0+26.3-api.jar")
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

## API reference

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

## Building Cubic Chunks

JDK 25. Clone with `--recursive` (the `CubicChunksCore` submodule), then `./gradlew jar` (the mod) and `./gradlew apiJar` (the API jar);
both land in `build/libs`. `./gradlew runClientGameTest` runs the client game tests.
