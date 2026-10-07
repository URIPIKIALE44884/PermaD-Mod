package com.permadeath;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityType;

/** Mobs que se pueden moldear. */
public enum MobKind {
    ZOMBIE("zombie", EntityType.ZOMBIE, Family.ZOMBIE, "overworld"),
    HUSK("husk", EntityType.HUSK, Family.ZOMBIE, "overworld"),
    DROWNED("drowned", EntityType.DROWNED, Family.ZOMBIE, "overworld"),
    ZOMBIE_VILLAGER("zombie_villager", EntityType.ZOMBIE_VILLAGER, Family.ZOMBIE, "overworld"),
    SKELETON("skeleton", EntityType.SKELETON, Family.SKELETON, "overworld", "nether"),
    STRAY("stray", EntityType.STRAY, Family.SKELETON, "overworld"),
    WITHER_SKELETON("wither_skeleton", EntityType.WITHER_SKELETON, Family.SKELETON, "nether"),
    SPIDER("spider", EntityType.SPIDER, Family.SPIDER, "overworld"),
    CAVE_SPIDER("cave_spider", EntityType.CAVE_SPIDER, Family.SPIDER, "overworld"),
    CREEPER("creeper", EntityType.CREEPER, Family.CREEPER, "overworld");

    public enum Family { ZOMBIE, SKELETON, SPIDER, CREEPER }

    public final String id;
    public final EntityType<?> type;
    public final Family family;
    public final Set<String> vanillaDims;

    MobKind(String id, EntityType<?> type, Family family, String... dims) {
        this.id = id;
        this.type = type;
        this.family = family;
        Set<String> set = new LinkedHashSet<>();
        for (String d : dims) {
            set.add(d);
        }
        this.vanillaDims = set;
    }

    public static MobKind of(EntityType<?> type) {
        for (MobKind kind : values()) {
            if (kind.type == type) {
                return kind;
            }
        }
        return null;
    }

    public static MobKind byId(String id) {
        for (MobKind kind : values()) {
            if (kind.id.equals(id)) {
                return kind;
            }
        }
        return null;
    }

    public static List<String> ids() {
        List<String> list = new ArrayList<>();
        for (MobKind kind : values()) {
            list.add(kind.id);
        }
        return list;
    }
}
