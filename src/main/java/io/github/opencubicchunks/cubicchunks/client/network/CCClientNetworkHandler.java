package io.github.opencubicchunks.cubicchunks.client.network;

import com.mojang.logging.LogUtils;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundCubeLightUpdatePacket;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundForgetLevelCloPacket;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelChunkPacket;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelCubeWithLightPacket;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundSetCubeCacheCenterPacket;
import io.github.opencubicchunks.cubicchunks.network.CCPayloadHandler;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundWorldSettingsPacket;
import io.github.opencubicchunks.cubicchunks.network.WirePositions;
import io.github.opencubicchunks.cubicchunks.world.CubicWorldSettings;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.slf4j.Logger;

/** The client's handlers for the mod's packets (Fabric runs them on the client's main thread). */
public class CCClientNetworkHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private CCClientNetworkHandler() {}

    public static void register() {
        receive(CCClientboundLevelCubeWithLightPacket.TYPE, new CCClientboundLevelCubeWithLightPacket.Handler());
        receive(CCClientboundLevelChunkPacket.TYPE, new CCClientboundLevelChunkPacket.Handler());
        receive(CCClientboundForgetLevelCloPacket.TYPE, new CCClientboundForgetLevelCloPacket.Handler());
        receive(CCClientboundSetCubeCacheCenterPacket.TYPE, new CCClientboundSetCubeCacheCenterPacket.Handler());
        receive(CCClientboundCubeLightUpdatePacket.TYPE, new CCClientboundCubeLightUpdatePacket.Handler());
        receiveWorldSettings();
    }

    /**
     * The server's world settings, which the client's levels follow (see CubicWorldSettings), arrive as it is joined. A server with the mod
     * must pack block positions alike (the same heightLimit). A server without it sends none: its world is vanilla, and its block positions
     * are read and written in vanilla's layout (see WirePositions). Positions only travel in the play phase, after the settings would have
     * come.
     */
    private static void receiveWorldSettings() {
        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> {
            CubicWorldSettings.setClient(null);
            WirePositions.setVanillaPeer(true);
        });
        ClientConfigurationNetworking.registerGlobalReceiver(CCClientboundWorldSettingsPacket.TYPE, (payload, context) -> {
            WirePositions.setVanillaPeer(false);
            if (payload.packedYLength() != BlockPos.PACKED_Y_LENGTH) {
                context.responseSender().disconnect(Component.translatable("disconnect.cubicchunks.heightLimit",
                        heightLimit(payload.packedYLength()), heightLimit(BlockPos.PACKED_Y_LENGTH)));
                return;
            }
            CubicWorldSettings.setClient(payload.settings());
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (WirePositions.vanillaPeer()) LOGGER.info("Joined a server without Cubic Chunks: its world is vanilla, block positions go the vanilla way");
        });
        // back to the mod's own packing for what comes next (a singleplayer world opened to LAN sends positions too)
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> WirePositions.setVanillaPeer(false));
        ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> WirePositions.setVanillaPeer(false));
    }

    /** The heightLimit config a game packing Y into this many bits runs with. */
    private static int heightLimit(int packedYLength) {
        return 1 << (packedYLength - 1);
    }

    private static <T extends CustomPacketPayload> void receive(CustomPacketPayload.Type<T> type, CCPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, context.player()));
    }
}
