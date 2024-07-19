package com.jack.jobportal.controller;

import com.jack.jobportal.entity.JobPostActivity;
import com.jack.jobportal.entity.RecruiterJobsDto;
import com.jack.jobportal.entity.RecruiterProfile;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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
        Object currentUserProfile = usersService.getCurrentUserProfile();
        model.addAttribute("user", currentUserProfile);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/";
        }

        String currentUsername = authentication.getName();
        model.addAttribute("username", currentUsername);

        if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("Recruiter"))) {
            if (currentUserProfile instanceof RecruiterProfile) {
                RecruiterProfile recruiterProfile = (RecruiterProfile) currentUserProfile;
                List<RecruiterJobsDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(recruiterProfile.getUserAccountId());
                model.addAttribute("jobPost", recruiterJobs);
            } else {
                logger.warn("Current user profile is not of type RecruiterProfile");
            }
        }

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

    @PostMapping("dashboard/edit/{id}")
    public String editJob(@PathVariable("id") int id, Model model) {
        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
        model.addAttribute("jobPostActivity", jobPostActivity);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }
}
