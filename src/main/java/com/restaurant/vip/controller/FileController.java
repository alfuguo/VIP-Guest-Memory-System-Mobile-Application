package com.restaurant.vip.controller;

import com.restaurant.vip.dto.FileUploadResponse;
import com.restaurant.vip.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileController {
    
    private final FileUploadService fileUploadService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Autowired
    public FileController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }
    
    /**
     * Upload guest photo
     * POST /api/files/guests/{guestId}/photo
     */
    @PostMapping("/guests/{guestId}/photo")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    public ResponseEntity<FileUploadResponse> uploadGuestPhoto(
            @PathVariable Long guestId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            FileUploadResponse response = fileUploadService.uploadGuestPhoto(file, guestId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            FileUploadResponse errorResponse = new FileUploadResponse();
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            FileUploadResponse errorResponse = new FileUploadResponse();
            errorResponse.setMessage("Failed to upload file: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Serve uploaded files
     * GET /api/files/guests/{filename}
     */
    @GetMapping("/guests/{filename:.+}")
    public ResponseEntity<Resource> serveGuestPhoto(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, "guests").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = determineContentType(filename);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete guest photo
     * DELETE /api/files/guests/{filename}
     */
    @DeleteMapping("/guests/{filename:.+}")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteGuestPhoto(@PathVariable String filename) {
        String relativePath = "guests/" + filename;
        boolean deleted = fileUploadService.deleteFile(relativePath);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Check if file exists
     * HEAD /api/files/guests/{filename}
     */
    @RequestMapping(value = "/guests/{filename:.+}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkFileExists(@PathVariable String filename) {
        String relativePath = "guests/" + filename;
        boolean exists = fileUploadService.fileExists(relativePath);
        
        if (exists) {
            try {
                long fileSize = fileUploadService.getFileSize(relativePath);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .build();
            } catch (IOException e) {
                return ResponseEntity.ok().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Private helper methods
    
    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}