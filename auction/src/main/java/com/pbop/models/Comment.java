package com.pbop.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(name = "commentText", length = 250)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId",nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionId",nullable = true)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewId",nullable = true)
    private Review review;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
