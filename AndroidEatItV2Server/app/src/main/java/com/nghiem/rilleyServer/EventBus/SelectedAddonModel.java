package com.nghiem.rilleyServer.EventBus;

import com.nghiem.rilleyServer.Model.SugarModel;

public class SelectedAddonModel {

    SugarModel sugarModel;

    public SelectedAddonModel(SugarModel sugarModel) {
        this.sugarModel = sugarModel;
    }


    public SugarModel getAddonModel() {
        return sugarModel;
    }

    public void setAddonModel(SugarModel sugarModel) {
        this.sugarModel = sugarModel;
    }
}
