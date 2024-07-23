package com.jack.jobportal.controller;

import com.jack.jobportal.entity.*;
import com.jack.jobportal.services.*;
import com.jack.jobportal.util.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.util.Optional;

@Controller
public class JobSeekerApplyController {
    private final JobPostActivityService jobPostActivityService;
    private final UsersService usersService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;
    private final RecruiterProfileService recruiterProfileService;
    private final JobSeekerProfileService jobSeekerProfileService;

    @Autowired
    public JobSeekerApplyController(JobPostActivityService jobPostActivityService, UsersService usersService,
                                    JobSeekerApplyService jobSeekerApplyService, JobSeekerSaveService jobSeekerSaveService,
                                    RecruiterProfileService recruiterProfileService, JobSeekerProfileService jobSeekerProfileService) {
        this.jobPostActivityService = jobPostActivityService;
        this.usersService = usersService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
        this.recruiterProfileService = recruiterProfileService;
        this.jobSeekerProfileService = jobSeekerProfileService;
    }

    @GetMapping("job-details-apply/{id}")
    public String display(@PathVariable("id") int id, Model model) {
        JobPostActivity jobDetails = jobPostActivityService.getOne(id);
        List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getJobCandidates(jobDetails);
        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getJobCandidates(jobDetails);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (AuthenticationUtils.isAuthenticated(authentication)) {
            addUserDetailsToModel(authentication, model, jobSeekerApplyList, jobSeekerSaveList);
        }

        model.addAttribute("applyJob", new JobSeekerApply());
        model.addAttribute("jobDetails", jobDetails);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "job-details";
    }

    @PostMapping("job-details/apply/{id}")
    public String apply(@PathVariable("id") int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (AuthenticationUtils.isAuthenticated(authentication)) {
            applyForJobJfAuthenticated(id, authentication.getName());
        }

        return "redirect:/dashboard/";
    }

    private void applyForJobJfAuthenticated(int id, String username) {
        Optional<Users> user = usersService.findByEmail(username);
        user.ifPresent(value -> {
            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(value.getUserId());

            if (seekerProfile.isPresent() && jobPostActivity != null) {
                JobSeekerApply jobSeekerApply = new JobSeekerApply();
                jobSeekerApply.setUserId(seekerProfile.get());
                jobSeekerApply.setJob(jobPostActivity);
                jobSeekerApply.setApplyDate(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
                jobSeekerApplyService.addNew(jobSeekerApply);
            } else {
                throw new RuntimeException("User not found or job post not found");
            }
        });
    }

    private void addUserDetailsToModel(Authentication authentication, Model model, List<JobSeekerApply> jobSeekerApplyList, List<JobSeekerSave> jobSeekerSaveList) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
            addRecruiterDetailsToModel(model, jobSeekerApplyList);
        } else {
            addJobSeekerDetailsToModel(model, jobSeekerApplyList, jobSeekerSaveList);
        }
    }

    private void addJobSeekerDetailsToModel(Model model, List<JobSeekerApply> jobSeekerApplyList, List<JobSeekerSave> jobSeekerSaveList) {
        JobSeekerProfile jobSeekerProfile = jobSeekerProfileService.getCurrentSeekerProfile();

        if (jobSeekerProfile != null) {
            boolean alreadyApplied = jobSeekerApplyList.stream()
                    .anyMatch(apply -> apply.getUserId()
                            .getUserAccountId()
                            .equals(jobSeekerProfile.getUserAccountId()));

            boolean alreadySaved = jobSeekerSaveList.stream()
                    .anyMatch(save -> save.getUserId()
                            .getUserAccountId()
                            .equals(jobSeekerProfile.getUserAccountId()));

            model.addAttribute("alreadyApplied", alreadyApplied);
            model.addAttribute("alreadySaved", alreadySaved);
        }
    }

    private void addRecruiterDetailsToModel(Model model, List<JobSeekerApply> jobSeekerApplyList) {
        RecruiterProfile recruiterProfile = recruiterProfileService.getCurrentRecruiterProfile();

        if (recruiterProfile != null) {
            model.addAttribute("applyList", jobSeekerApplyList);
        }
    }
}
