package com.dinilbositha.wasthirestaurant.model;

public class IntroSlide {
    private int image;
    private String title;
    private String desc;
private String buttonText;


    public IntroSlide(int image, String title, String desc, String buttonText) {
        this.image = image;
        this.title = title;
        this.desc = desc;
        this.buttonText = buttonText;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }
}
