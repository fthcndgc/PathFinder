package com.yuncan.pathfinder.clas;

public class Users {

    public String kullaniciadi;
    public String image;

    public Users(){

    }

    public Users(String kullaniciadi, String image) {
        this.kullaniciadi = kullaniciadi;
        this.image = image;
    }

    public String getkullaniciadi() {
        return kullaniciadi;
    }

    public void setkullaniciadi(String kullaniciadi) {
        this.kullaniciadi = kullaniciadi;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
