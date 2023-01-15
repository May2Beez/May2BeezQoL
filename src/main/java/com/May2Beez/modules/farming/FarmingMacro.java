package com.May2Beez.modules.farming;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.RotationUtils;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Objects;

import static com.May2Beez.May2BeezQoL.MODID;
import static com.May2Beez.May2BeezQoL.modules;

public class FarmingMacro extends Module {
    private final KeyBinding startLeft = new KeyBinding("Farming macro left", Keyboard.KEY_COMMA, MODID + " - Farming");
    private final KeyBinding startRight = new KeyBinding("Farming macro right", Keyboard.KEY_PERIOD, MODID + " - Farming");
    private Direction direction;
    private Direction lastDirection;
    private long startGoingForward = 0;

    private enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
    public FarmingMacro() {
        super("Farming Macro");
        ClientRegistry.registerKeyBinding(startLeft);
        ClientRegistry.registerKeyBinding(startRight);
    }

    @Override
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (startLeft.isPressed()) {
            direction = Direction.LEFT;
            toggle();
        }
        if (startRight.isPressed()) {
            direction = Direction.RIGHT;
            toggle();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        float pitch = Minecraft.getMinecraft().thePlayer.rotationPitch;
        switch (May2BeezQoL.config.lookingDirection) {
            case 0: return;
            case 1: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, 0f), May2BeezQoL.config.cameraSpeed);
                break;
            }
            case 2: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, 45f), May2BeezQoL.config.cameraSpeed);
                break;
            }
            case 3: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, 90f), May2BeezQoL.config.cameraSpeed);
                break;
            }
            case 4: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, 135f), May2BeezQoL.config.cameraSpeed);
                break;
            }
            case 5: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, 180f), May2BeezQoL.config.cameraSpeed);
                break;
            }
            case 6: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, -45f), May2BeezQoL.config.cameraSpeed);
                break;
            }
            case 7: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, -90f), May2BeezQoL.config.cameraSpeed);
                break;
            }
            case 8: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, -135f), May2BeezQoL.config.cameraSpeed);
                break;
            }
            case 9: {
                RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, -180f), May2BeezQoL.config.cameraSpeed);
                break;
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isToggled()) return;
        if (SkyblockUtils.hasOpenContainer()) return;

        double currentPosition = 0;

        if (May2BeezQoL.config.XorZindex == 0) {
            currentPosition = Minecraft.getMinecraft().thePlayer.posX;
        } else if (May2BeezQoL.config.XorZindex == 1) {
            currentPosition = Minecraft.getMinecraft().thePlayer.posZ;
        }

        if (direction == Direction.LEFT) {
            if (Math.abs(Float.parseFloat(May2BeezQoL.config.leftWall) - currentPosition) <= 0.5) {
                if (May2BeezQoL.config.forwardMs != 0) {
                    lastDirection = direction;
                    direction = Direction.UP;
                    startGoingForward = System.currentTimeMillis();
                } else {
                    lastDirection = direction;
                    direction = Direction.RIGHT;
                }
            }
        } else if (direction == Direction.RIGHT) {
            if (Math.abs(Float.parseFloat(May2BeezQoL.config.rightWall) - currentPosition) <= 0.5) {
                if (May2BeezQoL.config.forwardMs != 0) {
                    lastDirection = direction;
                    direction = Direction.UP;
                    startGoingForward = System.currentTimeMillis();
                } else {
                    lastDirection = direction;
                    direction = Direction.LEFT;
                }
            }
        } else if (direction == Direction.UP) {
            if (System.currentTimeMillis() - startGoingForward >= May2BeezQoL.config.forwardMs) {
                if (lastDirection == Direction.LEFT) {
                    lastDirection = direction;
                    direction = Direction.RIGHT;
                } else if (lastDirection == Direction.RIGHT){
                    lastDirection = direction;
                    direction = Direction.LEFT;
                }
            }
        }

        if (modules.stream().noneMatch(module -> Objects.equals(module.getName(), "Crop Alert") && module.isToggled()))
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), true);

        if (direction == Direction.LEFT) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), true);
        } else if (direction == Direction.RIGHT) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
            if (May2BeezQoL.config.backInsteadOfRight) {
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), true);
            } else {
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
            }
        } else if (direction == Direction.UP) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
            if (lastDirection == Direction.RIGHT && May2BeezQoL.config.backInsteadOfRight) {
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
            } else {
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
            }
        }
    }
}
