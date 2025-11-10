package com.majorfp4.jobscraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.network.NetworkEvent;

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
            // Código executado na thread principal do servidor
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Scoreboard scoreboard = player.getScoreboard();
            Objective professionObj = scoreboard.getObjective("profession");
            if (professionObj == null) {
                // Isso não deve acontecer se o PlayerJoinHandler funcionou
                return;
            }

            // ATUALIZA O PLACAR
            Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
            professionScore.setScore(msg.professionId);

            // (Opcional) Enviar mensagem de confirmação
            // player.displayClientMessage(new TextComponent("Profissão alterada!"), false);
        });
        ctx.get().setPacketHandled(true);
    }
}