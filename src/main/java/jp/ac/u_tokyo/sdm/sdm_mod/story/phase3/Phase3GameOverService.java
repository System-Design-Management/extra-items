package jp.ac.u_tokyo.sdm.sdm_mod.story.phase3;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.Phase3LivesPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.Phase5GameOverPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Phase3GameOverService {
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE3_ZOMBIE_TAG = "sdm_mod.phase3_zombie";
    static final int MAX_ZOMBIE_DEATHS = 5;
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
            int remaining = MAX_ZOMBIE_DEATHS - deaths;
            if (remaining <= 0) {
                ZOMBIE_DEATH_COUNTS.remove(player.getUuid());
                ServerPlayNetworking.send(player, Phase5GameOverPayload.INSTANCE);
            } else {
                ServerPlayNetworking.send(player, new Phase3LivesPayload(remaining));
            }
        });

        // 途中参加・再接続時に現在の残機を送信する
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !PHASE3_ID.equals(storyManager.getProgress().currentChapterId())) {
                return;
            }
            sendLivesUpdate(player);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ZOMBIE_DEATH_COUNTS.clear());
    }

    /** phase3 開始時（扉突入時）に呼ぶ。残機を満タンにしてHUDを表示する。 */
    public static void notifyPhase3Start(ServerPlayerEntity player) {
        ZOMBIE_DEATH_COUNTS.remove(player.getUuid());
        ServerPlayNetworking.send(player, new Phase3LivesPayload(MAX_ZOMBIE_DEATHS));
    }

    /** phase3 クリア時（本発見時）に呼ぶ。HUDを非表示にする。 */
    public static void notifyPhase3End(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new Phase3LivesPayload(-1));
    }

    private static void sendLivesUpdate(ServerPlayerEntity player) {
        int deaths = ZOMBIE_DEATH_COUNTS.getOrDefault(player.getUuid(), 0);
        int remaining = Math.max(MAX_ZOMBIE_DEATHS - deaths, 0);
        ServerPlayNetworking.send(player, new Phase3LivesPayload(remaining));
    }
}
