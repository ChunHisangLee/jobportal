package com.jack.jobportal.services;

import com.jack.jobportal.entity.JobSeekerProfile;
import com.jack.jobportal.entity.RecruiterProfile;
import com.jack.jobportal.entity.Users;
import com.jack.jobportal.repository.JobSeekerProfileRepository;
import com.jack.jobportal.repository.RecruiterProfileRepository;
import com.jack.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository usersRepository, JobSeekerProfileRepository jobSeekerProfileRepository, RecruiterProfileRepository recruiterProfileRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Users addNew(Users users) {
        users.setActive(true);
        users.setRegistrationDate(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        Users savedUser = usersRepository.save(users);
        int userTypeId = users.getUserTypeId().getUserTypeId();

        if (userTypeId == 1) {
            recruiterProfileRepository.save(new RecruiterProfile(savedUser));
        } else {
            jobSeekerProfileRepository.save(new JobSeekerProfile(savedUser));
        }

        return savedUser;
    }

    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public Object getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String username = authentication.getName();
        Users users = usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        int UserId = users.getUserId();

        if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("Recruiter"))) {
            RecruiterProfile recruiterProfile = recruiterProfileRepository.findById(UserId).orElse(new RecruiterProfile());
            return recruiterProfile;
        } else {
            JobSeekerProfile jobSeekerProfile = jobSeekerProfileRepository.findById(UserId).orElse(new JobSeekerProfile());
            return jobSeekerProfile;
        }
    }
}
