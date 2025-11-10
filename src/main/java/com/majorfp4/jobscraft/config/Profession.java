package com.majorfp4.jobscraft.config;

import java.util.List;

public class Profession {
    private final int id;
    private final String name;
    private final List<String> relatedBlocks;
    private final List<String> exclusiveBlocks;

    public Profession(int id, String name, List<String> relatedBlocks, List<String> exclusiveBlocks) {
        this.id = id;
        this.name = name;
        this.relatedBlocks = relatedBlocks;
        this.exclusiveBlocks = exclusiveBlocks;
    }

    public int getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profession that = (Profession) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
