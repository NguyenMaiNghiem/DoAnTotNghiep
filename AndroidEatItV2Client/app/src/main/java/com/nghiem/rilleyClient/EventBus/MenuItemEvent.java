package com.nghiem.rilleyClient.EventBus;

import com.nghiem.rilleyClient.Model.MilkTeaModel;

public class MenuItemEvent {
    private boolean success;
    private MilkTeaModel milkTeaModel;

    public MenuItemEvent() {
    }

    public MenuItemEvent(boolean success, MilkTeaModel milkTeaModel) {
        this.success = success;
        this.milkTeaModel = milkTeaModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public MilkTeaModel getMilkTeaModel() {
        return milkTeaModel;
    }

    public void setMilkTeaModel(MilkTeaModel milkTeaModel) {
        this.milkTeaModel = milkTeaModel;
    }
}
