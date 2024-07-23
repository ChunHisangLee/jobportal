package com.jack.jobportal.controller;

import com.jack.jobportal.entity.JobPostActivity;
import com.jack.jobportal.entity.JobSeekerProfile;
import com.jack.jobportal.entity.JobSeekerSave;
import com.jack.jobportal.entity.Users;
import com.jack.jobportal.services.JobPostActivityService;
import com.jack.jobportal.services.JobSeekerProfileService;
import com.jack.jobportal.services.JobSeekerSaveService;
import com.jack.jobportal.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class JobSeekerSaveController {
    private final UsersService usersService;
    private final JobSeekerProfileService jobSeekerProfileService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public JobSeekerSaveController(UsersService usersService, JobSeekerProfileService jobSeekerProfileService, JobPostActivityService jobPostActivityService, JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @PostMapping("job-details/save/{id}")
    public String save(@PathVariable("id") int id, JobSeekerSave jobSeekerSave) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/";
        }

        String currentUserName = authentication.getName();
        Optional<Users> user = usersService.findByEmail(currentUserName);
        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.get().getUserId());
        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);

        if (seekerProfile.isPresent() && jobPostActivity != null) {
            jobSeekerSave.setJob(jobPostActivity);
            jobSeekerSave.setUserId(seekerProfile.get());
            jobSeekerSaveService.addNew(jobSeekerSave);
        } else {
            throw new RuntimeException("User not found");
        }

        return "redirect:/dashboard/";
    }

    @GetMapping("saved-jobs/")
    public String savedJobs(Model model) {
        Object currentUserProfile = usersService.getCurrentUserProfile();

        if (!(currentUserProfile instanceof JobSeekerProfile jobSeekerProfile)) {
            model.addAttribute("jobPost", new ArrayList<>());
            model.addAttribute("user", null);
            return "saved-jobs";
        }

        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob(jobSeekerProfile);
        List<JobPostActivity> jobPosts = new ArrayList<>();

        for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
            jobPosts.add(jobSeekerSave.getJob());
        }

        model.addAttribute("jobPost", jobPosts);
        model.addAttribute("user", jobSeekerProfile);
        return "saved-jobs";
    }
}
