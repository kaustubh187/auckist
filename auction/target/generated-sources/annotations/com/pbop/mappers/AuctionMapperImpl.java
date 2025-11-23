package com.pbop.mappers;

import com.pbop.dtos.auction.CreateAuctionDto;
import com.pbop.dtos.auction.GetAuctionDto;
import com.pbop.enums.AuctionStatus;
import com.pbop.models.Auction;
import com.pbop.models.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T23:52:57+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.5 (Oracle Corporation)"
)
@Component
public class AuctionMapperImpl implements AuctionMapper {

    @Override
    public Auction toEntity(CreateAuctionDto dto) {
        if ( dto == null ) {
            return null;
        }

        Auction auction = new Auction();

        auction.setAuctionName( dto.auctionName() );
        auction.setAuctionDesc( dto.auctionDesc() );
        auction.setStatus( dto.status() );
        auction.setStartingPrice( dto.startingPrice() );

        return auction;
    }

    @Override
    public GetAuctionDto toDto(Auction auction) {
        if ( auction == null ) {
            return null;
        }

        Long sellerId = null;
        String sellerUsername = null;
        Long highestBidderId = null;
        Long auctionId = null;
        String auctionName = null;
        String auctionDesc = null;
        AuctionStatus status = null;
        BigDecimal startingPrice = null;
        BigDecimal currentPrice = null;

        sellerId = auctionSellerUserId( auction );
        sellerUsername = auctionSellerUsername( auction );
        highestBidderId = auctionHighestBidderUserId( auction );
        auctionId = auction.getAuctionId();
        auctionName = auction.getAuctionName();
        auctionDesc = auction.getAuctionDesc();
        status = auction.getStatus();
        startingPrice = auction.getStartingPrice();
        currentPrice = auction.getCurrentPrice();

        List<Long> productIds = mapProductToIds(auction.getProduct());

        GetAuctionDto getAuctionDto = new GetAuctionDto( auctionId, auctionName, auctionDesc, status, startingPrice, sellerId, sellerUsername, productIds, highestBidderId, currentPrice );

        return getAuctionDto;
    }

    @Override
    public List<GetAuctionDto> toDto(List<Auction> auctions) {
        if ( auctions == null ) {
            return null;
        }

        List<GetAuctionDto> list = new ArrayList<GetAuctionDto>( auctions.size() );
        for ( Auction auction : auctions ) {
            list.add( toDto( auction ) );
        }

        return list;
    }

    private Long auctionSellerUserId(Auction auction) {
        if ( auction == null ) {
            return null;
        }
        User seller = auction.getSeller();
        if ( seller == null ) {
            return null;
        }
        Long userId = seller.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private String auctionSellerUsername(Auction auction) {
        if ( auction == null ) {
            return null;
        }
        User seller = auction.getSeller();
        if ( seller == null ) {
            return null;
        }
        String username = seller.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    private Long auctionHighestBidderUserId(Auction auction) {
        if ( auction == null ) {
            return null;
        }
        User highestBidder = auction.getHighestBidder();
        if ( highestBidder == null ) {
            return null;
        }
        Long userId = highestBidder.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }
}
