package com.majorfp4.jobscraft.network;

import com.majorfp4.jobscraft.client.ClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import com.majorfp4.jobscraft.client.jei.JobsCraftJEIPlugin;

import java.util.function.Supplier;

/**
 * Pacote enviado do Servidor para o Cliente para sincronizar a profissão
 * atual do jogador.
 */
public class ClientboundSyncProfessionPacket {

    private final int professionId;

    public ClientboundSyncProfessionPacket(int professionId) {
        this.professionId = professionId;
    }

    public static void encode(ClientboundSyncProfessionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.professionId);
    }

    public static ClientboundSyncProfessionPacket decode(FriendlyByteBuf buf) {
        return new ClientboundSyncProfessionPacket(buf.readInt());
    }

    // Ação a ser executada no Cliente
    public static void handle(ClientboundSyncProfessionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Código executado na thread principal do Cliente

            // ATUALIZA O CACHE DO CLIENTE
            ClientCache.CURRENT_PROFESSION_ID = msg.professionId;
            JobsCraftJEIPlugin.refreshJEIFilter();
        });
        ctx.get().setPacketHandled(true);
    }
}