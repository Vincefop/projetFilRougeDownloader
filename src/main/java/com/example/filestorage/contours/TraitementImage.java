package com.example.filestorage.contours;

import java.awt.image.BufferedImage;

public class TraitementImage {
    private String url;
    private int nbCFU;
    private BufferedImage photoOriginale;
    private BufferedImage photoFinale;

    public TraitementImage() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNbCFU() {
        return nbCFU;
    }

    public void setNbCFU(int nbCFU) {
        this.nbCFU = nbCFU;
    }



    public BufferedImage getPhotoFinale() {
        return photoFinale;
    }

    public void setPhotoFinale(BufferedImage photoFinale) {
        this.photoFinale = photoFinale;
    }

    public void lancerTraitement(String url) throws Exception {
        System.out.println("1");
        this.setUrl(url);
        //on passe les param
        System.out.println("2");
        String[] args=new String[1];
        System.out.println(args[0]);
        args[0]=this.getUrl();
        System.out.println(args[0]);
        FindContours.test(args);
        System.out.println("3");
        this.setNbCFU(FindContours.getNbCFU());
        System.out.println("4");

        this.setPhotoFinale(FindContours.getImageFinale());
        System.out.println("5");
    }
}
