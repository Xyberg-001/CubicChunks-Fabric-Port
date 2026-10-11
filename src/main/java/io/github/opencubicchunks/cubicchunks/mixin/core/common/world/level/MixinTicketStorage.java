package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.world.level.CubicTicketStorage;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

@Dasm(value = ChunkToCloSet.class, target = @Ref(TicketStorage.class))
@Mixin(TicketStorage.class)
public abstract class MixinTicketStorage implements CubicTicketStorage {
    // TODO (P2) codec nonsense for save/load

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "addTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V")
    public native void cc_addTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "addTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V")
    public native void cc_addTicket(Ticket ticket, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "removeTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V")
    public native void cc_removeTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "removeTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V")
    public native void cc_removeTicket(Ticket ticket, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)Z")
    public native boolean cc_updateChunkForced(CloPos cloPos, boolean add);

    // ------------------------------------------------------------------ player tickets taken away a few at a time

    /**
     * A player's loading tickets, one on every cube in their view (1,331 at a view distance of 10), all went at once when they teleported:
     * the loading tracker then worked the levels of all those cubes and their neighbours out again in one tick (each cube asking its 26
     * neighbours about all of theirs), a stall of one to two seconds. In a cubic level they wait here instead and go
     * {@link CubicTicketStorage#PLAYER_TICKET_REMOVALS_PER_TICK} a tick (ServerChunkCache's tick, MixinServerChunkCache): the old place unloads over a few ticks. Flying,
     * a player leaves fewer than that a tick behind (a cube's face, 121), which go at once as before. A ticket given again while it waits
     * (the player came back) simply stays.
     */
    @Unique private boolean cc_staggerPlayerRemovals;
    @Unique private boolean cc_draining;
    @Unique private final Long2ObjectLinkedOpenHashMap<Ticket> cc_pendingPlayerRemovals = new Long2ObjectLinkedOpenHashMap<>();

    @Shadow public abstract boolean removeTicket(long key, Ticket ticket);

    @Override public void cc_staggerPlayerTicketRemovals() {
        this.cc_staggerPlayerRemovals = true;
    }

    @Inject(method = "removeTicket(JLnet/minecraft/server/level/Ticket;)Z", at = @At("HEAD"), cancellable = true)
    private void cc_waitToRemovePlayerTicket(long key, Ticket ticket, CallbackInfoReturnable<Boolean> cir) {
        if (this.cc_staggerPlayerRemovals && !this.cc_draining && ticket.getType() == TicketType.PLAYER_LOADING) {
            this.cc_pendingPlayerRemovals.put(key, ticket);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "addTicket(JLnet/minecraft/server/level/Ticket;)Z", at = @At("HEAD"))
    private void cc_keepWaitingPlayerTicket(long key, Ticket ticket, CallbackInfoReturnable<Boolean> cir) {
        if (this.cc_staggerPlayerRemovals && ticket.getType() == TicketType.PLAYER_LOADING) {
            this.cc_pendingPlayerRemovals.remove(key); // still there: vanilla finds it and keeps it
        }
    }

    @Override public int cc_drainPlayerTicketRemovals(int max) {
        if (this.cc_pendingPlayerRemovals.isEmpty()) {
            return 0;
        }
        int n = 0;
        this.cc_draining = true;
        try {
            while (n < max && !this.cc_pendingPlayerRemovals.isEmpty()) {
                long key = this.cc_pendingPlayerRemovals.firstLongKey();
                Ticket ticket = this.cc_pendingPlayerRemovals.removeFirst();
                this.removeTicket(key, ticket);
                n++;
            }
        } finally {
            this.cc_draining = false;
        }
        return n;
    }
}
