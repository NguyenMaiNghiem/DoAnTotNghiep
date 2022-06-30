package com.nghiem.rilleyClient.Model;

public class ChatInfoModel {
    private String createName, lastMessage;
    private long createDate, lastUpdate;

    public ChatInfoModel() {
    }

    public ChatInfoModel(String createName, String lastMessage, long createDate, long lastUpdate) {
        this.createName = createName;
        this.lastMessage = lastMessage;
        this.createDate = createDate;
        this.lastUpdate = lastUpdate;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
