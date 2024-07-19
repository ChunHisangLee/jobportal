package com.jack.jobportal.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RecruiterJobsDto {
    private Long totalCandidates;
    private Integer jobPostId;
    private String jobTitle;
    private JobLocation jobLocationId;
    private JobCompany jobCompanyId;
}
