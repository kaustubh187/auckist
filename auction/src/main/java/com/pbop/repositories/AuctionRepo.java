package com.pbop.repositories;

import com.pbop.enums.AuctionStatus;
import com.pbop.models.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepo extends JpaRepository<Auction,Long> {
    List<Auction> findByStatus(AuctionStatus status);


    List<Auction> findByStatusIn(List<AuctionStatus> statuses);
}
