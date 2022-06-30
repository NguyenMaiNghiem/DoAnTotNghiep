package com.nghiem.rilleyServer.EventBus;

import com.nghiem.rilleyServer.Model.SugarModel;

import java.util.List;

public class UpdateSugarModel {

    private List<SugarModel> sugarModel;

    public UpdateSugarModel() {

    }

    public UpdateSugarModel(List<SugarModel> sugarModel) {
        this.sugarModel = sugarModel;
    }

    public List<SugarModel> getAddonModel() {
        return sugarModel;
    }

    public void setAddonModel(List<SugarModel> sugarModel) {
        this.sugarModel = sugarModel;
    }
}
