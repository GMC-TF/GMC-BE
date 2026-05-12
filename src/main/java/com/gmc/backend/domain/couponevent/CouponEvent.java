package com.gmc.backend.domain.couponevent;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CouponEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Builder
    public CouponEvent(String name, String description, LocalDateTime startAt) {
        this.name = name;
        this.description = description;
        this.startAt = startAt;
    }

    public void update(String name, String description, LocalDateTime startAt) {
        this.name = name;
        this.description = description;
        this.startAt = startAt;
    }
}