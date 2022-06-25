package com.nghiem.rilleyServer.EventBus;

import com.nghiem.rilleyServer.Model.AddonModel;

public class SelectedAddonModel {

    AddonModel addonModel;

    public SelectedAddonModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }


    public AddonModel getAddonModel() {
        return addonModel;
    }

    public void setAddonModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }
}
