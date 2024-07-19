package com.jack.jobportal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "recruiter_profile")
public class RecruiterProfile {
    @Id
    private Integer userAccountId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    private Users userId;

    @Size(max = 255)
    @Column(name = "city")
    private String city;

    @Size(max = 255)
    @Column(name = "company")
    private String company;

    @Size(max = 255)
    @Column(name = "country")
    private String country;

    @Size(max = 255)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;

    @Size(max = 64)
    @Column(name = "profile_photo", length = 64)
    private String profilePhoto;

    @Size(max = 255)
    @Column(name = "state")
    private String state;

    public RecruiterProfile(Users users) {
        this.userId = users;
    }
}
