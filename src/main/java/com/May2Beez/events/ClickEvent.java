package com.May2Beez.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ClickEvent extends Event {
    @Cancelable
    public static class LeftClickEvent extends ClickEvent {
    }

    @Cancelable
    public static class RightClickEvent extends ClickEvent {
    }

    @Cancelable
    public static class MiddleClickEvent extends ClickEvent {
    }
}
