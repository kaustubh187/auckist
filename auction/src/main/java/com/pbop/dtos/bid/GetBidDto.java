package com.pbop.dtos.bid;

import com.pbop.enums.BidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetBidDto(
        Long bidId,
        Long auctionId,
        Long bidderId,
        String bidderUsername,
        BigDecimal bidPrice,
        BidStatus status,
        LocalDateTime createdAt
) {
}
