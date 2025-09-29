package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.ReportCardRes;
import com.lms.lms.modals.ReportCard;
import com.lms.lms.repo.ExamRepo;
import com.lms.lms.repo.ReportCardRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportCardRepo reportCardRepo;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private ExamRepo examRepo;

    @GetMapping("{examId}/get")
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
}
