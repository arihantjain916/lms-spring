package com.lms.lms.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.github.slugify.Slugify;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.Asset;
import com.lms.lms.modals.AssetMeta;
import com.lms.lms.repo.AssetMetaRepo;
import com.lms.lms.repo.AssetRepo;
import com.lms.lms.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

@RestController
public class HelperController {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private AssetRepo assetRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AssetMetaRepo assetMetaRepo;

    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<Default> uploadFile(@RequestParam("file") MultipartFile file) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();

            var isUserExist = userRepo.findById(user.getUsername()).orElse(null);

            if (isUserExist == null) {
                return ResponseEntity.badRequest().body(new Default("User don't exist", false, null, null));
            }
            List<String> allowedTypes = Arrays.asList(
                    "application/pdf",
                    "image/png",
                    "image/jpeg",
                    "audio/mpeg",
                    "audio/mpeg3",
                    "video/mp4"
            );

            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType)) {
                return ResponseEntity
                        .badRequest()
                        .body(new Default("Invalid file type. Allowed: PDF, PNG, JPG, JPEG, MP3, MPEG, MP4", false, null, null));
            }
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();

            var res = cloudinary.uploader().upload(convFile, ObjectUtils.asMap("folder", "/lms/"));

            Asset asset = new Asset();
            asset.setName(file.getOriginalFilename());
            asset.setUrl(res.get("secure_url").toString());
            asset.setPublic_id(res.get("public_id").toString());
            asset.setType(res.get("resource_type").toString());
            asset.setUser(isUserExist);
            assetRepo.save(asset);

            AssetMeta assetMeta = new AssetMeta();
            assetMeta.setAsset(asset);
            assetMeta.setSize(((Number) res.get("bytes")).longValue());
            assetMeta.setExtension(res.get("format").toString());
            assetMeta.setMimeType(file.getContentType());
            assetMeta.setDescription("");
            assetMeta.setVisibility("null");
            assetMetaRepo.save(assetMeta);

//
//            String contentType = file.getContentType();
//            if (contentType == null || !allowedTypes.contains(contentType)) {
//                return ResponseEntity
//                        .badRequest()
//                        .body(new Default("Invalid file type. Allowed: PDF, PNG, JPG, JPEG, MP3, MPEG", false, null, null));
//            }
//            String filePath = System.getProperty("user.dir") + "/Uploads" + File.separator + file.getOriginalFilename();
//            FileOutputStream fout = new FileOutputStream(filePath);
//            fout.write(file.getBytes());
//
//            // Closing the connection
//            fout.close();
//
//            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
//                    .path("/Uploads/")
//                    .path(file.getOriginalFilename())
//                    .toUriString();
            return ResponseEntity.ok().body(new Default("File Uploaded Successfully", true, null, res.get("secure_url").toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/slug/{title}")
    public ResponseEntity<Default> generateSlug(@PathVariable String title) {
        try {
            Slugify slugify = new Slugify();
            String slug = slugify.slugify(title);

            return ResponseEntity.ok().body(new Default("Slug Generated Successfully", true, null, slug));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
