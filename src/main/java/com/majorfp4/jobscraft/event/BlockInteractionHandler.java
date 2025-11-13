package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class BlockInteractionHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        if (player == null) return;

        BlockState clickedState = player.level.getBlockState(event.getPos());
        ItemStack itemInHand = player.getItemInHand(event.getHand());

        boolean isForbidden = false;
        String message = "";

        // --- CHECAGEM 1: INTERAÇÃO (Usando um bloco existente) ---
        if (isInteractionForbidden(player, clickedState)) {
            isForbidden = true;
            message = "Você não tem a profissão para usar este bloco.";
        }

        // --- CHECAGEM 2: COLOCAÇÃO (Colocando um bloco novo) ---
        if (!isForbidden && itemInHand.getItem() instanceof BlockItem blockItem) {
            BlockState stateToPlace = blockItem.getBlock().defaultBlockState();
            if (isInteractionForbidden(player, stateToPlace)) {
                isForbidden = true;
                message = "Você não tem a profissão para colocar este bloco.";
            }
        }

        // --- AÇÃO FINAL ---
        if (isForbidden) {
            event.setCanceled(true);
            if (player.level.isClientSide()) {
                player.displayClientMessage(new TextComponent(message), true);
            }
        }
    }

    private static boolean isInteractionForbidden(Player player, BlockState blockState) {
        ResourceLocation blockRL = ForgeRegistries.BLOCKS.getKey(blockState.getBlock());
        if (blockRL == null) return false;
        String blockId = blockRL.toString();

        // 1. Encontra a profissão que o BLOCO exige
        Profession matchingProfession = getProfessionByBlock(blockState, blockId);
        if (matchingProfession == null) {
            return false;
        }

        // 2. Checa se o bloco está na lista EXCLUSIVA
        if (!isBlockInList(blockState, blockId, matchingProfession.getExclusiveBlocks())) {
            return false;
        }

        // 3. O bloco é exclusivo. O jogador tem a profissão?
        Scoreboard scoreboard = player.getScoreboard();
        Objective professionObj = scoreboard.getObjective("profession");
        if (professionObj == null) {
            return true;
        }

        Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
        int playerProfessionId = professionScore.getScore();
        int requiredProfessionId = matchingProfession.getId();

        return playerProfessionId != requiredProfessionId;
    }


    private static boolean isBlockInList(BlockState state, String blockId, List<String> list) {
        for (String entry : list) {
            if (entry.startsWith("#")) {
                // É uma Tag
                String tagName = entry.substring(1);
                TagKey<Block> tagKey = BlockTags.create(new ResourceLocation(tagName));
                if (state.is(tagKey)) {
                    return true;
                }
            } else {
                // É um Bloco ID
                if (entry.equals(blockId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Profession getProfessionByBlock(BlockState state, String blockId) {
        Collection<Profession> professions = JobsConfig.getAllProfessions();
        for (Profession p : professions) {
            if (isBlockInList(state, blockId, p.getExclusiveBlocks()) || isBlockInList(state, blockId, p.getRelatedBlocks())) {
                return p;
            }
        }
        return null;
    }
}