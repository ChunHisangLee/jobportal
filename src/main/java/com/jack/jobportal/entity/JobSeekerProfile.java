package com.jack.jobportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_seeker_profile")
public class JobSeekerProfile {
    @Id
    private int userAccountId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    private Users userId;

    private String city;

    private String country;

    private String employmentType;

    private String firstName;

    private String lastName;

    @Column(length = 64)
    private String profilePhoto;

    private String resume;

    private String state;

    private String workAuthorization;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "jobSeekerProfile")
    private List<Skills> skills;

    public JobSeekerProfile(Users userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "JobSeekerProfile{" +
                "userAccountId=" + userAccountId +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", workAuthorization='" + workAuthorization + '\'' +
                ", employmentType='" + employmentType + '\'' +
                ", resume='" + resume + '\'' +
                ", profilePhoto='" + profilePhoto + '\'' +
                ", skills=" + skills +
                '}';
    }
}
