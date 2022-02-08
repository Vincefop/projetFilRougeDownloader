package com.example.filestorage.services;

import com.example.filestorage.entities.Picture;
import com.example.filestorage.exceptions.MyPictureNotFoundException;
import com.example.filestorage.exceptions.PictureStorageException;
import com.example.filestorage.payloads.UploadFileResponse;
import com.example.filestorage.properties.PictureStorageProperties;
import com.example.filestorage.repositories.PictureRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * le constructeur
     * @param pictureStorageProperties
     */
    @Autowired
    public PictureService(PictureStorageProperties pictureStorageProperties){
        this.pictureStorageLocation = Paths.get(pictureStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        try{
            Files.createDirectories(this.pictureStorageLocation);
        }catch( Exception e){
            throw new PictureStorageException("Could not find the directory where the upload picture is", e);
        }
    }

    /**
     * Permet de sauvegarder un fichier sur le serveur
     * @param file
     * @return le fichier sauvegardé en réponse
     */
    public UploadFileResponse storeFile(MultipartFile file){
        // Je normalise le nom du fichier
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new PictureStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.pictureStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            UploadFileResponse response = this.getUploadFileResponse(file, fileName);
            this.save(response);
            return response;
        } catch (IOException ex) {
            throw new PictureStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }

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
