package com.smecs.controller;

import com.smecs.dto.CreateReviewRequestDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.ReviewDTO;
import com.smecs.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<ReviewDTO>> createReview(@Valid @RequestBody CreateReviewRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Review submitted successfully", reviewService.createReview(request)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseDTO<PagedResponseDTO<ReviewDTO>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        // Sort by createdAt desc by default in service DAO impl, so we just pass simple pageable
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Reviews retrieved",
                reviewService.getReviewsByProduct(productId, pageable)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}


