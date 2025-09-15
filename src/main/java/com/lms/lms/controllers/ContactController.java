package com.lms.lms.controllers;

import com.lms.lms.dto.request.ContactReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.ContactUs;
import com.lms.lms.repo.ContactRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private ContactRepo contactRepo;

    @PostMapping("/save")
    public ResponseEntity<Default> saveContactUs(@Valid @RequestBody ContactReq contactUs) {
        try {
            ContactUs contact = new ContactUs();
            contact.setName(contactUs.getName());
            contact.setEmail(contactUs.getEmail());
            contact.setSubject(contactUs.getSubject());
            contact.setMessage(contactUs.getMessage());
            contact.setDepartment(contactUs.getDepartment());
            contact.setPhone(contactUs.getPhone());

            contactRepo.save(contact);
            return ResponseEntity.status(HttpStatus.OK).body(new Default("Contact Us saved successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Default("Internal Server Error", false, null, null));
        }
    }
}
