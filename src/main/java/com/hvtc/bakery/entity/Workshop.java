package com.hvtc.bakery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "workshops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    @Column(length = 50)
    private String duration; // e.g. "3 giờ"

    @Column(nullable = false)
    private Integer capacity; // total slots

    @Column(name = "registered_count")
    @Builder.Default
    private Integer registeredCount = 0;

    @Column(length = 255)
    private String image;

    @Column(length = 255)
    private String location;

    @Column(name = "workshop_date")
    private LocalDateTime workshopDate;

    @Builder.Default
    private boolean status = true;
}
