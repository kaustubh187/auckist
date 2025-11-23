package com.pbop.repositories;

import com.pbop.enums.BidStatus;
import com.pbop.models.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepo extends JpaRepository<Bid,Long> {
    List<Bid> findByAuction_AuctionIdOrderByBidPriceDesc(Long auctionId);


    List<Bid> findByBuyer_UserIdOrderByCreatedAtDesc(Long userId);


    Bid findTopByAuction_AuctionIdAndStatusOrderByBidPriceDesc(Long auctionId, BidStatus status);
}
