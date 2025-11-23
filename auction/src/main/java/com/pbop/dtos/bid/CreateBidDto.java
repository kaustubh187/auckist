package com.pbop.dtos.bid;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateBidDto(
        @NotNull(message = "Auction ID is required.")
        Long auctionId,

        @NotNull(message = "Bid amount is required.")
        @Positive(message = "Bid amount must be positive.")
        BigDecimal bidPrice
) {
}
