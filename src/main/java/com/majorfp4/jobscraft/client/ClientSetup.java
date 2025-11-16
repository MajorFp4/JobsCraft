package com.majorfp4.jobscraft.client;

import com.majorfp4.jobscraft.client.event.ClientGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
// ... (importações no topo)
import com.majorfp4.jobscraft.client.jei.JobsCraftJEIPlugin; // <-- NOVA IMPORTAÇÃO

/**
 * Lida com o setup que SÓ deve rodar no lado do cliente.
 */
public class ClientSetup {

    public ClientSetup(IEventBus modEventBus) {
        // Registra o método onClientSetup para ser chamado
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // Registra nosso handler de GUI (código antigo)
        MinecraftForge.EVENT_BUS.register(new ClientGuiHandler());
    }
}