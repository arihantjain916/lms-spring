package com.lms.lms.controllers;

import com.lms.lms.dto.request.ProgramApplicationReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.PaginatedResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
