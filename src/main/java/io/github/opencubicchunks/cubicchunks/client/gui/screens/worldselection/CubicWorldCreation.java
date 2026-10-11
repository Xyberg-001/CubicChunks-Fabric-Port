package io.github.opencubicchunks.cubicchunks.client.gui.screens.worldselection;

import io.github.opencubicchunks.cubicchunks.world.CubicWorldSettings;

/**
 * The world creation screen's Cubic Chunks choices (on its WorldCreationUiState, see MixinWorldCreationUiState): whether the new world is
 * cubic and the heights it holds. They start from the config.
 */
public interface CubicWorldCreation {
    boolean cc_isCubic();

    void cc_setCubic(boolean cubic);

    int cc_getMinY();

    void cc_setMinY(int minY);

    int cc_getMaxY();

    void cc_setMaxY(int maxY);

    /** Whether the chosen world type can be cubic (a cube generator is registered for its overworld; see CubicApi.supportsCubes). */
    default boolean cc_supportsCubes() {
        try {
            return io.github.xyberg001.cubicworlds.api.CubicApi.supportsCubes(
                    ((net.minecraft.client.gui.screens.worldselection.WorldCreationUiState) this).getSettings().selectedDimensions().overworld());
        } catch (IllegalStateException e) {
            return false; // no overworld
        }
    }

    /** Whether the new world will be cubic: chosen, and its world type can be. */
    default boolean cc_willBeCubic() {
        return this.cc_isCubic() && this.cc_supportsCubes();
    }

    /** The settings the new world is made with: the choices, with the heights brought within what the game can hold. */
    default CubicWorldSettings cc_settings() {
        return new CubicWorldSettings(this.cc_willBeCubic(), this.cc_getMinY(), this.cc_getMaxY()).clamped();
    }
}
