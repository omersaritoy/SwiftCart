package com.cavcav.swiftcart.user.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name = "user_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY,orphanRemoval = true)
    @JoinColumn(name = "user_id",nullable = false,unique = true)
    private User user;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String phone;

    private LocalDate birthDate;

    private String avatarUrl;

}
