package com.gmc.backend.member.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gmc.backend.domain.member.Member;

public class MemberInfoResponse {

    private final Long memberId;
    private final String name;
    private final String email;
    private final String role;
    private final boolean isEligible;

    private MemberInfoResponse(Long memberId, String name, String email, String role, boolean isEligible) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isEligible = isEligible;
    }

    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole().name(),
                member.isEligible()
        );
    }

    public Long getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    @JsonProperty("isEligible")
    public boolean isEligible() { return isEligible; }
}