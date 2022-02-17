package com.example.filestorage.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.awt.image.BufferedImage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadFileResponse {
    private String url;
    private int nbCFU;
    private BufferedImage photoFinale;
    private String fileName;
}
