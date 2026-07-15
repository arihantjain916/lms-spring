package com.lms.lms.controllers;

import com.github.slugify.Slugify;
import com.lms.lms.dto.request.ApplicationStatusReq;
import com.lms.lms.dto.request.ProgramApplicationReq;
import com.lms.lms.dto.request.ProgramReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.dto.response.ProgramApplicationRes;
import com.lms.lms.dto.response.ProgramRes;
import com.lms.lms.modals.Program;
import com.lms.lms.modals.ProgramApplication;
import com.lms.lms.repo.ProgramApplicationRepo;
import com.lms.lms.repo.ProgramRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/programs")
public class ProgramController {

    @Autowired
    private ProgramRepo programRepo;

    @Autowired
    private ProgramApplicationRepo programApplicationRepo;

    @GetMapping("")
    public ResponseEntity<?> getPrograms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            Page<Program> programs = programRepo.findByIsActiveTrue(pageable);
            List<ProgramRes> programList = programs
                    .stream()
                    .map(this::toProgramRes)
                    .toList();

            PaginatedResponse<ProgramRes> paginatedResponse = new PaginatedResponse<>(
                    "Programs Fetched Successfully",
                    true,
                    programList,
                    programs.getNumber() + 1,
                    programs.getSize(),
                    programs.getTotalElements(),
                    programs.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{programId}")
    public ResponseEntity<Default> getProgramById(@PathVariable String programId) {
        try {
            // accepts either the program id or its slug
            Program program = programRepo.findById(programId)
                    .orElseGet(() -> programRepo.findBySlug(programId).orElse(null));
            if (program == null) {
                return new ResponseEntity<>(new Default("Program Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(new Default("Program Fetched Successfully", true, null, this.toProgramRes(program)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/{programId}/applications")
    public ResponseEntity<Default> applyToProgram(@PathVariable String programId, @Valid @RequestBody ProgramApplicationReq req) {
        try {
            Program program = programRepo.findById(programId).orElse(null);
            if (program == null) {
                return new ResponseEntity<>(new Default("Program Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!program.getIsActive()) {
                return ResponseEntity.badRequest().body(new Default("Program Is Not Accepting Applications", false, null, null));
            }

            Boolean hasAlreadyApplied = programApplicationRepo.existsByProgram_IdAndEmail(programId, req.getEmail());
            if (hasAlreadyApplied) {
                return ResponseEntity.badRequest().body(new Default("Application Already Submitted With This Email", false, null, null));
            }

            ProgramApplication application = new ProgramApplication();
            application.setProgram(program);
            application.setName(req.getName());
            application.setEmail(req.getEmail());
            application.setPhone(req.getPhone());
            application.setMessage(req.getMessage());
            programApplicationRepo.save(application);

            return ResponseEntity.ok(new Default("Application Submitted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<Default> createProgram(@Valid @RequestBody ProgramReq req) {
        try {
            Program program = new Program();
            program.setTitle(req.getTitle());
            program.setSlug(this.generateUniqueSlug(req.getTitle(), null));
            program.setDescription(req.getDescription());
            program.setThumbnailUrl(req.getThumbnailUrl());
            program.setDurationWeeks(req.getDurationWeeks());
            program.setStartDate(req.getStartDate());
            program.setPrice(req.getPrice());
            program.setCurrency(req.getCurrency());
            program.setIsActive(req.getIsActive() == null ? true : req.getIsActive());
            programRepo.save(program);

            return ResponseEntity.ok(new Default("Program Created Successfully", true, null, this.toProgramRes(program)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{programId}")
    public ResponseEntity<Default> updateProgram(@PathVariable String programId, @Valid @RequestBody ProgramReq req) {
        try {
            Program program = programRepo.findById(programId).orElse(null);
            if (program == null) {
                return new ResponseEntity<>(new Default("Program Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!program.getTitle().equals(req.getTitle())) {
                program.setSlug(this.generateUniqueSlug(req.getTitle(), program.getId()));
            }
            program.setTitle(req.getTitle());
            program.setDescription(req.getDescription());
            program.setThumbnailUrl(req.getThumbnailUrl());
            program.setDurationWeeks(req.getDurationWeeks());
            program.setStartDate(req.getStartDate());
            program.setPrice(req.getPrice());
            program.setCurrency(req.getCurrency());
            if (req.getIsActive() != null) {
                program.setIsActive(req.getIsActive());
            }
            programRepo.save(program);

            return ResponseEntity.ok(new Default("Program Updated Successfully", true, null, this.toProgramRes(program)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{programId}")
    public ResponseEntity<Default> deleteProgram(@PathVariable String programId) {
        try {
            Program program = programRepo.findById(programId).orElse(null);
            if (program == null) {
                return new ResponseEntity<>(new Default("Program Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            programRepo.delete(program);
            return ResponseEntity.ok(new Default("Program Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{programId}/applications")
    public ResponseEntity<?> getProgramApplications(
            @PathVariable String programId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Program program = programRepo.findById(programId).orElse(null);
            if (program == null) {
                return new ResponseEntity<>(new Default("Program Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            Page<ProgramApplication> applications = programApplicationRepo.findByProgram_Id(programId, pageable);
            List<ProgramApplicationRes> applicationList = applications
                    .stream()
                    .map(this::toProgramApplicationRes)
                    .toList();

            PaginatedResponse<ProgramApplicationRes> paginatedResponse = new PaginatedResponse<>(
                    "Program Applications Fetched Successfully",
                    true,
                    applicationList,
                    applications.getNumber() + 1,
                    applications.getSize(),
                    applications.getTotalElements(),
                    applications.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/applications/{applicationId}")
    public ResponseEntity<Default> updateApplicationStatus(@PathVariable String applicationId, @Valid @RequestBody ApplicationStatusReq req) {
        try {
            ProgramApplication application = programApplicationRepo.findById(applicationId).orElse(null);
            if (application == null) {
                return new ResponseEntity<>(new Default("Application Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            ProgramApplication.Status status;
            try {
                status = ProgramApplication.Status.valueOf(req.getStatus().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(new Default("Invalid Status. Allowed: PENDING, APPROVED, REJECTED", false, null, null));
            }

            application.setStatus(status);
            programApplicationRepo.save(application);

            return ResponseEntity.ok(new Default("Application Status Updated Successfully", true, null, this.toProgramApplicationRes(application)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    private String generateUniqueSlug(String title, String currentId) {
        Slugify slugify = new Slugify();
        String slug = slugify.slugify(title);
        Program existing = programRepo.findBySlug(slug).orElse(null);
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            slug = slug + "-" + new Random().nextInt(10000);
        }
        return slug;
    }

    private ProgramApplicationRes toProgramApplicationRes(ProgramApplication application) {
        return new ProgramApplicationRes(
                application.getId(),
                application.getProgram().getId(),
                application.getName(),
                application.getEmail(),
                application.getPhone(),
                application.getMessage(),
                application.getStatus().name(),
                application.getCreatedAt()
        );
    }

    private ProgramRes toProgramRes(Program program) {
        return new ProgramRes(
                program.getId(),
                program.getTitle(),
                program.getSlug(),
                program.getDescription(),
                program.getThumbnailUrl(),
                program.getDurationWeeks(),
                program.getStartDate(),
                program.getPrice(),
                program.getCurrency(),
                program.getIsActive(),
                program.getCreatedAt()
        );
    }
}
