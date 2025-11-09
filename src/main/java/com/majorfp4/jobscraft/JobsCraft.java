package com.majorfp4.jobscraft;

import com.majorfp4.jobscraft.config.JobsConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JobsCraft.MOD_ID)
public class JobsCraft {
    public static final String MOD_ID = "jobscraft";

    public JobsCraft() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        JobsConfig.initialize(); // Cria pasta e carrega as profissões
    }
}