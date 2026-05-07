package com.gmc.backend.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Member(String email, String name, String profileImage, Role role) {
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.role = role;
    }

    public Member update(String name, String profileImage) {
        this.name = name;
        this.profileImage = profileImage;
        return this;
    }
}