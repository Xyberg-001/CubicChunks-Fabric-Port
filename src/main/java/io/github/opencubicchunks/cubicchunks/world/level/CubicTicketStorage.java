package io.github.opencubicchunks.cubicchunks.world.level;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;

public interface CubicTicketStorage {
    void cc_addTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    void cc_addTicket(Ticket ticket, CloPos cloPos);

    void cc_removeTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    void cc_removeTicket(Ticket ticket, CloPos cloPos);

    boolean cc_updateChunkForced(CloPos cloPos, boolean add);

    /** From now on player loading tickets are taken away a few at a time (a cubic level's; see MixinTicketStorage). */
    void cc_staggerPlayerTicketRemovals();

    /** Player loading tickets taken away a tick, at most (see MixinTicketStorage). */
    int PLAYER_TICKET_REMOVALS_PER_TICK = Integer.getInteger("cubicchunks.playerTicketRemovalsPerTick", 128);

    /** Takes away up to {@code max} of the player loading tickets waiting to go; how many went. */
    int cc_drainPlayerTicketRemovals(int max);
}
