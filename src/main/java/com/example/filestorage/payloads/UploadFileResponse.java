package com.example.filestorage.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
