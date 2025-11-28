package com.majorfp4.jobscraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import com.majorfp4.jobscraft.capability.TechProgressionCapability;
import com.majorfp4.jobscraft.network.ClientboundSyncProgressPacket;
import java.util.Collections;
import com.majorfp4.jobscraft.event.RecipeHandler;
import java.util.function.Supplier;

public class ServerboundChangeProfessionPacket {

    private final int professionId;

    public ServerboundChangeProfessionPacket(int professionId) {
        this.professionId = professionId;
    }

    public static void encode(ServerboundChangeProfessionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.professionId);
    }

    public static ServerboundChangeProfessionPacket decode(FriendlyByteBuf buf) {
        return new ServerboundChangeProfessionPacket(buf.readInt());
    }

    public static void handle(ServerboundChangeProfessionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null)
                return;

            Scoreboard scoreboard = player.getScoreboard();
            Objective professionObj = scoreboard.getObjective("profession");
            if (professionObj == null) {
                return;
            }

            Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
            professionScore.setScore(msg.professionId);

            // Reset Skill Level
            Objective skillObj = scoreboard.getObjective("skill");
            if (skillObj != null) {
                Score skillScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), skillObj);
                skillScore.setScore(161);
            }

            player.getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(progress -> {
                progress.resetProgress();

                PacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncProgressPacket(Collections.emptySet()));
            });

            PacketHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ClientboundSyncProfessionPacket(msg.professionId));
            RecipeHandler.sendUpdatedRecipes(player);
        });
        ctx.get().setPacketHandled(true);
    }
}