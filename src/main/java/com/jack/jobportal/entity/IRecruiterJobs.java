package com.jack.jobportal.entity;

public interface IRecruiterJobs {
    Long getTotalCandidates();

    int getJobPostId();

    String getJobTitle();

    int getLocationId();

    String getCity();

    String getState();

    String getCountry();

    int getCompanyId();

    String getName();
}
