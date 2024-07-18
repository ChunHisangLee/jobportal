package com.jack.jobportal.services;

import com.jack.jobportal.entity.JobSeekerProfile;
import com.jack.jobportal.entity.RecruiterProfile;
import com.jack.jobportal.entity.Users;
import com.jack.jobportal.repository.JobSeekerProfileRepository;
import com.jack.jobportal.repository.RecruiterProfileRepository;
import com.jack.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UsersService(UsersRepository usersRepository, JobSeekerProfileRepository jobSeekerProfileRepository, RecruiterProfileRepository recruiterProfileRepository) {
        this.usersRepository = usersRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
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
}
