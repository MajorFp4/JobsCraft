package com.majorfp4.jobscraft;

import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.client.ClientSetup;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            new ClientSetup(modEventBus);
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        JobsConfig.initialize();
        event.enqueueWork(PacketHandler::register);
    }
}