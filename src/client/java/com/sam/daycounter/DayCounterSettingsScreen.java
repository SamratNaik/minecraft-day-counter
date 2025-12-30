package com.sam.daycounter;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class DayCounterSettingsScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget customField;

    public DayCounterSettingsScreen(Screen parent) {
        super(Text.literal("Day Counter"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 4 + 10;

        // HUD toggle
        addToggle("HUD", DayCounterClient.CONFIG.enabled,
                v -> DayCounterClient.CONFIG.enabled = v, cx, y);

        // Label mode toggle
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Label Mode: " + DayCounterClient.CONFIG.labelMode),
                b -> {
                    DayCounterClient.CONFIG.labelMode = switch (DayCounterClient.CONFIG.labelMode) {
                        case "AUTO" -> "CUSTOM";
                        case "CUSTOM" -> "OFF";
                        default -> "AUTO";
                    };
                    b.setMessage(Text.literal("Label Mode: " + DayCounterClient.CONFIG.labelMode));
                    DayCounterConfig.save(DayCounterClient.CONFIG);
                }
        ).dimensions(cx - 100, y += 24, 200, 20).build());

        // Custom label input
        customField = new TextFieldWidget(
                textRenderer,
                cx - 100,
                y += 24,
                200,
                20,
                Text.literal("Custom Label")
        );
        customField.setText(DayCounterClient.CONFIG.customLabel);
        customField.setChangedListener(text -> {
            DayCounterClient.CONFIG.customLabel = text;
            DayCounterConfig.save(DayCounterClient.CONFIG);
        });
        addSelectableChild(customField);

        // Background toggle
        addToggle("Background", DayCounterClient.CONFIG.showBackground,
                v -> DayCounterClient.CONFIG.showBackground = v, cx, y += 28);

        // Scale slider
        addDrawableChild(new SliderWidget(
                cx - 100, y += 28, 200, 20,
                Text.literal("Hud Padding"),
                (DayCounterClient.CONFIG.scale - 0.75f) / 1.25f
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Hud Padding: " + String.format("%.2f", DayCounterClient.CONFIG.scale)));
            }

            @Override
            protected void applyValue() {
                DayCounterClient.CONFIG.scale = 0.75f + (float) value * 1.25f;
                DayCounterConfig.save(DayCounterClient.CONFIG);
            }
        });

        // Done button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                b -> client.setScreen(parent)
        ).dimensions(cx - 50, y += 36, 100, 20).build());
    }

    /* Disable blur (1.21+) */
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xC0101010);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);

        int centerX = width / 2;

        // Title (ARGB colors!)
        ctx.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Day Counter"),
                centerX,
                20,
                0xFFFFFFFF
        );

        ctx.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("by samfx"),
                centerX,
                34,
                0xFFAAAAAA
        );

        customField.render(ctx, mx, my, delta);
    }

    private void addToggle(String label, boolean initial, ToggleConsumer consumer, int cx, int y) {
        addDrawableChild(ButtonWidget.builder(
                Text.literal(label + ": " + initial),
                b -> {
                    boolean v = !b.getMessage().getString().endsWith("true");
                    consumer.set(v);
                    b.setMessage(Text.literal(label + ": " + v));
                    DayCounterConfig.save(DayCounterClient.CONFIG);
                }
        ).dimensions(cx - 100, y, 200, 20).build());
    }

    private interface ToggleConsumer {
        void set(boolean value);
    }
}
