package com.majorfp4.jobscraft.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerProgressProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    private final PlayerProgress progress = new PlayerProgress();
    private final LazyOptional<PlayerProgress> optional = LazyOptional.of(() -> progress);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        // Se o Forge estiver pedindo a NOSSA capability, nós a retornamos.
        if (cap == TechProgressionCapability.PLAYER_PROGRESS) {
            return optional.cast();
        }
        // Senão, retornamos vazio.
        return LazyOptional.empty();
    }

    // Métodos para salvar/carregar os dados
    @Override
    public CompoundTag serializeNBT() {
        return progress.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        progress.deserializeNBT(nbt);
    }
}