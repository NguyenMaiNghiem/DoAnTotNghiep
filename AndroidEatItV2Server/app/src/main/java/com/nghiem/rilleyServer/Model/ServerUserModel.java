package com.nghiem.rilleyServer.Model;

public class ServerUserModel {
    private String uid, name, phone, milktea;
    private boolean active;

    public ServerUserModel() {
    }

    public ServerUserModel(String uid, String name, String phone, boolean active) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.active = active;
    }

    public String getMilktea() {
        return milktea;
    }

    public void setMilktea(String milktea) {
        this.milktea = milktea;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "ServerUserModel{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", milktea='" + milktea + '\'' +
                ", active=" + active +
                '}';
    }
}
