package com.majorfp4.jobscraft.network;

import com.majorfp4.jobscraft.client.ClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import com.majorfp4.jobscraft.client.jei.JobsCraftJEIPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Pacote enviado do Servidor para o Cliente para sincronizar a "memória"
 * de progresso da tech tree.
 */
public class ClientboundSyncProgressPacket {

    private final Set<ResourceLocation> craftedItems;

    public ClientboundSyncProgressPacket(Set<ResourceLocation> craftedItems) {
        this.craftedItems = craftedItems;
    }

    // "Empacota" o Set
    public static void encode(ClientboundSyncProgressPacket msg, FriendlyByteBuf buf) {
        // Envia o número de itens
        buf.writeInt(msg.craftedItems.size());
        // Envia cada item como uma string
        for (ResourceLocation item : msg.craftedItems) {
            buf.writeResourceLocation(item);
        }
    }

    // "Desempacota" o Set
    public static ClientboundSyncProgressPacket decode(FriendlyByteBuf buf) {
        Set<ResourceLocation> items = new HashSet<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            items.add(buf.readResourceLocation());
        }
        return new ClientboundSyncProgressPacket(items);
    }

    // Ação a ser executada no Cliente
    public static void handle(ClientboundSyncProgressPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Código executado na thread principal do Cliente

            // ATUALIZA O CACHE DO CLIENTE
            ClientCache.CRAFTED_ITEMS.clear();
            ClientCache.CRAFTED_ITEMS.addAll(msg.craftedItems);
            JobsCraftJEIPlugin.refreshJEIFilter();

        });
        ctx.get().setPacketHandled(true);
    }
}