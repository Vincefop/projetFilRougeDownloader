package com.example.filestorage;

import com.example.filestorage.properties.PictureResultStorageProperties;
import com.example.filestorage.properties.PictureStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
// On active les propriété custom sur spring
@EnableConfigurationProperties({
        PictureStorageProperties.class,
        PictureResultStorageProperties.class
})
public class FileStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileStorageApplication.class, args);
    }

}
