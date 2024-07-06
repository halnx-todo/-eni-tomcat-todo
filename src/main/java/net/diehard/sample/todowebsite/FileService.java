package net.diehard.sample.todowebsite;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cette classe ne produit pas d'exception, mais trace des WARN logs
 */
public class FileService {

    private static final Logger _log = Logger.getLogger(FileService.class.getCanonicalName());


    public static String storeFile(MultipartFile file, String storageLocation) {
        if (file ==null){
            return null;
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                _log.warning("wrong path ;-)");
                return null;
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path fileStorageLocation = new File(storageLocation).toPath();
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            _log.log(Level.SEVERE, "Could not store file " + fileName + "", ex);
        }
        return null;
    }

    public static Resource loadFileAsResource(String storageLocation, String fileName) {
        try {
            Path fileStorageLocation = new File(storageLocation).toPath();
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            }
        } catch (MalformedURLException ex) {
            _log.log(Level.SEVERE, "Could not retrieve file " + fileName + "", ex);
        }
        return null;
    }

}
