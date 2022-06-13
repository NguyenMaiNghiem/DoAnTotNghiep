package com.example.eatitv2server.EventBus;

import com.example.eatitv2server.Model.SizeModel;

public class SelectedSizeModel {

    private SizeModel sizeModel;

    public SelectedSizeModel(SizeModel sizeModel) {
        this.sizeModel = sizeModel;
    }

    public SizeModel getSizeModel() {
        return sizeModel;
    }

    public void setSizeModel(SizeModel sizeModel) {
        this.sizeModel = sizeModel;
    }
}
