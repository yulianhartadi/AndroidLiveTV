package com.app.item;

public class ItemChannel {

    private String id;
    private String channelUrl;
    private String channelImage;
    private String channelName;
    private String channelCategory;
    private String channelDescription;
    private String channelAvgRate;
    private boolean isTv;

    public ItemChannel() {
        // TODO Auto-generated constructor stub
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public void setChannelUrl(String url) {
        this.channelUrl = url;
    }


    public String getImage() {
        return channelImage;
    }

    public void setImage(String image) {
        this.channelImage = image;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelname) {
        this.channelName = channelname;
    }

    public String getDescription() {
        return channelDescription;
    }

    public void setDescription(String desc) {
        this.channelDescription = desc;
    }

    public boolean isTv() {
        return isTv;
    }

    public void setIsTv(boolean flag) {
        this.isTv = flag;
    }

    public String getChannelCategory() {
        return channelCategory;
    }

    public void setChannelCategory(String channelCategory) {
        this.channelCategory = channelCategory;
    }

    public String getChannelAvgRate() {
        return channelAvgRate;
    }

    public void setChannelAvgRate(String channelAvgRate) {
        this.channelAvgRate = channelAvgRate;
    }
}
