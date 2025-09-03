package com.lms.lms.controllers;


import com.lms.lms.dto.request.Login;
import com.lms.lms.dto.request.Register;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.LoginRes;
import com.lms.lms.modals.User;
import com.lms.lms.repo.UserRepo;
import com.lms.lms.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @PostMapping("/register")
    public ResponseEntity<Default> register(@Valid @RequestBody Register register){
        try{
            User username = userRepo.findByUsername(register.getUsername()).orElse(null);
            User email = userRepo.findByEmail(register.getEmail()).orElse(null);

            if(username != null || email != null){
                return new ResponseEntity<>(new Default("User Already Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }
            User user = new User();

            var pass = encoder.encode(register.getPassword());
            user.setUsername(register.getUsername());
            user.setEmail(register.getEmail());
            user.setPassword(pass);
            user.setName(register.getName());
            userRepo.save(user);
            return ResponseEntity.ok(new Default("User Created Successfully", true, null, null));
        }
        catch (Exception e){
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> register(@Valid @RequestBody Login login){
        try{
            User isUserExist = userRepo.findByUsername(login.getUsername()).orElse(null);

            if(isUserExist == null){
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));

            if (auth.isAuthenticated()){
                var token = jwtService.generateToken(isUserExist.getId());
                return ResponseEntity.ok(new LoginRes("User Login Successfully", true, token));
            }
            return new ResponseEntity<>(new Default("Invalid Credentials", false, null, null), HttpStatus.UNAUTHORIZED);
        }
        catch (Exception e){
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
