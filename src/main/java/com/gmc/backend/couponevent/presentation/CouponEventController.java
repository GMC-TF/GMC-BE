package com.gmc.backend.couponevent.presentation;

import com.gmc.backend.common.response.ApiResponse;
import com.gmc.backend.couponevent.application.CouponEventService;
import com.gmc.backend.couponevent.application.dto.CouponEventResponse;
import com.gmc.backend.couponevent.application.dto.CouponEventStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/coupon-events")
@RequiredArgsConstructor
public class CouponEventController {

    private final CouponEventService couponEventService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponEventStatusResponse>>> getAllEventStatus(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(couponEventService.getAllEventStatus()));
    }

    @PatchMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CouponEventResponse>> updateEvent(
            @PathVariable Long eventId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal String email) {
        CouponEventResponse response = couponEventService.updateEvent(eventId, name, description, startAt, image);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal String email) {
        couponEventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CouponEventResponse>> createEvent(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam MultipartFile image,
            @RequestParam int quantity,
            @AuthenticationPrincipal String email) {
        CouponEventResponse response = couponEventService.createEvent(name, description, startAt, image, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}