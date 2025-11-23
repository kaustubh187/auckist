package com.pbop.models;


import com.pbop.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionId;

    @Column(nullable = false,length = 25)
    private String auctionName;

    @Column(nullable = false,length = 250)
    private String auctionDesc;

    @OneToOne
    @JoinColumn(name = "productId")
    private Product product;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Column(nullable = false,precision = 19,scale = 4)
    private BigDecimal startingPrice;

    @Column(nullable = false,precision = 19,scale = 4,columnDefinition = "DECIMAL(19,4) DEFAULT 0.0000")
    private BigDecimal currentPrice = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name="userId",nullable = false)
    private User seller;

    @OneToMany(mappedBy ="auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bid> bids = new HashSet<>();

    @ManyToOne
    @JoinColumn(name="highest_bidder_id",nullable = true)
    private User highestBidder;

}
