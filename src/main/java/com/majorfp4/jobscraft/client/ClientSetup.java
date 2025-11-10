package com.majorfp4.jobscraft.client;

import com.majorfp4.jobscraft.client.event.ClientGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Lida com o setup que SÓ deve rodar no lado do cliente.
 */
public class ClientSetup {

    public ClientSetup(IEventBus modEventBus) {
        // Registra o método onClientSetup para ser chamado
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // Registra nosso handler de GUI no barramento de eventos principal do Forge
        // Isso "liga" o @SubscribeEvent dentro de ClientGuiHandler
        MinecraftForge.EVENT_BUS.register(new ClientGuiHandler());
    }
}