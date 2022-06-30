package com.nghiem.rilleyShipper.eventbus;

import com.nghiem.rilleyShipper.model.MilkTeaModel;

public class MilkTeaSelectEvent {
    private MilkTeaModel milkTeaModel;

    public MilkTeaSelectEvent(MilkTeaModel milkTeaModel) {
        this.milkTeaModel = milkTeaModel;
    }

    public MilkTeaModel getMilkTeaModel() {
        return milkTeaModel;
    }

    public void setMilkTeaModel(MilkTeaModel milkTeaModel) {
        this.milkTeaModel = milkTeaModel;
    }
}
