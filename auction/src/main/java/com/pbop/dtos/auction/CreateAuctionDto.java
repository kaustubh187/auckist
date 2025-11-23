package com.pbop.dtos.auction;

import com.pbop.enums.AuctionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateAuctionDto(
        @NotBlank(message = "Auction name is required.")
        String auctionName,

        @NotBlank(message = "Description is required.")
        String auctionDesc,


        @NotNull(message = "At least one product ID is required.")
        List<Long> productIds,

        @NotNull(message = "Starting price is required.")
        java.math.BigDecimal startingPrice,


        AuctionStatus status
) {
}
