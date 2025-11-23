package com.pbop.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiptId;

    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id",nullable = false)
    private User buyer;

    private BigDecimal amount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionId",nullable = false)
    private Auction auction;

    @Column(precision = 19,scale = 4)
    private BigDecimal extraCharges;

    @Column(nullable = false,updatable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
}
