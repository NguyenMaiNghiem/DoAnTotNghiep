package com.nghiem.rilleyServer.EventBus;

import com.nghiem.rilleyServer.Model.AddonModel;

import java.util.List;

public class UpdateAddonModel {

    private List<AddonModel> addonModel;

    public UpdateAddonModel() {

    }

    public UpdateAddonModel(List<AddonModel> addonModel) {
        this.addonModel = addonModel;
    }

    public List<AddonModel> getAddonModel() {
        return addonModel;
    }

    public void setAddonModel(List<AddonModel> addonModel) {
        this.addonModel = addonModel;
    }
}
