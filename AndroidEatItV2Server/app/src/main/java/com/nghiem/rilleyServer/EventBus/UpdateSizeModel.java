package com.nghiem.rilleyServer.EventBus;

import com.nghiem.rilleyServer.Model.SizeModel;

import java.util.List;

public class UpdateSizeModel {

    private List<SizeModel> sizeModelList;

    public UpdateSizeModel() {

    }

    public UpdateSizeModel(List<SizeModel> sizeModelList) {
        this.sizeModelList = sizeModelList;
    }

    public List<SizeModel> getSizeModelList() {
        return sizeModelList;
    }

    public void setSizeModelList(List<SizeModel> sizeModelList) {
        this.sizeModelList = sizeModelList;
    }
}
