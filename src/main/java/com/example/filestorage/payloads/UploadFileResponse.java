package com.example.filestorage.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;

// On customise le payload JSON (Sorte de DTO pour JSON)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileResponse {
    private String filename;
    private String pictureDownloadUri;
    private String fileType;
    private long size;
    private int numberOfCFU;


/*
    private String fileNameResult;
    private String pictureDownloadUriResult;
    private String fileTypeResult;
*/
}
