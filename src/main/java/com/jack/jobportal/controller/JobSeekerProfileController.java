package com.jack.jobportal.controller;

import com.jack.jobportal.entity.JobSeekerProfile;
import com.jack.jobportal.entity.Skills;
import com.jack.jobportal.entity.Users;
import com.jack.jobportal.repository.UsersRepository;
import com.jack.jobportal.services.JobSeekerProfileService;
import com.jack.jobportal.util.FileDownloadUtil;
import com.jack.jobportal.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/job-seeker-profile")
public class JobSeekerProfileController {
    private final JobSeekerProfileService jobSeekerProfileService;
    private final UsersRepository usersRepository;

    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService, UsersRepository usersRepository) {
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.usersRepository = usersRepository;
    }

    @GetMapping("/")
    public String jobSeekerProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            return "job-seeker-profile";
        }

        Users user = getAuthenticatedUser(authentication);
        JobSeekerProfile jobSeekerProfile = getJobSeekerProfile(user);

        if (jobSeekerProfile.getSkills().isEmpty()) {
            jobSeekerProfile.setSkills(List.of(new Skills()));
        }

        model.addAttribute("skills", jobSeekerProfile.getSkills());
        model.addAttribute("profile", jobSeekerProfile);
        return "job-seeker-profile";
    }

    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam("pdf") MultipartFile pdf,
                         Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/";
        }

        Users user = getAuthenticatedUser(authentication);
        jobSeekerProfile.setUserId(user);
        jobSeekerProfile.setUserAccountId(user.getUserId());

        jobSeekerProfile.getSkills()
                .forEach(skill -> skill.setJobSeekerProfile(jobSeekerProfile));
        handleFileUpload(jobSeekerProfile, image, pdf);
        jobSeekerProfileService.addNew(jobSeekerProfile);
        model.addAttribute("profile", jobSeekerProfile);
        model.addAttribute("skills", jobSeekerProfile.getSkills());
        return "redirect:/dashboard/";
    }

    @GetMapping("/{id}")
    public String candidateProfile(@PathVariable("id") int id, Model model) {
        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(id);

        if (seekerProfile.isPresent()) {
            model.addAttribute("profile", seekerProfile.get());
        } else {
            return "redirect:/";
        }

        return "job-seeker-profile";
    }

    @GetMapping("/downloadResume")
    public ResponseEntity<?> downloadResume(@RequestParam("fileName") String fileName, @RequestParam("userID") String userId) {
        try {
            Resource resource = new FileDownloadUtil().getFileAsResource("photos/candidate/" + userId, fileName);

            if (resource == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("File not found");
            }

            String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving file");
        }
    }

    private void handleFileUpload(JobSeekerProfile jobSeekerProfile, MultipartFile image, MultipartFile pdf) {
        String uploadDir = "photos/candidate/" + jobSeekerProfile.getUserAccountId();

        try {
            if (!image.isEmpty()) {
                String imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
                jobSeekerProfile.setProfilePhoto(imageName);
                FileUploadUtil.saveFile(uploadDir, imageName, image);
            }

            if (!pdf.isEmpty()) {
                String resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));
                jobSeekerProfile.setResume(resumeName);
                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);
            }
        } catch (IOException ex) {
            throw new RuntimeException("File upload failed. ", ex);
        }
    }

    private Users getAuthenticatedUser(Authentication authentication) {
        return usersRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    private JobSeekerProfile getJobSeekerProfile(Users user) {
        return jobSeekerProfileService.getOne(user.getUserId())
                .orElse(new JobSeekerProfile());
    }
}
