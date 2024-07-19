package com.jack.jobportal.entity;

import jakarta.persistence.*;
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
    @JoinColumn(name = "user_account_id")
    private Users userId;

    private String city;

    private String company;

    private String country;

    private String firstName;

    private String lastName;

    @Column(length = 64)
    private String profilePhoto;

    private String state;

    public RecruiterProfile(Users users) {
        this.userId = users;
    }

    public String getPhotosImagePath() {
        if (profilePhoto == null) {
            return null;
        }

        return "/photos/recruiter/" + userAccountId + "/" + profilePhoto;
    }
}
