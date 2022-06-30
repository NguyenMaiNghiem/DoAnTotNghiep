package com.nghiem.rilleyServer.EventBus;

public class SugarSizeEditEvent {
    private boolean addon;
    private int pos;

    public SugarSizeEditEvent(boolean addon, int pos) {
        this.addon = addon;
        this.pos = pos;
    }

    public boolean isSugar() {
        return addon;
    }

    public void setAddon(boolean addon) {
        this.addon = addon;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
