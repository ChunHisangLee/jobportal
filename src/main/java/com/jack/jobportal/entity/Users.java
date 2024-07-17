package com.jack.jobportal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", nullable = false)
    private int userId;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private boolean isActive;

    @Size(max = 255)
    @NotEmpty
    @Column(name = "password")
    private String password;

    @Column(name = "registration_date")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Instant registrationDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_type_id")
    private UsersType userTypeId;

    public Users(int userId, String email, boolean isActive, String password, Instant registrationDate, UsersType userTypeId) {
        this.userId = userId;
        this.email = email;
        this.isActive = isActive;
        this.password = password;
        this.registrationDate = registrationDate;
        this.userTypeId = userTypeId;
    }

    @Override
    public String toString() {
        return "Users{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", password='" + password + '\'' +
                ", registrationDate=" + registrationDate +
                ", userTypeId=" + userTypeId +
                '}';
    }
}
