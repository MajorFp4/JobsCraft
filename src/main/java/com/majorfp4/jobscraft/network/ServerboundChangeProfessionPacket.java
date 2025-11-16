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

import java.util.function.Supplier;

/**
 * Pacote enviado do Cliente para o Servidor quando
 * o jogador escolhe uma nova profissão na GUI.
 */
public class ServerboundChangeProfessionPacket {

    private final int professionId;

    public ServerboundChangeProfessionPacket(int professionId) {
        this.professionId = professionId;
    }

    // "Empacota" o ID
    public static void encode(ServerboundChangeProfessionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.professionId);
    }

    // "Desempacota" o ID
    public static ServerboundChangeProfessionPacket decode(FriendlyByteBuf buf) {
        return new ServerboundChangeProfessionPacket(buf.readInt());
    }

    // Ação a ser executada no Servidor
    public static void handle(ServerboundChangeProfessionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Scoreboard scoreboard = player.getScoreboard();
            Objective professionObj = scoreboard.getObjective("profession");
            if (professionObj == null) {
                return;
            }

            // 1. Atualiza o placar
            Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
            professionScore.setScore(msg.professionId);

            // 2. Reseta o progresso e sincroniza com o cliente (só precisa fazer uma vez)
            player.getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(progress -> {
                progress.resetProgress(); // Limpa os dados no servidor

                // Envia a lista de progresso VAZIA para o cliente
                PacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncProgressPacket(Collections.emptySet())
                );
            });

            // 3. Envia o novo ID de PROFISSÃO de volta ao cliente
            PacketHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ClientboundSyncProfessionPacket(msg.professionId)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}