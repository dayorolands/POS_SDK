package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 12/13/2017.
 */

public class MainMenuItem {
    private int imageId;
    private String text;

    public MainMenuItem(int imageId, String text)
    {
        setImageId(imageId);
        setText(text);
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
