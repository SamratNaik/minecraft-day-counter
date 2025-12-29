package com.sam.daycounter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

public class DayCounterClient implements ClientModInitializer {

    public static DayCounterConfig CONFIG;
    private static KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        CONFIG = DayCounterConfig.load();

        openSettingsKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.daycounter.settings",
                        GLFW.GLFW_KEY_K,
                        "key.categories.daycounter"
                )
        );

        // Handle key input on client tick (NOT in render)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.wasPressed()) {
                client.setScreen(new DayCounterSettingsScreen(client.currentScreen));
            }
        });

        // HUD rendering (lambda avoids method reference issues)
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            render(drawContext);
        });
    }

    private void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!CONFIG.enabled || client.world == null) return;

        long day = client.world.getTimeOfDay() / 24000L + 1;
        String dayText = "Day " + day;

        // ===== LABEL RESOLUTION =====
        String label = null;
        int labelColor = 0xFFFFFF;

        if ("AUTO".equals(CONFIG.labelMode)) {
            if (client.world.getLevelProperties().isHardcore()) {
                label = "HARDCORE";
                labelColor = 0xFF5555;
            } else {
                GameMode gm = client.interactionManager != null
                        ? client.interactionManager.getCurrentGameMode()
                        : null;

                if (gm == GameMode.CREATIVE) {
                    label = "CREATIVE";
                    labelColor = 0xFFFF55;
                } else {
                    label = "SURVIVAL";
                    labelColor = 0x55FF55;
                }
            }
        } else if ("CUSTOM".equals(CONFIG.labelMode) && !CONFIG.customLabel.isBlank()) {
            label = CONFIG.customLabel;
            labelColor = 0x55FFFF;
        }
        // OFF → label remains null

        int x = 10;
        int y = 10;
        int padding = 4;

        context.getMatrices().push();
        context.getMatrices().scale(CONFIG.scale, CONFIG.scale, 1.0f);

        int lineHeight = client.textRenderer.fontHeight;

        int width = client.textRenderer.getWidth(dayText);
        int height = lineHeight;

        if (label != null) {
            width = Math.max(width, client.textRenderer.getWidth(label));
            height += lineHeight;
        }

        if (CONFIG.showBackground) {
            context.fill(
                    x - padding,
                    y - padding,
                    x + width + padding,
                    y + height + padding,
                    0x90000000
            );
        }

        int drawY = y;

        if (label != null) {
            context.drawTextWithShadow(
                    client.textRenderer,
                    label,
                    x,
                    drawY,
                    labelColor
            );
            drawY += lineHeight;
        }

        context.drawTextWithShadow(
                client.textRenderer,
                dayText,
                x,
                drawY,
                0xFFFFFF
        );

        context.getMatrices().pop();
    }
}
