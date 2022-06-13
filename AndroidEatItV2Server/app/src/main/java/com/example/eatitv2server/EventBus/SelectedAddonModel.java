package com.example.eatitv2server.EventBus;

import com.example.eatitv2server.Model.AddonModel;

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
