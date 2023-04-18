package com.May2Beez.modules;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.utils.LogUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import static com.May2Beez.May2BeezQoL.MODID;

public class Module {
    @Expose
    @SerializedName("name")
    public String name;

    @Expose
    @SerializedName("toggled")
    private boolean toggled;

    @Expose
    @SerializedName("keyCode")
    private int keycode;

    public KeyBinding keyBinding;

    @Expose
    @SerializedName("disableOnFailsafe")
    public boolean disableOnFailsafe = true;

    public Module(String name, int keycode) {
        this.name = name;
        this.keycode = keycode;
        this.keyBinding = new KeyBinding(name, getKeycode(), MODID);
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    public Module(String name, KeyBinding keyBinding) {
        this.name = name;
        this.keycode = keyBinding.getKeyCode();
        this.keyBinding = keyBinding;
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    public Module(String name) {
        this.name = name;
        this.keycode = 0;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyBinding != null && keyBinding.isPressed()) {
            toggle();
        }
    }

    public void toggle() {
        this.toggled = !this.toggled;
        if (this.toggled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onEnable() {
        LogUtils.addMessage(getName() + " is enabled!", EnumChatFormatting.GREEN);
    }

    public boolean isKeybind() {
        return false;
    }

    public String getName() {
        return this.name;
    }

    public boolean isPressed() {
        return (this.keycode != 0 && Keyboard.isKeyDown(this.keycode) && isKeybind());
    }

    public int getKeycode() {
        return this.keycode;
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public void onDisable() {
        LogUtils.addMessage(getName() + " is disabled!", EnumChatFormatting.RED);
    }

    protected void useMiningSpeedBoost() {
        if (!May2BeezQoL.config.useMiningSpeed) return;

        if (May2BeezQoL.miningSpeedReady) {
            KeyBinding.setKeyBindState((Minecraft.getMinecraft()).gameSettings.keyBindAttack.getKeyCode(), false);
            if(Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(Minecraft.getMinecraft().thePlayer.inventory.currentItem) != null) {
                Minecraft.getMinecraft().playerController.sendUseItem(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(Minecraft.getMinecraft().thePlayer.inventory.currentItem));
                May2BeezQoL.miningSpeedReady = false;
            }
        }
    }
}