package com.jack.jobportal.controller;

import com.jack.jobportal.entity.*;
import com.jack.jobportal.services.JobPostActivityService;
import com.jack.jobportal.services.JobSeekerApplyService;
import com.jack.jobportal.services.JobSeekerSaveService;
import com.jack.jobportal.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(JobPostActivityController.class);
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

        addSearchAttributesToModel(model, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, today, days7, days30, job, location);
        LocalDate searchDate = determineSearchDate(today, days7, days30);
        List<JobPostActivity> jobPosts = getJobPosts(job, location, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, searchDate);
        processUserAuthentication(model, jobPosts);
        return "dashboard";
    }

    @GetMapping("/global-search/")
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

        addSearchAttributesToModel(model, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, today, days7, days30, job, location);
        LocalDate searchDate = determineSearchDate(today, days7, days30);
        List<JobPostActivity> jobPosts = getJobPosts(job, location, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, searchDate);
        model.addAttribute("jobPost", jobPosts);
        return "global-search";
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

    private void processUserAuthentication(Model model, List<JobPostActivity> jobPosts) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("user", null);
            return;
        }

        Object currentUserProfile = usersService.getCurrentUserProfile();
        model.addAttribute("user", currentUserProfile);
        String currentUsername = authentication.getName();
        model.addAttribute("username", currentUsername);

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
            if (currentUserProfile instanceof RecruiterProfile recruiterProfile) {
                List<RecruiterJobsDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(recruiterProfile.getUserAccountId());
                model.addAttribute("jobPost", recruiterJobs);
            } else {
                logger.warn("Current user profile is not of type RecruiterProfile");
            }
        } else {
            updateJobPostStatusForJobSeeker(model, jobPosts, (JobSeekerProfile) currentUserProfile);
        }
    }

    private void updateJobPostStatusForJobSeeker(Model model, List<JobPostActivity> jobPosts, JobSeekerProfile currentUserProfile) {
        List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getCandidatesJobs(currentUserProfile);
        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob(currentUserProfile);

        jobPosts.forEach(jobActivity -> {
            boolean isActive = jobSeekerApplyList.stream()
                    .anyMatch(jobSeekerApply -> Objects.equals(jobActivity.getJobPostId(), jobSeekerApply.getJob().getJobPostId()));

            boolean isSaved = jobSeekerSaveList.stream()
                    .anyMatch(jobSeekerSave -> Objects.equals(jobActivity.getJobPostId(), jobSeekerSave.getJob().getJobPostId()));

            jobActivity.setIsActive(isActive);
            jobActivity.setIsSaved(isSaved);
        });

        model.addAttribute("jobPost", jobPosts);
    }

    private List<JobPostActivity> getJobPosts(String job, String location, String partTime, String fullTime, String freelance, String remoteOnly, String officeOnly, String partialRemote, LocalDate searchDate) {
        if (noSearchCriteria(partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, job, location, searchDate)) {
            return jobPostActivityService.getAll();
        }

        return jobPostActivityService.search(job, location, Arrays.asList(partTime, fullTime, freelance), Arrays.asList(remoteOnly, officeOnly, partialRemote), searchDate);

    }

    private boolean noSearchCriteria(String partTime, String fullTime, String freelance, String remoteOnly, String officeOnly, String partialRemote, String job, String location, LocalDate searchDate) {
        return partTime == null && fullTime == null && freelance == null && remoteOnly == null && officeOnly == null && partialRemote == null && !StringUtils.hasText(job) && !StringUtils.hasText(location) && searchDate == null;
    }

    private LocalDate determineSearchDate(boolean today, boolean days7, boolean days30) {
        if (days30) {
            return LocalDate.now().minusDays(30);
        } else if (days7) {
            return LocalDate.now().minusDays(7);
        } else if (today) {
            return LocalDate.now();
        }

        return null;
    }

    private void addSearchAttributesToModel(Model model, String partTime, String fullTime, String freelance, String remoteOnly, String officeOnly, String partialRemote, boolean today, boolean days7, boolean days30, String job, String location) {
        model.addAttribute("partTime", "Part-Time".equals(partTime));
        model.addAttribute("fullTime", "Full-Time".equals(fullTime));
        model.addAttribute("freelance", "Freelance".equals(freelance));

        model.addAttribute("remoteOnly", "Remote-Only".equals(remoteOnly));
        model.addAttribute("officeOnly", "Office-Only".equals(officeOnly));
        model.addAttribute("partialRemote", "Partial-Remote".equals(partialRemote));

        model.addAttribute("today", today);
        model.addAttribute("days7", days7);
        model.addAttribute("days30", days30);

        model.addAttribute("job", job);
        model.addAttribute("location", location);
    }
}
