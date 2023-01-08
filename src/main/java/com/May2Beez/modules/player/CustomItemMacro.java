package com.May2Beez.modules.player;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.modules.Module;
import com.May2Beez.commands.UseCooldown;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import static com.May2Beez.utils.SkyblockUtils.findItemInHotbar;

public class CustomItemMacro extends Module {
    private Thread thread;
    private int milis = 0;
    private boolean working = false;

    public CustomItemMacro() {
        super("Custom Item Macro", new KeyBinding("Custom Item Macro", Keyboard.KEY_NONE, May2BeezQoL.MODID + " - Player"));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END || working) return;
        if (!isToggled()) return;
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(() -> {
                try {
                    working = true;
                    int prevItem = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
                    for (String i : UseCooldown.RCitems.keySet()) {
                        if (milis % Math.floor(UseCooldown.RCitems.get(i)/100) == 0) {
                            int slot = findItemInHotbar(i);
                            if (slot != -1) {
                                Minecraft.getMinecraft().thePlayer.inventory.currentItem = slot;
                                Minecraft.getMinecraft().playerController.sendUseItem(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(slot));
                            }
                        }
                    }
                    for (String i : UseCooldown.LCitems.keySet()) {
                        if (milis % Math.floor(UseCooldown.LCitems.get(i)/100) == 0) {
                            int slot = findItemInHotbar(i);
                            if (slot != -1) {
                                Minecraft.getMinecraft().thePlayer.inventory.currentItem = slot;
                                Thread.sleep(100);
                                Minecraft.getMinecraft().thePlayer.swingItem();
                            }
                        }
                    }
                    Minecraft.getMinecraft().thePlayer.inventory.currentItem = prevItem;
                    milis++;
                    Thread.sleep(100);
                    working = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Custom Item Use");
            thread.start();
        }
    }
}
