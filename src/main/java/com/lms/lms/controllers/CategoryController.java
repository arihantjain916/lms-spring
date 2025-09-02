package com.lms.lms.controllers;

import com.github.slugify.Slugify;
import com.lms.lms.dto.request.CategoryReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.CategoryRes;
import com.lms.lms.modals.Category;
import com.lms.lms.repo.CategoryRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepo categoryRepo;

    @GetMapping("")
    public ResponseEntity<List<CategoryRes>> getAll() {
        List<CategoryRes> categories = categoryRepo.findCategoryCourseCounts()
                .stream()
                .map(result -> {
                    Category category = (Category) result[0];
                    Long courseCount = (Long) result[1];
                    return new CategoryRes(
                            category.getId(),
                            category.getName(),
                            category.getDescription(),
                            courseCount
                    );
                })
                .collect(Collectors.toList());
                
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

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

            if(Boolean.parseBoolean(categoryReq.getId())){
                System.out.println("v " + categoryReq.getId());
                category.setId(categoryReq.getId());
            }

            categoryRepo.save(category);
            return ResponseEntity.ok()
                    .body(new Default("Category Added Successfully", true, null));
        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Object> updateCategory(@Valid @RequestBody CategoryReq categoryReq) {
        try {
            Category existingCategory = categoryRepo.findById(categoryReq.getId()).orElse(null);

            if (existingCategory == null) {
                return ResponseEntity.badRequest()
                        .body(new Default("Category does not exist", false, null));
            }

            Slugify slugify = new Slugify();
            String slug = slugify.slugify(categoryReq.getName());

            Category categoryWithSameSlug = categoryRepo.findBySlug(slug).orElse(null);

            if (categoryWithSameSlug != null && !categoryWithSameSlug.getId().equals(existingCategory.getId())) {
                slug = slug + "-" + new Random().nextInt(10000);
            }

            existingCategory.setName(categoryReq.getName());
            existingCategory.setDescription(categoryReq.getDescription());
            existingCategory.setSlug(slug);

            categoryRepo.save(existingCategory);

            return ResponseEntity.ok()
                    .body(new Default("Category updated successfully", true, null));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new Default(e.getMessage(), false, null));
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteCategory(@PathVariable String id){
        try{
            Category category = categoryRepo.findById(id).orElse(null);
            if(category == null){
                return ResponseEntity.badRequest().body(new Default("Category do not exist", false, null));
            }
            categoryRepo.delete(category);
            return ResponseEntity.ok().body(new Default("Category Deleted Successfully", true, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null));
        }
    }
}
