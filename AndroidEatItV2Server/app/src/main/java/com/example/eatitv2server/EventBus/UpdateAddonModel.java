package com.example.eatitv2server.EventBus;

import com.example.eatitv2server.Model.AddonModel;

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
