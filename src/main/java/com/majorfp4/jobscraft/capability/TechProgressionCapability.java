package com.majorfp4.jobscraft.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class TechProgressionCapability {

    public static final Capability<PlayerProgress> PLAYER_PROGRESS =
            CapabilityManager.get(new CapabilityToken<>() {});

}