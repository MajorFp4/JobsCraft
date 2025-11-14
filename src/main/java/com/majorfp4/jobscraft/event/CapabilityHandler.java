package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.capability.PlayerProgress;
import com.majorfp4.jobscraft.capability.PlayerProgressProvider;
import com.majorfp4.jobscraft.capability.TechProgressionCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    /**
     * Evento que anexa nossa Capability (mochila de dados) ao jogador
     * assim que ele é criado.
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            // Anexa nosso "Fornecedor" (Provider) ao jogador.
            event.addCapability(new ResourceLocation(JobsCraft.MOD_ID, "player_progress"),
                    new PlayerProgressProvider());
        }
    }

    /**
     * Evento que copia os dados da Capability quando o jogador morre
     * (ou troca de dimensão, como ir para o Nether).
     * Sem isso, o progresso seria resetado ao morrer.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        event.getOriginal().getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(oldProgress -> {
            event.getPlayer().getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(newProgress -> {

                newProgress.deserializeNBT(oldProgress.serializeNBT());
            });
        });
    }

    /**
     * Evento que registra nossa Capability no Forge (necessário).
     * Este evento é disparado pelo "ModEventBus", então
     * precisamos ligá-lo no nosso arquivo principal.
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerProgress.class);
    }
}