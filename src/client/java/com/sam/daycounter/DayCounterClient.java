package com.sam.daycounter;

import net.fabricmc.api.ClientModInitializer;
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

        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        while (openSettingsKey.wasPressed()) {
            client.setScreen(new DayCounterSettingsScreen(client.currentScreen));
        }

        if (!CONFIG.enabled || client.world == null) return;

        long day = client.world.getTimeOfDay() / 24000L + 1;
        String dayText = "Day " + day;

        // ===== LABEL RESOLUTION =====
        String label = null;
        int labelColor = 0xFFFFFF;

        if (CONFIG.labelMode.equals("AUTO")) {
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
        } else if (CONFIG.labelMode.equals("CUSTOM") && !CONFIG.customLabel.isBlank()) {
            label = CONFIG.customLabel;
            labelColor = 0x55FFFF;
        }
        // OFF → label stays null

        int x = 10;
        int y = 10;
        int padding = 4;

        context.getMatrices().push();
        context.getMatrices().scale(CONFIG.scale, CONFIG.scale, 1.0f);

        int width = client.textRenderer.getWidth(dayText);
        int height = 10;

        if (label != null) {
            width = Math.max(width, client.textRenderer.getWidth(label));
            height += 10;
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
            drawY += 10;
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
