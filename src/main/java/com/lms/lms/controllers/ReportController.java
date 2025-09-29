package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.ReportCardReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.ReportCardRes;
import com.lms.lms.mappers.ReportMapper;
import com.lms.lms.modals.ReportCard;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.ExamRepo;
import com.lms.lms.repo.ReportCardRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportCardRepo reportCardRepo;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private ExamRepo examRepo;


    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private ReportMapper reportMapper;


    @GetMapping("/{examId}/get")
    public ResponseEntity<Default> getReportCard(@PathVariable String examId) {
        try {
            var user = userDetails.userDetails();
            var examDetails = examRepo.findById(examId).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }
            ReportCard reportCard = reportCardRepo.findByUser_IdAndExam_Id(user.getId(), examId);
            if (reportCard == null) {
                return ResponseEntity.badRequest().body(new Default("Report Card Not Generated Yet", false, null, null));
            }
            ReportCardRes res = new ReportCardRes(
                    reportCard.getId(),
                    reportCard.getTotalMarks(),
                    reportCard.getObtainedMarks(),
                    reportCard.getPercentage(),
                    reportCard.getGrade()
            );
            return ResponseEntity.ok().body(new Default("Report Card Generated Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping("/generate")
    public ResponseEntity<Default> generateReportCard(@Valid @RequestBody ReportCardReq reportCardReq) {
        try {
            var user = userDetails.userDetails();
            var examDetails = examRepo.findById(reportCardReq.getExamId()).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            var isUserEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), examDetails.getCourses().getId());

            if (!isUserEnrolled) {
                return ResponseEntity.badRequest().body(new Default("User is not enrolled in the course", false, null, null));
            }

            ReportCard isReportCardGenerated = reportCardRepo.findByUser_IdAndExam_Id(user.getId(), reportCardReq.getExamId());

            if (isReportCardGenerated != null) {
                return ResponseEntity.badRequest().body(new Default("Report Card Already Generated. Please update the existing Report Card.", false, null, null));
            }

            ReportCard reportCard = new ReportCard();
            reportCard.setUser(user);
            reportCard.setExam(examDetails);
            reportCard.setPercentage(reportCardReq.getPercentage());
            reportCard.setGrade(reportCardReq.getGrade());
            reportCard.setObtainedMarks(reportCardReq.getObtainedMarks());
            reportCard.setTotalMarks(reportCardReq.getTotalMarks());
            reportCardRepo.save(reportCard);
            return ResponseEntity.ok().body(new Default("Report Card Generated Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/update")
    public ResponseEntity<Default> update(@Valid @RequestBody ReportCardReq reportCardReq) {
        try {
            var user = userDetails.userDetails();

            var examDetails = examRepo.findById(reportCardReq.getExamId()).orElse(null);
            if (examDetails == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Exam Id", false, null, null));
            }

            ReportCard isReportCardGenerated = reportCardRepo.findByUser_IdAndExam_Id(user.getId(), reportCardReq.getExamId());

            if (isReportCardGenerated == null) {
                return ResponseEntity.badRequest().body(new Default("Report Card Not Found.", false, null, null));
            }

            isReportCardGenerated.setPercentage(reportCardReq.getPercentage());
            isReportCardGenerated.setGrade(reportCardReq.getGrade());
            isReportCardGenerated.setObtainedMarks(reportCardReq.getObtainedMarks());
            isReportCardGenerated.setTotalMarks(reportCardReq.getTotalMarks());
            reportCardRepo.save(isReportCardGenerated);
            return ResponseEntity.ok().body(new Default("Report Card Updated Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Default> getUserReport() {
        try {
            var user = userDetails.userDetails();
            List<ReportCardRes> reportCard = reportCardRepo.findByUser_Id(user.getId()).stream().map(reportMapper::toDto).toList();

            return ResponseEntity.ok().body(new Default("Report Card Fetched Successfully", true, null, reportCard));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<Default> getReportByCourse(@PathVariable String examId) {
        try {

            List<ReportCardRes> reportCard = reportCardRepo.findByExam_Id(examId).stream().map(reportMapper::toDto).toList();

            return ResponseEntity.ok().body(new Default("Report Card Fetched Successfully", true, null, reportCard));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
