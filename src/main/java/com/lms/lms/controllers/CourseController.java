package com.lms.lms.controllers;

import com.lms.lms.dto.request.CourseReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.Courses;
import com.lms.lms.repo.CategoryRepo;
import com.lms.lms.repo.CoursesRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @GetMapping("/all")
    public List<Courses> getCourses()
    {
        return coursesRepo.findAll();
    }

    @GetMapping("/{id}")
    public Courses getCourseById(@PathVariable Long id)
    {
        return coursesRepo.findById(id).orElse(null);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCourse(@Valid @RequestBody CourseReq courses){
        try{
            var slug= courses.getSlug();

            var isSlugExist = coursesRepo.findBySlug(slug).orElse(null);

            var isCategoryExist = categoryRepo.findById(courses.getCategoryId()).orElse(null);


            if(isSlugExist != null){
                return ResponseEntity.badRequest().body(new Default("Slug Already Exist. Please try with another one", false, null));
            }

            if(isCategoryExist == null){
                return ResponseEntity.badRequest().body(new Default("Category Don't Exist", false, null));
            }

            Courses course = new Courses();
            course.setTitle(courses.getTitle());
            course.setSlug(slug);
            course.setDescription(courses.getDescription());
            course.setCategory(isCategoryExist);
            coursesRepo.save(course);

            return ResponseEntity.ok().body(new Default("Course Added Successfully", true, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCourse (@Valid @RequestBody CourseReq course){
        try{
            var id = course.getId();

            var isCourseExist = coursesRepo.findById(id).orElse(null);

            var isCategoryExist = categoryRepo.findById(course.getCategoryId()).orElse(null);

            if(isCourseExist == null){
                return ResponseEntity.badRequest().body(new Default("Invalid Course Id", false, null));
            }

            if(isCategoryExist == null){
                return ResponseEntity.badRequest().body(new Default("Category Don't Exist", false, null));
            }

            isCourseExist.setTitle(course.getTitle());
            isCourseExist.setSlug(course.getSlug());
            isCourseExist.setDescription(course.getDescription());
            isCourseExist.setCategory(isCategoryExist);
            coursesRepo.save(isCourseExist);

            return ResponseEntity.ok().body(new Default("Course Updated Successfully", true, null));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Default> deleteCourse(@PathVariable("id") Long id){
        try{
            var isCourseExist = coursesRepo.findById(id).orElse(null);
            if(isCourseExist == null){
                return  ResponseEntity.badRequest().body(new Default("Course Not Found", false, null));
            }

            coursesRepo.deleteById(id);
            return ResponseEntity.ok().body(new Default("Course Deleted Successfully", true, null));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null));
        }
    }
}
