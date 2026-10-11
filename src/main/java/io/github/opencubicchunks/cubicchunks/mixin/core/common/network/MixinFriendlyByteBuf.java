package io.github.opencubicchunks.cubicchunks.mixin.core.common.network;

import io.github.opencubicchunks.cubicchunks.network.WirePositions;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** On a server without the mod, block positions go over the network in vanilla's layout (see {@link WirePositions}). */
@Mixin(FriendlyByteBuf.class)
public abstract class MixinFriendlyByteBuf {
    @Inject(method = "readBlockPos(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/core/BlockPos;", at = @At("HEAD"), cancellable = true)
    private static void cc_readVanillaBlockPos(ByteBuf buf, CallbackInfoReturnable<BlockPos> cir) {
        if (WirePositions.vanillaPeer()) cir.setReturnValue(WirePositions.fromVanilla(buf.readLong()));
    }

    @Inject(method = "writeBlockPos(Lio/netty/buffer/ByteBuf;Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"), cancellable = true)
    private static void cc_writeVanillaBlockPos(ByteBuf buf, BlockPos pos, CallbackInfo ci) {
        if (WirePositions.vanillaPeer()) {
            buf.writeLong(WirePositions.toVanilla(pos));
            ci.cancel();
        }
    }
}
