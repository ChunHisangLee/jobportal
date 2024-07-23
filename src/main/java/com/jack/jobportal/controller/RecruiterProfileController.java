package com.jack.jobportal.controller;

import com.jack.jobportal.entity.RecruiterProfile;
import com.jack.jobportal.entity.Users;
import com.jack.jobportal.repository.UsersRepository;
import com.jack.jobportal.services.RecruiterProfileService;
import com.jack.jobportal.util.AuthenticationUtils;
import com.jack.jobportal.util.FileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/recruiter-profile")
public class RecruiterProfileController {
    private static final Logger logger = LoggerFactory.getLogger(RecruiterProfileController.class);
    private final UsersRepository usersRepository;
    private final RecruiterProfileService recruiterProfileService;

    @Autowired
    public RecruiterProfileController(UsersRepository usersRepository, RecruiterProfileService recruiterProfileService) {
        this.usersRepository = usersRepository;
        this.recruiterProfileService = recruiterProfileService;
    }

    @GetMapping("/")
    public String recruiterProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (AuthenticationUtils.isAuthenticated(authentication)) {
            String currentUsername = authentication.getName();
            Users user = usersRepository.findByEmail(currentUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("Could not find user with email: " + currentUsername));

            recruiterProfileService.getOne(user.getUserId())
                    .ifPresent(profile -> model.addAttribute("profile", profile));
        }

        return "recruiter_profile";
    }

    @PostMapping("addNew")
    public String addNew(RecruiterProfile recruiterProfile, @RequestParam("image") MultipartFile multipartFile, Model model) {
        Optional<Users> currentUser = getCurrentAuthenticatedUser();

        if (currentUser.isPresent()) {
            Users user = currentUser.get();
            recruiterProfile.setUserId(user);
            recruiterProfile.setUserAccountId(user.getUserId());
            model.addAttribute("profile", recruiterProfile);

            String fileName = handleUploadFile(multipartFile, recruiterProfile);
            RecruiterProfile savedProfile = recruiterProfileService.addNew(recruiterProfile);
            saveUploadedFile(fileName, multipartFile, savedProfile);

            return "redirect:/dashboard/";
        } else {
            throw new UsernameNotFoundException("Could not find the authenticated user.");
        }
    }

    private void saveUploadedFile(String fileName, MultipartFile multipartFile, RecruiterProfile savedProfile) {
        String uploadDir = "photos/recruiter/" + savedProfile.getUserAccountId();

        try {
            FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
            logger.info("File saved successfully at {}", uploadDir);
        } catch (Exception ex) {
            logger.error("Failed to save file: {}", fileName, ex);
        }
    }

    private String handleUploadFile(MultipartFile multipartFile, RecruiterProfile recruiterProfile) {
        if (!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            recruiterProfile.setProfilePhoto(fileName);
            return fileName;
        }

        return "";
    }

    private Optional<Users> getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (AuthenticationUtils.isAuthenticated(authentication)) {
            String username = authentication.getName();
            return usersRepository.findByEmail(username);
        }

        return Optional.empty();
    }
}
