package io.github.opencubicchunks.cubicchunks.gametest;

import java.util.concurrent.CompletableFuture;

import io.github.xyberg001.cubicworlds.api.CubeGenerator;
import io.github.xyberg001.cubicworlds.api.CubeTerrain;
import io.github.xyberg001.cubicworlds.api.CubicApi;
import io.github.opencubicchunks.cubicchunks.world.level.PlaceholderTerrain;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import javax.annotation.Nullable;

/**
 * The tests' cubic terrain: Cubic Chunks' placeholder stone hills, for the superflat worlds the test world builder makes. Cubic Chunks makes
 * only worlds whose type has a cube generator cubic (CubicApi.supportsCubes), so the tests register this one for superflat, through the
 * API a mod uses (it never ships).
 */
final class PlaceholderCubeGenerator implements CubeGenerator {
    private static boolean registered;

    static void register() {
        if (!registered) {
            registered = true;
            CubicApi.registerCubeGenerator(FlatLevelSource.class, (level, chunkGenerator) -> new PlaceholderCubeGenerator());
        }
    }

    @Override public CompletableFuture<?> generate(CubeTerrain cube) {
        BlockState stone = PlaceholderTerrain.block();
        for (int x = cube.minX(); x < cube.minX() + CubeTerrain.SIZE; x++) {
            for (int z = cube.minZ(); z < cube.minZ() + CubeTerrain.SIZE; z++) {
                int top = Math.min(cube.maxY(), PlaceholderTerrain.surfaceY(x, z));
                if (top >= cube.minY()) {
                    cube.fill(x, cube.minY(), top, z, stone);
                }
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override public @Nullable Surface surface(int x, int z) {
        BlockState stone = PlaceholderTerrain.block();
        return new Surface(PlaceholderTerrain.surfaceY(x, z), stone, stone, Integer.MIN_VALUE, null);
    }
}
