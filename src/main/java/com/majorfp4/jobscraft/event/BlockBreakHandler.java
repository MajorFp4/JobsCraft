package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import com.majorfp4.jobscraft.JobsCraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class BlockBreakHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getPlayer();
        if (player.level.isClientSide) return;

        if (!(player instanceof ServerPlayer serverPlayer)) return;
        BlockState blockState = event.getState();
        Block block = blockState.getBlock();
        ResourceLocation blockRL = ForgeRegistries.BLOCKS.getKey(block);
        if (blockRL == null) return;

        String blockId = blockRL.toString(); // ex: "minecraft:diamond_ore"
        Scoreboard scoreboard = serverPlayer.getScoreboard();

        Objective professionObj = scoreboard.getObjective("profession");
        if (professionObj == null) return;

        // Usar getOrCreatePlayerScore para compatibilidade
        Score professionScore = scoreboard.getOrCreatePlayerScore(serverPlayer.getScoreboardName(), professionObj);
        int professionId = professionScore.getScore();

        String playerProfession = professionId == 0 ? "NONE" : getProfessionNameById(professionId);

        Profession matchingProfession = getProfessionByBlock(blockId);
        if (matchingProfession == null) return;

        // Se o jogador não é da profissão correspondente
        if (!matchingProfession.getName().equalsIgnoreCase(playerProfession)) {

            // Se for um bloco exclusivo, torna inquebrável
            if (matchingProfession.getExclusiveBlocks().contains(blockId)) {
                event.setNewSpeed(0.0F);
                return;
            }

            // Se for um bloco relacionado de outra profissão, 70% mais lento
            if (matchingProfession.getRelatedBlocks().contains(blockId)) {
                event.setNewSpeed(event.getOriginalSpeed() * 0.3F);
            }
        }
    }

    private static Profession getProfessionByBlock(String blockId) {
        Collection<Profession> professions = JobsConfig.getAllProfessions();
        for (Profession p : professions) {
            if (p.getExclusiveBlocks().contains(blockId) || p.getRelatedBlocks().contains(blockId)) {
                return p;
            }
        }
        return null;
    }

    private static String getProfessionNameById(int id) {
        // Placeholder simples — depois podemos criar mapeamento
        if (id == 1) return "Minerador";
        return "NONE";
    }
}
