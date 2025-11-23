package com.pbop.mappers;

import com.pbop.dtos.bid.CreateBidDto;
import com.pbop.dtos.bid.GetBidDto;
import com.pbop.models.Bid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BidMapper {
    @Mapping(target = "bidId", ignore = true)
    @Mapping(target = "auction", ignore = true)
    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "status", constant = "Active") // New bids are always ACTIVE
    @Mapping(target = "createdAt", ignore = true)
    Bid toEntity(CreateBidDto dto);

    // Entity to DTO (Used for fetching bid history)
    @Mapping(target = "auctionId", source = "auction.auctionId")
    @Mapping(target = "bidderId", source = "buyer.userId")
    @Mapping(target = "bidderUsername", source = "buyer.username")
    GetBidDto toDto(Bid bid);

    List<GetBidDto> toDto(List<Bid> bids);
}
