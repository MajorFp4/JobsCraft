package com.majorfp4.jobscraft.config;

import java.util.List;

public class Profession {
    private final String name;
    private final List<String> relatedBlocks;
    private final List<String> exclusiveBlocks;

    public Profession(String name, List<String> relatedBlocks, List<String> exclusiveBlocks) {
        this.name = name;
        this.relatedBlocks = relatedBlocks;
        this.exclusiveBlocks = exclusiveBlocks;
    }

    public String getName() {
        return name;
    }

    public List<String> getRelatedBlocks() {
        return relatedBlocks;
    }

    public List<String> getExclusiveBlocks() {
        return exclusiveBlocks;
    }
}
