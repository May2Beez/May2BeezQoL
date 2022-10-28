package com.May2Beez;

import com.May2Beez.utils.SkyblockUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import static com.May2Beez.SkyblockMod.MODID;

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

    private KeyBinding keyBinding;

    private boolean devOnly;

    public Module(String name, int keycode) {
        this.name = name;
        this.keycode = keycode;
        this.keyBinding = new KeyBinding(name, getKeycode(), MODID);
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
        SkyblockUtils.SendInfo(" §r§2is enabled!", true, getName());
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
        SkyblockUtils.SendInfo(" §r§cis disabled!", false, getName());
    }

    public void setDevOnly(boolean devOnly) {
        this.devOnly = devOnly;
    }

    public boolean isDevOnly() {
        return this.devOnly;
    }
}