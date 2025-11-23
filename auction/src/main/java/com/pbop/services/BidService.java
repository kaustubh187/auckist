package com.pbop.services;

import com.pbop.dtos.auction.PriceUpdateDto;
import com.pbop.dtos.bid.CreateBidDto;
import com.pbop.dtos.bid.GetBidDto;
import com.pbop.enums.AuctionStatus;
import com.pbop.enums.BidStatus;
import com.pbop.exceptions.AuctionNotFoundException;
import com.pbop.exceptions.BiddingRuleException;
import com.pbop.mappers.BidMapper;
import com.pbop.models.Auction;
import com.pbop.models.Bid;
import com.pbop.models.User;
import com.pbop.repositories.AuctionRepo;
import com.pbop.repositories.BidRepo;
import com.pbop.repositories.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BidService {
    @Autowired
    private BidRepo bidRepository;
    @Autowired private AuctionRepo auctionRepository; // To check auction status/price
    @Autowired private UserRepo userRepository;
    @Autowired private BidMapper mapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;




    // 1. Placing a Bid
    @Transactional // Essential for thread safety (concurrency)
    public GetBidDto placeBid(CreateBidDto dto, Long buyerId) {

        Auction auction = auctionRepository.findById(dto.auctionId())
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found."));

        // --- Core Business Validation ---

        // V1: Check Auction Status
        if (auction.getStatus() != AuctionStatus.Open) {
            throw new BiddingRuleException("Bids can only be placed on OPEN auctions.");
        }

        // V2: Check Bid Amount (Concurrency handled by the transaction)
        Bid highestBid = bidRepository.findTopByAuction_AuctionIdAndStatusOrderByBidPriceDesc(
                auction.getAuctionId(), BidStatus.Active);

        BigDecimal currentHighestPrice = highestBid != null ? highestBid.getBidPrice() : auction.getStartingPrice();

        if (dto.bidPrice().compareTo(currentHighestPrice) <= 0) {
            throw new BiddingRuleException("Your bid must be higher than the current highest price of " + currentHighestPrice);
        }

        // --- Update Old Bid (If exists) ---
        if (highestBid != null) {
            highestBid.setStatus(BidStatus.Inactive);
            bidRepository.save(highestBid); // Set old bid to INACTIVE
        }

        // --- Create New Bid ---
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Bid newBid = mapper.toEntity(dto);
        newBid.setBuyer(buyer);
        newBid.setAuction(auction);
        newBid.setStatus(BidStatus.Active); // Ensure it's active

        Bid savedBid = bidRepository.save(newBid);

        // --- Update Auction Metadata ---
        auction.setCurrentPrice(newBid.getBidPrice());
        auction.setHighestBidder(buyer);
        auctionRepository.save(auction);

        PriceUpdateDto updatePayload = new PriceUpdateDto(
                auction.getAuctionId(),
                auction.getCurrentPrice(),
                auction.getHighestBidder().getUsername()
        );

        // 2. Broadcast the update to all clients subscribed to this auction's topic
        String destination = "/topic/auction/" + auction.getAuctionId();
        messagingTemplate.convertAndSend(destination, updatePayload);

        return mapper.toDto(savedBid);
    }

    // 2. Get Bids for Current User
    public List<GetBidDto> getBidsByUserId(Long userId) {
        List<Bid> bids = bidRepository.findByBuyer_UserIdOrderByCreatedAtDesc(userId);
        return mapper.toDto(bids);
    }

    // 3. Get Active/All Bids for an Auction (assuming owner/admin access)
    public List<GetBidDto> getBidsByAuctionId(Long auctionId) {
        List<Bid> bids = bidRepository.findByAuction_AuctionIdOrderByBidPriceDesc(auctionId);
        return mapper.toDto(bids);
    }
}
