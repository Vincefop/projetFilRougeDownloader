package com.example.filestorage.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

// On définis une propriété custom et on indique à spring ou se trouve le dossier des fichiers téléchargés
@ConfigurationProperties("file")
public class PictureStorageProperties {
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
