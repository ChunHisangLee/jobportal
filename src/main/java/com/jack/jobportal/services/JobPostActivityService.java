package com.jack.jobportal.services;

import com.jack.jobportal.entity.*;
import com.jack.jobportal.repository.JobPostActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobPostActivityService {
    private final JobPostActivityRepository jobPostActivityRepository;

    @Autowired
    public JobPostActivityService(JobPostActivityRepository jobPostActivityRepository) {
        this.jobPostActivityRepository = jobPostActivityRepository;
    }

    public JobPostActivity addNew(JobPostActivity jobPostActivity) {
        return jobPostActivityRepository.save(jobPostActivity);
    }

    public List<RecruiterJobsDto> getRecruiterJobs(int recruiter) {
        List<IRecruiterJobs> recruiterJobsList = jobPostActivityRepository.getRecruiterJobs(recruiter);
        return recruiterJobsList.stream()
                .map(this::convertToRecruiterJobsDto)
                .collect(Collectors.toList());
    }

    private RecruiterJobsDto convertToRecruiterJobsDto(IRecruiterJobs rec) {
        JobLocation location = new JobLocation(rec.getLocationId(), rec.getCity(), rec.getState(), rec.getCountry());
        JobCompany company = new JobCompany(rec.getCompanyId(), rec.getName(), "");
        return new RecruiterJobsDto(rec.getTotalCandidates(), rec.getJobPostId(), rec.getJobTitle(), location, company);
    }

    public JobPostActivity getOne(int id) {
        return jobPostActivityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }
}
