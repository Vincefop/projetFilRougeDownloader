package com.example.filestorage.controllers;

import com.example.filestorage.payloads.UploadFileResponse;
import com.example.filestorage.services.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
public class PictureController {

    private static final Logger logger = LoggerFactory.getLogger(PictureController.class);

    @Autowired
    private PictureService pictureService;

    /**
     * Permet d'upload un fichier
     * @param file
     * @return
     */
    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info(file.getName() + " reçue");
        return this.pictureService.storeFile(file);
    }

    /**
     * Permet de upload plusieurs fichiers d'un coup
     * @param files
     * @return
     */
    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    /**
     * Permet de download un fichier
     * @param fileName
     * @param request
     * @return
     */
    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = this.pictureService.loadPictureAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // On dédinin le content type si il n'existe pas
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    @GetMapping("/downloadFileResult/{fileName:.+}")
    public ResponseEntity<Resource> downloadFileResult(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = this.pictureService.loadPictureResultAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // On dédinin le content type si il n'existe pas
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/downloadCFUResult/{fileName:.+}")
    public int downloadCFUResult(@PathVariable String fileName, HttpServletRequest request) {

        return this.pictureService.getCFUfromFilename(fileName);
    }

    @GetMapping("/files")
    public List<UploadFileResponse> findAll() {
        return this.pictureService.findAll();
    }
}
