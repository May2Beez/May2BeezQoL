package com.May2Beez.gui;

import com.May2Beez.May2BeezQoL;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChangeLocationGUI extends GuiScreen {

    private Callable<Rectangle> drawFunction;
    private BiFunction<Integer, Integer, Void> saveFunction;
    private Function<Float, Void> saveScaleFunction;
//    private static Rectangle startLocation;
    private int xOffset;
    private int yOffset;

    private boolean isDragging = false;

    public static void open(Callable<Rectangle> drawFunction, BiFunction<Integer, Integer, Void> saveFunction) {
        open(drawFunction, saveFunction, null);
    }

    public static void open(Callable<Rectangle> drawFunction, BiFunction<Integer, Integer, Void> saveFunction, Function<Float, Void> saveScaleFunction) {
        ChangeLocationGUI gui = new ChangeLocationGUI();
        gui.drawFunction = drawFunction;
        gui.saveFunction = saveFunction;
        gui.saveScaleFunction = saveScaleFunction;
        try {
            drawFunction.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        May2BeezQoL.display = gui;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawRect(0, 0, width, height, 0x80000000);
        onMouseMove();

        try {
            Rectangle point = drawFunction.call();
            if (isDragging || (mouseX >= point.x && mouseX <= point.x + point.width && mouseY >= point.y && mouseY <= point.y + point.height)) {
                drawRect(point.x, point.y, point.x + point.width, point.y + point.height, new Color(44, 44, 44, 200).getRGB());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int dwheel = Mouse.getEventDWheel();
        if (dwheel != 0 && saveScaleFunction != null) {
            saveScaleFunction.apply(dwheel > 0 ? 0.1f : -0.1f);
            initGui();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        Rectangle point = null;
        try {
            point = drawFunction.call();
            if (mouseX >= point.x && mouseX <= point.x + point.width && mouseY >= point.y && mouseY <= point.y + point.height) {
                isDragging = true;
                xOffset = mouseX - point.x;
                yOffset = mouseY - point.y;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        isDragging = false;
        May2BeezQoL.config.markDirty();
        May2BeezQoL.config.writeData();
    }

    private void onMouseMove() {
        if (!isDragging) return;

        int x = Mouse.getEventX() * width / mc.displayWidth - xOffset;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1 - yOffset;

        Rectangle startLocation = null;
        try {
            startLocation = drawFunction.call();
            startLocation.x = x;
            startLocation.y = y;

            if (startLocation.x < 0) startLocation.x = 0;
            if (startLocation.y < 0) startLocation.y = 0;
            if (startLocation.x + startLocation.width > width) startLocation.x = width - startLocation.width;
            if (startLocation.y + startLocation.height > height) startLocation.y = height - startLocation.height;

            saveFunction.apply(startLocation.x, startLocation.y);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
