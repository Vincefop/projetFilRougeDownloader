package com.example.filestorage.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


// On définis une propriété custom et on indique à spring ou se trouve le dossier des fichiers téléchargés
@ConfigurationProperties("fileresult")
public class PictureResultStorageProperties {
    private String downloadDir;

    public String getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
    }
}

/*
@ConfigurationProperties("nul")
public class PictureResultStorageProperties {}

 */