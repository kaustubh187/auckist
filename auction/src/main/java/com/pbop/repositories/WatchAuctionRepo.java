package com.pbop.repositories;

import com.pbop.models.Auction;
import com.pbop.models.User;
import com.pbop.models.WatchAuction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WatchAuctionRepo extends JpaRepository<WatchAuction,Long> {
    Optional<WatchAuction> findByUserAndAuction(User user, Auction auction);
}
