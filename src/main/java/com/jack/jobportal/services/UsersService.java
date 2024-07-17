package com.jack.jobportal.services;

import com.jack.jobportal.entity.Users;
import com.jack.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UsersService {
    private final UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }


    public Users addNew(Users users) {
        users.setActive(true);
        users.setRegistrationDate(Instant.now());
        return usersRepository.save(users);
    }
}
