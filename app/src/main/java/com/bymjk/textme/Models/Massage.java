package com.bymjk.textme.Models;

public class Massage {

    private String massageId;
    private String massage;
    private String senderId;

    private String imageUrl;
    private long timestamp;
    private int feeling = -1;

    public Massage() {
    }

    public Massage(String massage, String senderId, long timestamp) {
        this.massage = massage;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }


    public String getMassageId() {
        return massageId;
    }

    public void setMassageId(String massageId) {
        this.massageId = massageId;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    public void getMassageId(String massageId) {
    }
}
