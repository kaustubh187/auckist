package com.pbop.models;

import com.pbop.enums.ProductCategory;
import com.pbop.enums.ReminderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ReminderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private ReminderType reminderType;

    @Column(nullable = true)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ProductCategory productCategory;

    @ManyToOne
    @JoinColumn(name = "auctionId")
    private Auction auction;

    @Column(nullable = false)
    private LocalDateTime targetDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
