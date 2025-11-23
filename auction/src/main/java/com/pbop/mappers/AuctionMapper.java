package com.pbop.mappers;

import com.pbop.dtos.auction.CreateAuctionDto;
import com.pbop.dtos.auction.GetAuctionDto;
import com.pbop.models.Auction;
import com.pbop.models.Bid;
import com.pbop.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuctionMapper {

    // Helper method to extract Bids to Set<Long>
    default Set<Long> mapBidsToIds(Set<Bid> bids) {
        if (bids == null) return Set.of();
        return bids.stream().map(Bid::getBidId).collect(Collectors.toSet());
    }

    // Helper method to extract Product to List<Long> (Handles single product case)
    default List<Long> mapProductToIds(Product product) {
        if (product == null) return List.of();
        return List.of(product.getProductId());
    }

    // DTO to Entity (Used in POST)
    @Mapping(target = "auctionId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "bids", ignore = true)
    @Mapping(target = "highestBidder", ignore = true)
    Auction toEntity(CreateAuctionDto dto);

    // Entity to DTO (Used in GET)
    @Mapping(target = "sellerId", source = "seller.userId")
    @Mapping(target = "sellerUsername", source = "seller.username")
    @Mapping(target = "productIds", expression = "java(mapProductToIds(auction.getProduct()))")
    @Mapping(target = "highestBidderId", source = "highestBidder.userId")
    GetAuctionDto toDto(Auction auction);

    List<GetAuctionDto> toDto(List<Auction> auctions);
}
