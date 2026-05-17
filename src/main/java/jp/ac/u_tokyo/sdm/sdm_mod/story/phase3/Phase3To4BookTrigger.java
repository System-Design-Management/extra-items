package jp.ac.u_tokyo.sdm.sdm_mod.story.phase3;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4ZombieService;
import net.minecraft.server.network.ServerPlayerEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Phase3To4BookTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase3To4BookTrigger.class);
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE4_ID = "phase4";
    private static final int BOOK_MARKER_LIGHT_LEVEL = 1;
    private static final int BLOCK_UPDATE_FLAGS = 3;
    // Temporary book trigger position. Replace this constant when the final book location is fixed.
    private static final BlockPos BOOK_TRIGGER_POS = new BlockPos(-120, 42, -636);
    private static final BlockPos BOOK_MARKER_LIGHT_POS = BOOK_TRIGGER_POS.up();

    private Phase3To4BookTrigger() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            if (!hitResult.getBlockPos().equals(BOOK_TRIGGER_POS)) {
                return ActionResult.PASS;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE3_ID)) {
                return ActionResult.PASS;
            }

            storyManager.advanceToChapter(PHASE4_ID);
            Phase4ZombieService.spawnPhase4Zombies((ServerWorld) world);
            removeMarkerLight((ServerWorld) world);
            Phase3GameOverService.notifyPhase3End((ServerPlayerEntity) player);
            LOGGER.info("Story advanced from {} to {} by right-clicking block at {}.", PHASE3_ID, PHASE4_ID, BOOK_TRIGGER_POS);
            return ActionResult.SUCCESS;
        });
        ServerTickEvents.END_SERVER_TICK.register(Phase3To4BookTrigger::tickMarkerLight);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> removeMarkerLight(server.getOverworld()));
    }

    private static void tickMarkerLight(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        ServerWorld world = server.getOverworld();

        if (storyManager.isActive() && storyManager.isAtChapter(PHASE3_ID)) {
            ensureMarkerLight(world);
            return;
        }

        removeMarkerLight(world);
    }

    private static void ensureMarkerLight(ServerWorld world) {
        if (!canPlaceMarkerLight(world)) {
            return;
        }

        BlockState currentState = world.getBlockState(BOOK_MARKER_LIGHT_POS);
        if (isMarkerLight(currentState)) {
            return;
        }

        world.setBlockState(
            BOOK_MARKER_LIGHT_POS,
            Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, BOOK_MARKER_LIGHT_LEVEL),
            BLOCK_UPDATE_FLAGS
        );
    }

    private static boolean canPlaceMarkerLight(ServerWorld world) {
        BlockState state = world.getBlockState(BOOK_MARKER_LIGHT_POS);
        return state.isAir() || isMarkerLight(state);
    }

    private static void removeMarkerLight(ServerWorld world) {
        BlockState state = world.getBlockState(BOOK_MARKER_LIGHT_POS);
        if (!isMarkerLight(state)) {
            return;
        }

        world.setBlockState(BOOK_MARKER_LIGHT_POS, Blocks.AIR.getDefaultState(), BLOCK_UPDATE_FLAGS);
    }

    private static boolean isMarkerLight(BlockState state) {
        return state.isOf(Blocks.LIGHT)
            && state.contains(LightBlock.LEVEL_15)
            && state.get(LightBlock.LEVEL_15) == BOOK_MARKER_LIGHT_LEVEL;
    }
}
