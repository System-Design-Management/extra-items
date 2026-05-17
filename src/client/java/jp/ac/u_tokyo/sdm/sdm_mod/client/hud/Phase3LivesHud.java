package jp.ac.u_tokyo.sdm.sdm_mod.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class Phase3LivesHud implements HudElement {
    public static final Phase3LivesHud INSTANCE = new Phase3LivesHud();

    private static final int MAX_LIVES = 5;
    private static final int MARGIN = 8;
    private static final int PADDING_X = 8;
    private static final int PADDING_Y = 6;
    private static final String LABEL = "残機 ";
    private static final String HEART = "♥";

    // -1 = 非表示
    private int remainingLives = -1;

    private Phase3LivesHud() {
    }

    public void setRemainingLives(int lives) {
        this.remainingLives = lives;
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (remainingLives < 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        MutableText hearts = Text.empty();
        for (int i = 0; i < MAX_LIVES; i++) {
            Formatting color = i < remainingLives ? Formatting.RED : Formatting.DARK_GRAY;
            hearts.append(Text.literal(HEART).formatted(color));
        }
        Text display = Text.empty()
            .append(Text.literal(LABEL).formatted(Formatting.WHITE))
            .append(hearts);

        int textWidth = textRenderer.getWidth(display);
        int textHeight = textRenderer.fontHeight;
        int screenWidth = client.getWindow().getScaledWidth();

        int boxRight = screenWidth - MARGIN;
        int boxLeft = boxRight - PADDING_X * 2 - textWidth;
        int boxTop = MARGIN;
        int boxBottom = boxTop + PADDING_Y * 2 + textHeight;

        context.fill(boxLeft, boxTop, boxRight, boxBottom, 0xCC111111);
        context.drawBorder(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, 0xAAFFFFFF);
        context.drawText(textRenderer, display, boxLeft + PADDING_X, boxTop + PADDING_Y, 0xFFFFFFFF, true);
    }
}
