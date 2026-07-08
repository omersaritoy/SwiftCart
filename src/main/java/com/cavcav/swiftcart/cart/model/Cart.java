package com.cavcav.swiftcart.cart.model;

import com.cavcav.swiftcart.user.model.BaseEntity;
import com.cavcav.swiftcart.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<CartItem> items=new ArrayList<>();

}
