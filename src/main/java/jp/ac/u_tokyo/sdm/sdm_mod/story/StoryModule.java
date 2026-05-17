package jp.ac.u_tokyo.sdm.sdm_mod.story;

import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2DoorArrowService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2PoliceOfficerGunTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2PoliceOfficerHintService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2TutorialDialogueService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2TutorialGateService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2To3RegionTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2TutorialZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3BookshelfService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3GameOverService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3ZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3To4BookTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4IntroDialogueService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4To5RegionTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraDialogueService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5StairArrowService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5To6RegionTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4ZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.registry.StoryChapterRegistry;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryCombatService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryAreaConstraintService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryDoorLockService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryEntityControlService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.Phase5GameOverService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryAutoStartService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryRespawnService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryStudentIdGateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StoryModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoryModule.class);
    private static StoryManager storyManager;

    private StoryModule() {
    }

    public static void initialize() {
        StoryChapterRegistry.initialize();
        storyManager = StoryManager.createDefault();
        StoryAutoStartService.initialize();
        StoryEntityControlService.initialize();
        StoryCombatService.initialize();
        StoryAreaConstraintService.initialize();
        StoryDoorLockService.initialize();
        Phase5GameOverService.initialize();
        StoryRespawnService.initialize();
        StoryStudentIdGateService.initialize();
        Phase2DoorArrowService.initialize();
        Phase2PoliceOfficerGunTrigger.initialize();
        Phase2PoliceOfficerHintService.initialize();
        Phase2TutorialDialogueService.initialize();
        Phase2TutorialGateService.initialize();
        Phase2To3RegionTrigger.initialize();
        Phase2TutorialZombieService.initialize();
        Phase3ZombieService.initialize();
        Phase3GameOverService.initialize();
        Phase3BookshelfService.initialize();
        Phase3To4BookTrigger.initialize();
        Phase4IntroDialogueService.initialize();
        Phase4ZombieService.initialize();
        Phase4To5RegionTrigger.initialize();
        Phase5OnaraDialogueService.initialize();
        Phase5StairArrowService.initialize();
        Phase5To6RegionTrigger.initialize();
        LOGGER.info("Story module initialized with {} chapter(s).", StoryChapterRegistry.size());
    }

    public static StoryManager getStoryManager() {
        if (storyManager == null) {
            throw new IllegalStateException("Story module is not initialized.");
        }

        return storyManager;
    }
}
