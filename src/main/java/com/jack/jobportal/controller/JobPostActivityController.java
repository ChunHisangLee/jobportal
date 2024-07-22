package com.jack.jobportal.controller;

import com.jack.jobportal.entity.*;
import com.jack.jobportal.services.JobPostActivityService;
import com.jack.jobportal.services.JobSeekerApplyService;
import com.jack.jobportal.services.JobSeekerSaveService;
import com.jack.jobportal.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Controller
public class JobPostActivityController {

    private final UsersService usersService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public JobPostActivityController(UsersService usersService, JobPostActivityService jobPostActivityService, JobSeekerApplyService jobSeekerApplyService, JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @GetMapping("/dashboard/")
    public String searchJobs(Model model,
                             @RequestParam(value = "job", required = false) String job,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam(value = "partTime", required = false) String partTime,
                             @RequestParam(value = "fullTime", required = false) String fullTime,
                             @RequestParam(value = "freelance", required = false) String freelance,
                             @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                             @RequestParam(value = "officeOnly", required = false) String officeOnly,
                             @RequestParam(value = "partialRemote", required = false) String partialRemote,
                             @RequestParam(value = "today", required = false) boolean today,
                             @RequestParam(value = "days7", required = false) boolean days7,
                             @RequestParam(value = "days30", required = false) boolean days30) {

        populateJobAttributes(model, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, today, days7, days30, job, location);
        LocalDate searchDate = getSearchDate(today, days7, days30);
        List<JobPostActivity> jobPost = getJobPostActivities(job, location, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, searchDate);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object currentUserProfile = usersService.getCurrentUserProfile();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            model.addAttribute("username", currentUsername);

            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                List<RecruiterJobsDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(((RecruiterProfile) currentUserProfile).getUserAccountId());
                model.addAttribute("jobPost", recruiterJobs);
            } else {
                updateJobActivitiesForJobSeeker(jobPost, currentUserProfile);
                model.addAttribute("jobPost", jobPost);
            }
        }

        model.addAttribute("user", currentUserProfile);
        return "dashboard";
    }

    @GetMapping("global-search/")
    public String globalSearch(Model model,
                               @RequestParam(value = "job", required = false) String job,
                               @RequestParam(value = "location", required = false) String location,
                               @RequestParam(value = "partTime", required = false) String partTime,
                               @RequestParam(value = "fullTime", required = false) String fullTime,
                               @RequestParam(value = "freelance", required = false) String freelance,
                               @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                               @RequestParam(value = "officeOnly", required = false) String officeOnly,
                               @RequestParam(value = "partialRemote", required = false) String partialRemote,
                               @RequestParam(value = "today", required = false) boolean today,
                               @RequestParam(value = "days7", required = false) boolean days7,
                               @RequestParam(value = "days30", required = false) boolean days30) {

        populateJobAttributes(model, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, today, days7, days30, job, location);
        LocalDate searchDate = getSearchDate(today, days7, days30);
        List<JobPostActivity> jobPost = getJobPostActivities(job, location, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, searchDate);
        model.addAttribute("jobPost", jobPost);
        return "global-search";
    }

    @GetMapping("/dashboard/add")
    public String addJobs(Model model) {
        model.addAttribute("jobPostActivity", new JobPostActivity());
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/addNew")
    public String addNew(JobPostActivity jobPostActivity, Model model) {
        Users user = usersService.getCurrentUser();

        if (user != null) {
            jobPostActivity.setPostedById(user);
        }

        jobPostActivity.setPostedDate(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        model.addAttribute("jobPostActivity", jobPostActivity);
        jobPostActivityService.addNew(jobPostActivity);
        return "redirect:/dashboard/";
    }

    @GetMapping("dashboard/edit/{id}")
    public String editJob(@PathVariable("id") int id, Model model) {
        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
        model.addAttribute("jobPostActivity", jobPostActivity);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    private void populateJobAttributes(Model model, String partTime, String fullTime, String freelance, String remoteOnly, String officeOnly, String partialRemote, boolean today, boolean days7, boolean days30, String job, String location) {
        model.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        model.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        model.addAttribute("freelance", Objects.equals(freelance, "Freelance"));

        model.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Remote-Only"));
        model.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        model.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));

        model.addAttribute("today", today);
        model.addAttribute("days7", days7);
        model.addAttribute("days30", days30);

        model.addAttribute("job", job);
        model.addAttribute("location", location);
    }

    private LocalDate getSearchDate(boolean today, boolean days7, boolean days30) {
        if (days30) {
            return LocalDate.now().minusDays(30);
        } else if (days7) {
            return LocalDate.now().minusDays(7);
        } else if (today) {
            return LocalDate.now();
        } else {
            return null;
        }
    }

    private List<JobPostActivity> getJobPostActivities(String job, String location, String partTime, String fullTime, String freelance, String remoteOnly, String officeOnly, String partialRemote, LocalDate searchDate) {
        boolean dateSearchFlag = searchDate != null;
        boolean remote = partTime == null && fullTime == null && freelance == null;
        boolean type = officeOnly == null && remoteOnly == null && partialRemote == null;

        if (remote) {
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
        }

        if (type) {
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
        }

        if (!dateSearchFlag && remote && type && !StringUtils.hasText(job) && !StringUtils.hasText(location)) {
            return jobPostActivityService.getAll();
        } else {
            return jobPostActivityService.search(job, location, Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote), searchDate);
        }
    }

    private void updateJobActivitiesForJobSeeker(List<JobPostActivity> jobPost, Object currentUserProfile) {
        List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getCandidatesJobs((JobSeekerProfile) currentUserProfile);
        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);

        for (JobPostActivity jobActivity : jobPost) {
            boolean exist = jobSeekerApplyList.stream().anyMatch(apply -> Objects.equals(jobActivity.getJobPostId(), apply.getJob().getJobPostId()));
            boolean saved = jobSeekerSaveList.stream().anyMatch(save -> Objects.equals(jobActivity.getJobPostId(), save.getJob().getJobPostId()));

            jobActivity.setIsActive(exist);
            jobActivity.setIsSaved(saved);
        }
    }
}
