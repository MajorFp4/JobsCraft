package com.majorfp4.jobscraft.client;

import net.minecraft.resources.ResourceLocation;
import  java.util.HashSet;
import  java.util.Set;

/**
 * Armazena dados do lado do cliente que recebemos do servidor.
 */
public class ClientCache {

    /**
     * O ID da profissão atual do jogador.
     * Atualizado pelo ClientboundSyncProfessionPacket.
     * O padrão é 0 (NONE).
     */
    public static int CURRENT_PROFESSION_ID = 0;
    public static final Set<ResourceLocation> CRAFTED_ITEMS = new HashSet<>();

}