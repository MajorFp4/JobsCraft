package com.majorfp4.jobscraft.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FileUtils {
    public static Path createJobsFolder() {
        Path configPath = Path.of("config", "Jobs");
        File folder = configPath.toFile();

        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created)
                System.out.println("[JobsCraft] Pasta 'Jobs' criada em: " + folder.getAbsolutePath());
        } else {
            System.out.println("[JobsCraft] Pasta 'Jobs' encontrada.");
        }

        return configPath;
    }

    public static void createExampleProfession(Path jobsFolder) {
        File example = new File(jobsFolder.toFile(), "Miner.toml");
        if (example.exists()) return;

        String content = """
                name = "Miner"
                
                iD = 1

                related_blocks = [
                    "#forge:stones"
                ]

                exclusive_blocks = [
                    "#forge:ores"
                ]
                """;

        try (FileWriter writer = new FileWriter(example)) {
            writer.write(content);
            System.out.println("[JobsMod] Arquivo de exemplo 'Miner.toml' criado.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
