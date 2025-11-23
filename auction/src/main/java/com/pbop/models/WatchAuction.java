package com.pbop.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "watch_auction", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "auction_id"})})
@EqualsAndHashCode(of = "id")
public class WatchAuction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who is watching

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction; // The auction being watched

    private LocalDateTime addedAt = LocalDateTime.now();
}
