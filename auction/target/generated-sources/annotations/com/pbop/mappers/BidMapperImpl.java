package com.pbop.mappers;

import com.pbop.dtos.bid.CreateBidDto;
import com.pbop.dtos.bid.GetBidDto;
import com.pbop.enums.BidStatus;
import com.pbop.models.Auction;
import com.pbop.models.Bid;
import com.pbop.models.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class BidMapperImpl implements BidMapper {

    @Override
    public Bid toEntity(CreateBidDto dto) {
        if ( dto == null ) {
            return null;
        }

        Bid bid = new Bid();

        bid.setBidPrice( dto.bidPrice() );

        bid.setStatus( BidStatus.Active );

        return bid;
    }

    @Override
    public GetBidDto toDto(Bid bid) {
        if ( bid == null ) {
            return null;
        }

        Long auctionId = null;
        Long bidderId = null;
        String bidderUsername = null;
        Long bidId = null;
        BigDecimal bidPrice = null;
        BidStatus status = null;
        LocalDateTime createdAt = null;

        auctionId = bidAuctionAuctionId( bid );
        bidderId = bidBuyerUserId( bid );
        bidderUsername = bidBuyerUsername( bid );
        bidId = bid.getBidId();
        bidPrice = bid.getBidPrice();
        status = bid.getStatus();
        createdAt = bid.getCreatedAt();

        GetBidDto getBidDto = new GetBidDto( bidId, auctionId, bidderId, bidderUsername, bidPrice, status, createdAt );

        return getBidDto;
    }

    @Override
    public List<GetBidDto> toDto(List<Bid> bids) {
        if ( bids == null ) {
            return null;
        }

        List<GetBidDto> list = new ArrayList<GetBidDto>( bids.size() );
        for ( Bid bid : bids ) {
            list.add( toDto( bid ) );
        }

        return list;
    }

    private Long bidAuctionAuctionId(Bid bid) {
        if ( bid == null ) {
            return null;
        }
        Auction auction = bid.getAuction();
        if ( auction == null ) {
            return null;
        }
        Long auctionId = auction.getAuctionId();
        if ( auctionId == null ) {
            return null;
        }
        return auctionId;
    }

    private Long bidBuyerUserId(Bid bid) {
        if ( bid == null ) {
            return null;
        }
        User buyer = bid.getBuyer();
        if ( buyer == null ) {
            return null;
        }
        Long userId = buyer.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private String bidBuyerUsername(Bid bid) {
        if ( bid == null ) {
            return null;
        }
        User buyer = bid.getBuyer();
        if ( buyer == null ) {
            return null;
        }
        String username = buyer.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
