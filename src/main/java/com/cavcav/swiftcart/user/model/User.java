package com.cavcav.swiftcart.user.model;


import jakarta.persistence.*;
import lombok.*;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role=Role.CUSTOMER;
    @Column(nullable = false)
    private Boolean isActive=true;
    @Column(nullable = false)
    private Boolean isEmailVerified=false;

    public User( String email,String password) {
        this.email = email;
        this.password = password;
    }
}
