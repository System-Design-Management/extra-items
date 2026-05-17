package jp.ac.u_tokyo.sdm.sdm_mod.client;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.DoorArrowHud;
import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.Phase3LivesHud;
import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.RespawnGuideHud;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.CameraShakeState;
import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.SetupGuideHud;
import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.TeacherDialogueHud;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.BloodZombieEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.BoyEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.GirlEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.PosterEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.StudentEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.PoliceOfficerEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.SdmLogoEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.model.SdmLogoEntityModel;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.SearchPcScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.TeacherDialogueScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.TechnicalBookScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.warp.WarpSelectScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.story.StoryClientNetworking;
import jp.ac.u_tokyo.sdm.sdm_mod.network.TeacherDialogueHudPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.network.TeacherDialoguePayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.Phase3LivesPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.RespawnGuidePayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.SetupGuideHudPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.Identifier;

public class SdmModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.BLOOD_ZOMBIE, BloodZombieEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.STUDENT, StudentEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.GIRL, GirlEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.BOY, BoyEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POLICE_OFFICER, PoliceOfficerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SDM_LOGO, SdmLogoEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POSTER, PosterEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(SdmLogoEntityModel.LAYER, SdmLogoEntityModel::getTexturedModelData);
        HandledScreens.register(ModScreenHandlers.SEARCH_PC, SearchPcScreen::new);
        HandledScreens.register(ModScreenHandlers.TECHNICAL_BOOK, TechnicalBookScreen::new);
        HandledScreens.register(ModScreenHandlers.WARP_SELECT, WarpSelectScreen::new);
        StoryClientNetworking.initialize();
        // サーバーから TeacherDialoguePayload が届いたらダイアログ画面を開く。
        // execute() でメインスレッドに乗せてから setScreen を呼ぶ（スレッドセーフ対策）。
        ClientPlayNetworking.registerGlobalReceiver(TeacherDialoguePayload.ID, (payload, context) ->
            context.client().execute(() ->
                context.client().setScreen(new TeacherDialogueScreen(payload.text()))
            )
        );
        // HUD版: プレイを止めずにオーバーレイ表示する。
        // HudElementRegistry に登録することで毎フレーム render() が呼ばれる。
        HudElementRegistry.addLast(
            Identifier.of("sdm_mod", "teacher_dialogue_hud"),
            TeacherDialogueHud.INSTANCE
        );
        HudElementRegistry.addLast(
            Identifier.of("sdm_mod", "door_arrow_hud"),
            DoorArrowHud.INSTANCE
        );
        HudElementRegistry.addLast(
            Identifier.of("sdm_mod", "setup_guide_hud"),
            SetupGuideHud.INSTANCE
        );
        HudElementRegistry.addLast(
            Identifier.of("sdm_mod", "respawn_guide_hud"),
            RespawnGuideHud.INSTANCE
        );
        HudElementRegistry.addLast(
            Identifier.of("sdm_mod", "phase3_lives_hud"),
            Phase3LivesHud.INSTANCE
        );
        ClientPlayNetworking.registerGlobalReceiver(Phase3LivesPayload.ID, (payload, context) ->
            context.client().execute(() -> Phase3LivesHud.INSTANCE.setRemainingLives(payload.remainingLives()))
        );
        ClientPlayNetworking.registerGlobalReceiver(RespawnGuidePayload.ID, (payload, context) ->
            context.client().execute(RespawnGuideHud.INSTANCE::show)
        );
        ClientPlayNetworking.registerGlobalReceiver(SetupGuideHudPayload.ID, (payload, context) ->
            context.client().execute(() -> {
                SetupGuideHud.INSTANCE.setVisible(payload.visible());
                if (payload.visible()) {
                    ClientCommandLockState.lock();
                }
            })
        );
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            ClientCommandLockState.unlock()
        );
        // HUD はフレームではなくティック単位で文字を進める必要があるため、
        // ClientTickEvents でティックごとに tick() を呼び出す。
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CameraShakeState.tick();
            TeacherDialogueHud.INSTANCE.tick();
            RespawnGuideHud.INSTANCE.tick();
            // afterClose 内で setScreen() を呼べないため、ScheduledScreen に積まれた画面をここで開く
            Screen scheduled = ScreenScheduler.poll();
            if (scheduled != null) {
                client.setScreen(scheduled);
            }
            // afterClose 内で ClientPlayNetworking.send() が失敗する場合の保険として
            // アクションも END_CLIENT_TICK で実行する
            ScreenScheduler.runPendingAction();
        });
        // サーバーから HUD パケットが届いたら HUD に表示する。
        ClientPlayNetworking.registerGlobalReceiver(TeacherDialogueHudPayload.ID, (payload, context) ->
            context.client().execute(() -> TeacherDialogueHud.INSTANCE.show(payload.text(), payload.minDisplayTicks()))
        );
    }
}
