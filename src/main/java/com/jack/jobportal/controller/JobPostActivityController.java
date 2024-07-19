package com.jack.jobportal.controller;

import com.jack.jobportal.entity.JobPostActivity;
import com.jack.jobportal.entity.Users;
import com.jack.jobportal.services.JobPostActivityService;
import com.jack.jobportal.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
public class JobPostActivityController {
    private final UsersService usersService;
    private final JobPostActivityService jobPostActivityService;
    private static final Logger logger = LoggerFactory.getLogger(JobPostActivityController.class);

    @Autowired
    public JobPostActivityController(UsersService usersService, JobPostActivityService jobPostActivityService) {
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
    }

    @GetMapping("/dashboard/")
    public String searchJobs(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            model.addAttribute("username", authentication.getName());
        }

        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "dashboard";
    }

    @GetMapping("/dashboard/add")
    public String addJob(Model model) {
        model.addAttribute("jobPostActivity", new JobPostActivity());
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/addNew")
    public String addNew(JobPostActivity jobPostActivity, Model model) {
        Users user = usersService.getCurrentUser();

        if (user != null) {
            jobPostActivity.setPostedById(user);
            logger.info("User found: {}", user.getEmail());
        } else {
            logger.warn("No current user found.");
            return "redirect:/";
        }

        jobPostActivity.setPostedDate(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        model.addAttribute("jobPostActivity", jobPostActivity);
        jobPostActivityService.addNew(jobPostActivity);
        logger.info("Job post activity added: {}", jobPostActivity);
        return "redirect:/dashboard/";
    }
}
