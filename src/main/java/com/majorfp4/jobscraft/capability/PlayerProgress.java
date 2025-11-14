package com.majorfp4.jobscraft.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import java.util.HashSet;
import java.util.Set;

public class PlayerProgress implements INBTSerializable<CompoundTag> {

    // Usamos um 'Set' para que um item não possa ser adicionado duas vezes.
    private final Set<ResourceLocation> craftedItems = new HashSet<>();

    /**
     * Adiciona um item à lista de progresso.
     */
    public void addCraftedItem(ResourceLocation item) {
        this.craftedItems.add(item);
    }

    /**
     * Verifica se o jogador já fabricou um item.
     */
    public boolean hasCrafted(ResourceLocation item) {
        return this.craftedItems.contains(item);
    }

    /**
     * Pega a lista de todos os itens fabricados.
     */
    public Set<ResourceLocation> getCraftedItems() {
        return this.craftedItems;
    }

    /**
     * Limpa o progresso. (Usado ao trocar de profissão).
     */
    public void resetProgress() {
        this.craftedItems.clear();
    }

    /**
     * Salva os dados no disco (quando o jogador sai).
     */
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();
        // Salva cada ID de item como uma string na lista
        for (ResourceLocation item : this.craftedItems) {
            list.add(StringTag.valueOf(item.toString()));
        }
        nbt.put("CraftedItems", list);
        return nbt;
    }

    /**
     * Carrega os dados do disco (quando o jogador entra).
     */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.craftedItems.clear();
        ListTag list = nbt.getList("CraftedItems", Tag.TAG_STRING);
        // Lê cada string e a transforma de volta em um ResourceLocation
        for (Tag tag : list) {
            this.craftedItems.add(new ResourceLocation(tag.getAsString()));
        }
    }
}