package com.majorfp4.jobscraft.network;

import com.majorfp4.jobscraft.JobsCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JobsCraft.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        // Registra nosso pacote (Cliente -> Servidor)
        INSTANCE.registerMessage(id++,
                ServerboundChangeProfessionPacket.class,
                ServerboundChangeProfessionPacket::encode,
                ServerboundChangeProfessionPacket::decode,
                ServerboundChangeProfessionPacket::handle
        );
        INSTANCE.registerMessage(id++,
                ClientboundSyncProfessionPacket.class,
                ClientboundSyncProfessionPacket::encode,
                ClientboundSyncProfessionPacket::decode,
                ClientboundSyncProfessionPacket::handle
        );
        INSTANCE.registerMessage(id++,
                ClientboundSyncProgressPacket.class,
                ClientboundSyncProgressPacket::encode,
                ClientboundSyncProgressPacket::decode,
                ClientboundSyncProgressPacket::handle
        );
    }
}