package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2DoorArrowService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4DialogueClosedPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4IntroDialogueService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.FireworkShakePayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraClosedPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraDialogueService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryStartService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class StoryNetworking {
    private StoryNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(Phase5GameOverPayload.ID, Phase5GameOverPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(Phase3LivesPayload.ID, Phase3LivesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowOpVideoPayload.ID, ShowOpVideoPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SetupGuideHudPayload.ID, SetupGuideHudPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowEdVideoPayload.ID, ShowEdVideoPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DoorArrowPayload.ID, DoorArrowPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowBookUiPayload.ID, ShowBookUiPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StoryVideoStartPayload.ID, StoryVideoStartPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SearchPcLocationOpenedPayload.ID, SearchPcLocationOpenedPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SearchPcLocationClosedPayload.ID, SearchPcLocationClosedPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Phase4DialogueClosedPayload.ID, Phase4DialogueClosedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(Phase5OnaraPayload.ID, Phase5OnaraPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FireworkShakePayload.ID, FireworkShakePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RespawnGuidePayload.ID, RespawnGuidePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Phase5OnaraClosedPayload.ID, Phase5OnaraClosedPayload.CODEC);
        // 動画再生終了後にクライアントからこのパケットが届いたらストーリーを開始する
        ServerPlayNetworking.registerGlobalReceiver(StoryVideoStartPayload.ID, (payload, context) ->
            context.server().execute(() -> StoryStartService.start(context.server()))
        );
        ServerPlayNetworking.registerGlobalReceiver(SearchPcLocationOpenedPayload.ID, (payload, context) ->
            context.server().execute(() -> Phase2DoorArrowService.recordLocationScreenOpened(context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(SearchPcLocationClosedPayload.ID, (payload, context) ->
            context.server().execute(() -> Phase2DoorArrowService.recordLocationScreenClosed(context.player(), context.server()))
        );
        // キー本を確認したプレイヤーへHUDセリフを流し、完了後にphase4へ進む
        ServerPlayNetworking.registerGlobalReceiver(Phase4DialogueClosedPayload.ID, (payload, context) ->
            context.server().execute(() -> Phase4IntroDialogueService.start(context.player()))
        );
        // おなら音が終わったらHUDセリフを流し、その後にゾンビをスポーンさせる
        ServerPlayNetworking.registerGlobalReceiver(Phase5OnaraClosedPayload.ID, (payload, context) ->
            context.server().execute(() -> Phase5OnaraDialogueService.start(context.player()))
        );
    }
}
