package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// remainingLives: 残り残機数。-1 で非表示。
public record Phase3LivesPayload(int remainingLives) implements CustomPayload {
    public static final CustomPayload.Id<Phase3LivesPayload> ID =
        new CustomPayload.Id<>(Identifier.of(SdmMod.MOD_ID, "phase3_lives"));
    public static final PacketCodec<RegistryByteBuf, Phase3LivesPayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> buf.writeInt(payload.remainingLives()),
            buf -> new Phase3LivesPayload(buf.readInt())
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
