package com.example.filestorage.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Picture {
    @Id
    private String id;
    private String filename;
    private String pictureDownloadUri;
    private String fileType;
    private long size;

    private int numberOfCFU;
}
