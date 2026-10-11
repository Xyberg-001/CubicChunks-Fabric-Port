package io.github.opencubicchunks.cubicchunks.network;

import net.minecraft.core.BlockPos;

/**
 * Block positions as they cross the network. The mod packs them with more bits for Y than vanilla (see MixinBlockPos), in every world, and
 * the network sends them packed (FriendlyByteBuf.readBlockPos/writeBlockPos, which every block position codec goes through). A server
 * without the mod packs them as vanilla, so while this client is connected to one it reads and writes them in vanilla's layout (x 26 bits,
 * z 26, y 12), and its own packing stays as it is for everything else.
 */
public final class WirePositions {
    private static final int VANILLA_HORIZONTAL_BITS = 26;
    private static final int VANILLA_Y_BITS = 12;
    private static final long VANILLA_HORIZONTAL_MASK = (1L << VANILLA_HORIZONTAL_BITS) - 1;
    private static final long VANILLA_Y_MASK = (1L << VANILLA_Y_BITS) - 1;
    private static final int VANILLA_X_OFFSET = VANILLA_Y_BITS + VANILLA_HORIZONTAL_BITS;
    private static final int VANILLA_Z_OFFSET = VANILLA_Y_BITS;

    private static volatile boolean vanillaPeer;

    private WirePositions() {}

    /** Whether the connection this game is on packs block positions as vanilla (a server without the mod). */
    public static boolean vanillaPeer() {
        return vanillaPeer;
    }

    public static void setVanillaPeer(boolean vanilla) {
        vanillaPeer = vanilla;
    }

    public static long toVanilla(BlockPos pos) {
        return ((long) pos.getX() & VANILLA_HORIZONTAL_MASK) << VANILLA_X_OFFSET
                | ((long) pos.getZ() & VANILLA_HORIZONTAL_MASK) << VANILLA_Z_OFFSET
                | (long) pos.getY() & VANILLA_Y_MASK;
    }

    public static BlockPos fromVanilla(long packed) {
        int x = (int) (packed >> VANILLA_X_OFFSET);
        int y = (int) (packed << (64 - VANILLA_Y_BITS) >> (64 - VANILLA_Y_BITS));
        int z = (int) (packed << (64 - VANILLA_Z_OFFSET - VANILLA_HORIZONTAL_BITS) >> (64 - VANILLA_HORIZONTAL_BITS));
        return new BlockPos(x, y, z);
    }
}
