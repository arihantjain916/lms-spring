package com.lms.lms.controllers;

import com.github.slugify.Slugify;
import com.lms.lms.dto.request.CategoryReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.Category;
import com.lms.lms.repo.CategoryRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepo categoryRepo;

    @PostMapping("/add")
    public ResponseEntity<Object> createCategory(@Valid @RequestBody CategoryReq categoryReq){
        try{
            Slugify slugify = new Slugify();
            String slug = slugify.slugify(categoryReq.getName());

            Category isSlugExist = categoryRepo.findBySlug(slug).orElse(null);
            if(isSlugExist != null){
                Random random = new Random();
                int randomNum = random.nextInt(10000);
                slug = slug + "-" + randomNum;
            }
            Category category = new Category();

            category.setName(categoryReq.getName());
            category.setDescription(categoryReq.getDescription());
            category.setSlug(slug);

            categoryRepo.save(category);
            return ResponseEntity.ok()
                    .body(new Default("Category Added Successfully", true, null));
        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null));
        }
    }
}
