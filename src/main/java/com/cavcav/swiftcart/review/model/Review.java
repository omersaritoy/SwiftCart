package com.cavcav.swiftcart.review.model;

import com.cavcav.swiftcart.user.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")

public class Review extends BaseEntity {
}
