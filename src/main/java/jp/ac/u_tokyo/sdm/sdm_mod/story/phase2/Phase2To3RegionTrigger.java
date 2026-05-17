package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import jp.ac.u_tokyo.sdm.sdm_mod.ModSounds;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3GameOverService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Phase2To3RegionTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase2To3RegionTrigger.class);
    private static final String PHASE2_ID = "phase2";
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE3_START_TEXT1 = "大閲覧室に入ったな。ゾンビがたくさんいるかもしれん。";
    private static final String PHASE3_START_TEXT2 = "本の場所は覚えているな？素早く探すんだぞ！";
    private static final String PHASE3_START_TEXT3 = "さっき確認した番号の本棚にある本を１冊ずつ確認するしかないな。";
    private static final String PHASE3_START_TEXT4 = "おい！まだ見つからないのか！！本棚の番号は19だったよな。そこを探しに行け！";
    private static final int LINE_03_01_TICKS = 100;
    private static final int LINE_03_02_TICKS = 80;
    private static final int LINE_03_03_TICKS = 120;
    private static final int LINE_03_04_TICKS = 140;
    // PHASE3_START_TEXT1（26文字）の表示 + 自動消去（30tick）に余裕を持たせた遅延
    private static final long SECOND_DIALOGUE_DELAY_TICKS = 65L;
    // PHASE3_START_TEXT2（21文字）の表示 + 自動消去（30tick）に余裕を持たせた遅延
    private static final long THIRD_DIALOGUE_DELAY_TICKS = 60L;
    // PHASE3_START_TEXT3（28文字）の表示 + 自動消去（30tick）+ 2分（2400tick）の遅延
    private static final long FOURTH_DIALOGUE_DELAY_TICKS = 2460L;
    private static final Map<UUID, Long> PENDING_SECOND_DIALOGUE_TICK = new HashMap<>();
    private static final Map<UUID, Long> PENDING_THIRD_DIALOGUE_TICK = new HashMap<>();
    private static final Map<UUID, Long> PENDING_FOURTH_DIALOGUE_TICK = new HashMap<>();
    private static final Set<BlockPos> TRIGGER_DOOR_BASE_POSITIONS = Set.of(
        new BlockPos(-175, 41, -640),
        new BlockPos(-176, 41, -640)
    );

    private Phase2To3RegionTrigger() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase2To3RegionTrigger::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PENDING_SECOND_DIALOGUE_TICK.clear();
            PENDING_THIRD_DIALOGUE_TICK.clear();
            PENDING_FOURTH_DIALOGUE_TICK.clear();
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
                return ActionResult.PASS;
            }

            BlockPos clickedPos = hitResult.getBlockPos();
            BlockState clickedState = world.getBlockState(clickedPos);
            if (!(clickedState.getBlock() instanceof DoorBlock)) {
                return ActionResult.PASS;
            }

            BlockPos doorBasePos = normalizeDoorBasePos(clickedPos, clickedState);
            if (!TRIGGER_DOOR_BASE_POSITIONS.contains(doorBasePos)) {
                return ActionResult.PASS;
            }

            storyManager.advanceToChapter(PHASE3_ID);
            notifyTriggered((ServerPlayerEntity) player);
            LOGGER.info(
                "Story advanced from {} to {} after player {} used door at {}.",
                PHASE2_ID,
                PHASE3_ID,
                player.getName().getString(),
                doorBasePos
            );
            return ActionResult.PASS;
        });
    }

    private static BlockPos normalizeDoorBasePos(BlockPos clickedPos, BlockState clickedState) {
        if (clickedState.contains(DoorBlock.HALF) && clickedState.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            return clickedPos.down();
        }

        return clickedPos;
    }

    private static void tick(MinecraftServer server) {
        long currentTick = server.getOverworld().getTime();
        PENDING_SECOND_DIALOGUE_TICK.entrySet().removeIf(entry -> {
            if (currentTick < entry.getValue()) {
                return false;
            }
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player != null) {
                Phase2DialogueVoiceService.enqueue(
                    player,
                    "phase3_start_2",
                    PHASE3_START_TEXT2,
                    ModSounds.PHASE3_LINE_03_02,
                    LINE_03_02_TICKS
                );
                PENDING_THIRD_DIALOGUE_TICK.put(entry.getKey(), currentTick + THIRD_DIALOGUE_DELAY_TICKS);
            }
            return true;
        });
        PENDING_THIRD_DIALOGUE_TICK.entrySet().removeIf(entry -> {
            if (currentTick < entry.getValue()) {
                return false;
            }
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player != null) {
                Phase2DialogueVoiceService.enqueue(
                    player,
                    "phase3_start_3",
                    PHASE3_START_TEXT3,
                    ModSounds.PHASE3_LINE_03_03,
                    LINE_03_03_TICKS
                );
                PENDING_FOURTH_DIALOGUE_TICK.put(entry.getKey(), currentTick + FOURTH_DIALOGUE_DELAY_TICKS);
            }
            return true;
        });
        PENDING_FOURTH_DIALOGUE_TICK.entrySet().removeIf(entry -> {
            if (currentTick < entry.getValue()) {
                return false;
            }
            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE3_ID)) {
                return true;
            }
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player != null) {
                Phase2DialogueVoiceService.enqueue(
                    player,
                    "phase3_start_4",
                    PHASE3_START_TEXT4,
                    ModSounds.PHASE3_LINE_03_04,
                    LINE_03_04_TICKS,
                    Phase2DialogueVoiceService.DeliveryMode.INTERRUPT
                );
            }
            return true;
        });
    }

    private static void notifyTriggered(ServerPlayerEntity player) {
        Phase3GameOverService.notifyPhase3Start(player);
        Phase2DialogueVoiceService.enqueue(
            player,
            "phase3_start_1",
            PHASE3_START_TEXT1,
            ModSounds.PHASE3_LINE_03_01,
            LINE_03_01_TICKS,
            Phase2DialogueVoiceService.DeliveryMode.INTERRUPT
        );
        long futureTick = player.getWorld().getTime() + SECOND_DIALOGUE_DELAY_TICKS;
        PENDING_SECOND_DIALOGUE_TICK.put(player.getUuid(), futureTick);
    }
}
