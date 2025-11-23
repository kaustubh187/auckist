package com.pbop.models;

import com.pbop.enums.BidStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;

    @ManyToOne
    @JoinColumn(name = "buyer_id",nullable = false)
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "auctionId",nullable = false)
    private Auction auction;

    @Column(nullable = false)
    private BigDecimal bidPrice;

    @Enumerated(EnumType.STRING)
    private BidStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

}
