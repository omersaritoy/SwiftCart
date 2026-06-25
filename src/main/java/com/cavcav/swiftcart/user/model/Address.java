package com.cavcav.swiftcart.user.model;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;          // "Ev", "İş" gibi etiket

    @Column(nullable = false)
    private String fullAddress;    // sokak, mahalle, bina no detayı

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String district;       // ilçe

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String phone;          // teslimat telefonu

    @Column(nullable = false)
    private Boolean isDefault = false;


}
