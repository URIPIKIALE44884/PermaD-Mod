package com.permadeath.client;

import java.util.function.IntConsumer;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/** Deslizador de numeros enteros. Avisa al soltar el mouse para no saturar la red. */
public class IntSlider extends SliderWidget {
    private final String label;
    private final String suffix;
    private final int min;
    private final int max;
    private final IntConsumer onChange;
    private final Runnable releaseAction;

    public IntSlider(int x, int y, int width, int height, String label, String suffix, int min, int max, int value,
            IntConsumer onChange, Runnable releaseAction) {
        super(x, y, width, height, Text.empty(), (value - min) / (double) (max - min));
        this.label = label;
        this.suffix = suffix;
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        this.releaseAction = releaseAction;
        updateMessage();
    }

    private int current() {
        return min + (int) Math.round(this.value * (max - min));
    }

    @Override
    protected void updateMessage() {
        if (label == null) {
            return;
        }
        setMessage(Text.literal(label + ": " + current() + suffix));
    }

    @Override
    protected void applyValue() {
        onChange.accept(current());
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        releaseAction.run();
    }
}
