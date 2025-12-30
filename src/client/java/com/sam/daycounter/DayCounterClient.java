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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.wasPressed()) {
                client.setScreen(new DayCounterSettingsScreen(client.currentScreen));
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickDelta) -> renderHud(ctx));
    }

    private static void renderHud(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!CONFIG.enabled || client.world == null) return;

        long day = client.world.getTimeOfDay() / 24000L + 1;
        String dayText = "Day " + day;

        String label = null;
        int labelColor = 0xFFFFFFFF; // default white


        if ("AUTO".equals(CONFIG.labelMode)) {
            if (client.world.getLevelProperties().isHardcore()) {
                label = "HARDCORE";
                labelColor = 0xFFFF5555;
            } else if (client.interactionManager != null &&
                    client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                label = "CREATIVE";
                labelColor = 0xFFFFFF55;
            } else {
                label = "SURVIVAL";
                labelColor = 0xFF55FF55;
            }
        } else if ("CUSTOM".equals(CONFIG.labelMode) && !CONFIG.customLabel.isBlank()) {
            label = CONFIG.customLabel;
            labelColor = 0xFF55FFFF;
        }

        float s = CONFIG.scale;

        int x = Math.round(10 * s);
        int y = Math.round(10 * s);
        int padding = Math.round(4 * s);
        int spacing = Math.round(2 * s);

        int lineHeight = client.textRenderer.fontHeight;



        int width = client.textRenderer.getWidth(dayText);
        int height = lineHeight;

        if (label != null) {
            width = Math.max(width, client.textRenderer.getWidth(label));
            height += lineHeight;
        }

        if (CONFIG.showBackground) {
            ctx.fill(
                    x - padding,
                    y - padding,
                    x + width + padding,
                    y + height + padding,
                    0x90000000
            );
        }

        int drawY = y;

        if (label != null) {
            ctx.drawText(
                    client.textRenderer,
                    label,
                    x,
                    drawY,
                    labelColor,
                    false
            );

            drawY += lineHeight+1;
        }

        ctx.drawText(
                client.textRenderer,
                dayText,
                x,
                drawY,
                0xFFFFFFFF,
                false
        );

    }
}
