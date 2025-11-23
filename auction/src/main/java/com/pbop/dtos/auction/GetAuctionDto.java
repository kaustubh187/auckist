package com.pbop.dtos.auction;

import com.pbop.enums.AuctionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record GetAuctionDto(
        Long auctionId,
        String auctionName,
        String auctionDesc,
        AuctionStatus status,
        BigDecimal startingPrice,

        // Owner Info
        Long sellerId,
        String sellerUsername,

        // Product Info (Simplified: just IDs)
        List<Long> productIds,

        // Bidding Info
        Long highestBidderId,
        BigDecimal currentPrice // Assuming you add this field to Auction entity
) {
}
