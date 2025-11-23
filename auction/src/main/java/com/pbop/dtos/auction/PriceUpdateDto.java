package com.pbop.dtos.auction;

import java.math.BigDecimal;

public record PriceUpdateDto(
        Long auctionId,
        BigDecimal currentPrice,
        String highestBidderUsername
) { }
