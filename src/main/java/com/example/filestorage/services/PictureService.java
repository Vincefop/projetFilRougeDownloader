package com.example.filestorage.services;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.net.SyslogOutputStream;
import com.example.filestorage.entities.Picture;
import com.example.filestorage.exceptions.MyPictureNotFoundException;
import com.example.filestorage.exceptions.PictureStorageException;
import com.example.filestorage.payloads.DownloadFileResponse;
import com.example.filestorage.payloads.UploadFileResponse;
import com.example.filestorage.payloads.UploadFileResultResponse;
import com.example.filestorage.properties.PictureResultStorageProperties;
import com.example.filestorage.properties.PictureStorageProperties;
import com.example.filestorage.repositories.PictureRepository;
import com.example.filestorage.contours.*;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class PictureService {

    // On défini le path
    private final Path pictureStorageLocation;
    private final Path pictureResultStorageLocation;

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private ModelMapper modelMapper;

    String fileName;
    String fileNameResult;
    int nbCFU;
    /**
     * le constructeur
     * @param pictureStorageProperties
     */
    @Autowired
    public PictureService(PictureStorageProperties pictureStorageProperties, PictureResultStorageProperties pictureResultStorageProperties){
        this.pictureStorageLocation = Paths.get(pictureStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        this.pictureResultStorageLocation = Paths.get(pictureResultStorageProperties.getDownloadDir()).toAbsolutePath().normalize();

        try{
            Files.createDirectories(this.pictureStorageLocation);
            Files.createDirectories(this.pictureResultStorageLocation);
        }catch( Exception e){
            throw new PictureStorageException("Could not find the directory where the upload picture is", e);
        }
    }

    public int getCFUfromFilename(String fileName){
        return this.pictureRepository.findByFilename(fileName).getNumberOfCFU();
    }

    /**
     * Permet de sauvegarder un fichier sur le serveur
     * @param file
     * @return le fichier sauvegardé en réponse
     */
    public UploadFileResponse storeFile(MultipartFile file){
        // Je normalise le nom du fichier
        fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid char
            // acters
            if(fileName.contains("..")) {
                throw new PictureStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.pictureStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            UploadFileResponse response = this.getUploadFileResponse(file, fileName);

            this.save(response);
            traiterImage(response,targetLocation.toString());
            return response;

        } catch (IOException ex) {
            throw new PictureStorageException("Could not store file " + fileName + ". Please try again!", ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void traiterImage(UploadFileResponse response, String targetLocation) throws Exception {
        //traitement de l'image

        TraitementImage trt=new TraitementImage();
        //Correction dans la bdd du nbre de cfu
        trt.lancerTraitement(targetLocation);
        nbCFU= trt.getNbCFU();
        response.setNumberOfCFU(nbCFU);
        this.save(response);


        //enregistrement de l'image result
        BufferedImage imgResult=trt.getPhotoFinale();

        fileNameResult = "result"+ this.fileName;
        MultipartFile fileResult =BufferedImage2MultiPartFile(imgResult);



        // Check if the file's name contains invalid characters
        if(fileNameResult.contains("..")) {
            throw new PictureStorageException("Sorry! Filename contains invalid path sequence " + fileNameResult);
        }

        // Copy file to the target location (Replacing existing file with the same name)
        Path targetLocationResult = this.pictureResultStorageLocation.resolve(this.fileNameResult);
        Files.copy(fileResult.getInputStream(), targetLocationResult, StandardCopyOption.REPLACE_EXISTING);
        UploadFileResultResponse responseResult =this.getUploadFileResponseWithResult(fileResult ,fileNameResult);

        this.saveResult(responseResult);





    }

    public MultipartFile BufferedImage2MultiPartFile(BufferedImage buf) throws IOException {
        //BufferedImage  Convert to  ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(buf, "jpg", out);
        //ByteArrayOutputStream  Convert to  byte[]
        byte[] imageByte = out.toByteArray();
        // Will  byte[]  Convert to  MultipartFile
        MultipartFile multipartFile = new ConvertToMultipartFile(imageByte, "newNamepic", this.fileNameResult, "jpg", imageByte.length);
        return multipartFile;
    }

    /**
     * Permet de récuperer un fichier sur le serveur
     * @param fileName
     * @return
     */
    public Resource loadPictureAsResource(String fileName) {
        try {
            Path filePath = this.pictureStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyPictureNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyPictureNotFoundException("File not found " + fileName, ex);
        }
    }


    /**
     * Permet de récuperer un fichier result sur le serveur
     * @param fileName
     * @return
     */

    public Resource loadPictureResultAsResource(String fileName) {
        try {
            Path filePath = this.pictureResultStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyPictureNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyPictureNotFoundException("File not found " + fileName, ex);
        }
    }

    /**
     * Permet de formater le UploadFileResponse
     * @param picture
     * @param fileName
     * @return l'uploadFileResponse formaté
     */
    private UploadFileResponse getUploadFileResponse(MultipartFile picture, String fileName) {
        // On défini L'uri du fichier pour le téléchargement
        String pictureDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, pictureDownloadUri, picture.getContentType(), picture.getSize(),-1);
    }

    private UploadFileResultResponse getUploadFileResponseWithResult(MultipartFile pictureResult, String fileNameResult){
        // On défini L'uri du fichier pour le téléchargement
        String pictureDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFileResult/")
                .path(fileNameResult)
                .toUriString();

        System.out.println("filenameresult : "+fileNameResult);
        System.out.println("picturedownload : "+pictureDownloadUri);


        return new UploadFileResultResponse(fileNameResult, pictureDownloadUri, pictureResult.getContentType(), pictureResult.getSize(), nbCFU);
    }

    /**
     * Permet de sauvegarder en BDD un fichier
     * @param file
     * @return
     */
    public UploadFileResponse save(UploadFileResponse file) {
        Picture pictureToSave = this.modelMapper.map(file, Picture.class);
        Picture pictureSaved = this.pictureRepository.save(pictureToSave);
        return this.modelMapper.map(pictureSaved, UploadFileResponse.class);
    }
    public UploadFileResultResponse saveResult(UploadFileResultResponse file) {
        Picture pictureToSave = this.modelMapper.map(file, Picture.class);
        Picture pictureSaved = this.pictureRepository.save(pictureToSave);
        return this.modelMapper.map(pictureSaved, UploadFileResultResponse.class);
    }

    /**
     * Permet de retourner la liste des fichiers sauvegardé
     * @return
     */
    public List<UploadFileResponse> findAll() {
        List<UploadFileResponse> uploadFileResponseList = new ArrayList<>();
        this.pictureRepository.findAll().forEach(file -> {
            uploadFileResponseList.add(modelMapper.map(file, UploadFileResponse.class));
        });
        return uploadFileResponseList;
    }



}
