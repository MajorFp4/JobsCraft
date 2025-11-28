package com.majorfp4.jobscraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class JobsCraftConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue baseSkill;
    public static final ForgeConfigSpec.DoubleValue ioFactor;
    public static final ForgeConfigSpec.DoubleValue yFactor;

    static {
        BUILDER.push("JobsCraft General Configuration");

        baseSkill = BUILDER.comment("Skill Base para jogadores sem profissão ou iniciantes")
                .defineInRange("baseSkill", 161, 1, 10000);

        ioFactor = BUILDER.comment("Fator de I/O (Divisor de Velocidade) - Quanto maior, menor a velocidade inicial")
                .defineInRange("ioFactor", 614.67, 1.0, 10000.0);

        yFactor = BUILDER.comment("Fator de Declividade (Y) para cálculo de ganho de XP")
                .defineInRange("yFactor", 180.0, 1.0, 10000.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
