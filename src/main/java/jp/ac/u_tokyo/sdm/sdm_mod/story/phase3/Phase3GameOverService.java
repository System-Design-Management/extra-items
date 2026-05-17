package jp.ac.u_tokyo.sdm.sdm_mod.story.phase3;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.Phase5GameOverPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Phase3GameOverService {
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE3_ZOMBIE_TAG = "sdm_mod.phase3_zombie";
    private static final int MAX_ZOMBIE_DEATHS = 5;
    private static final Map<UUID, Integer> ZOMBIE_DEATH_COUNTS = new HashMap<>();

    private Phase3GameOverService() {
    }

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !PHASE3_ID.equals(storyManager.getProgress().currentChapterId())) {
                return;
            }

            if (!(damageSource.getAttacker() instanceof ZombieEntity zombie)
                || !zombie.getCommandTags().contains(PHASE3_ZOMBIE_TAG)) {
                return;
            }

            int deaths = ZOMBIE_DEATH_COUNTS.merge(player.getUuid(), 1, Integer::sum);
            if (deaths >= MAX_ZOMBIE_DEATHS) {
                ZOMBIE_DEATH_COUNTS.remove(player.getUuid());
                ServerPlayNetworking.send(player, Phase5GameOverPayload.INSTANCE);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ZOMBIE_DEATH_COUNTS.clear());
    }
}
