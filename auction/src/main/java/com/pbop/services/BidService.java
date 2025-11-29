package com.pbop.services;

import com.pbop.config.GlobalExceptionHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BidService {
    private final BidRepo bidRepository;
    private final AuctionRepo auctionRepository; // To check auction status/price
    private final UserRepo userRepository;
    private final BidMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;// For WebSocket messaging

    private static final Logger log = LoggerFactory.getLogger(BidService.class);

    @Autowired
    public BidService(BidRepo bidRepository, AuctionRepo auctionRepository, UserRepo userRepository, BidMapper mapper, SimpMessagingTemplate messagingTemplate) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.messagingTemplate = messagingTemplate;
    }


    // 1. Placing a Bid
    @Transactional // Essential for thread safety (concurrency)
    public GetBidDto placeBid(CreateBidDto dto, Long buyerId) {
        log.debug("Placing bid: {} by user: {}", dto, buyerId);
        Auction auction = auctionRepository.findById(dto.auctionId())
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found."));

        // --- Core Business Validation ---

        // V1: Check Auction Status
        if (auction.getStatus() != AuctionStatus.Open) {
            log.error("Attempted to bid on non-open auction: {}", auction.getAuctionId());
            throw new BiddingRuleException("Bids can only be placed on OPEN auctions.");
        }

        // V2: Check Bid Amount (Concurrency handled by the transaction)
        Bid highestBid = bidRepository.findTopByAuction_AuctionIdAndStatusOrderByBidPriceDesc(
                auction.getAuctionId(), BidStatus.Active);

        BigDecimal currentHighestPrice = highestBid != null ? highestBid.getBidPrice() : auction.getStartingPrice();

        if (dto.bidPrice().compareTo(currentHighestPrice) <= 0) {
            log.error("Bid price {} is not higher than current highest price {} for auction {}",
                    dto.bidPrice(), currentHighestPrice, auction.getAuctionId());
            throw new BiddingRuleException("Your bid must be higher than the current highest price of " + currentHighestPrice);
        }

        // --- Update Old Bid (If exists) ---
        if (highestBid != null) {
            highestBid.setStatus(BidStatus.Inactive);
            bidRepository.save(highestBid); // Set old bid to INACTIVE
        }

        log.debug("Old highest bid updated to INACTIVE for auction: {}", auction.getAuctionId());
        // --- Create New Bid ---
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Bid newBid = mapper.toEntity(dto);
        newBid.setBuyer(buyer);
        newBid.setAuction(auction);
        newBid.setStatus(BidStatus.Active); // Ensure it's active

        log.debug("Creating new bid entity: {}", newBid);
        Bid savedBid = bidRepository.save(newBid);

        // --- Update Auction Metadata ---
        log.debug("Updating auction {} with new highest bid {}", auction.getAuctionId(), newBid.getBidPrice());
        auction.setCurrentPrice(newBid.getBidPrice());
        auction.setHighestBidder(buyer);
        auctionRepository.save(auction);

        PriceUpdateDto updatePayload = new PriceUpdateDto(
                auction.getAuctionId(),
                auction.getCurrentPrice(),
                auction.getHighestBidder().getUsername()
        );

        log.debug("Broadcasting price update: {}", updatePayload);
        // 2. Broadcast the update to all clients subscribed to this auction's topic
        String destination = "/topic/auction/" + auction.getAuctionId();
        messagingTemplate.convertAndSend(destination, updatePayload);

        return mapper.toDto(savedBid);
    }

    // 2. Get Bids for Current User
    public List<GetBidDto> getBidsByUserId(Long userId) {
        List<Bid> bids = bidRepository.findByBuyer_UserIdOrderByCreatedAtDesc(userId);
        log.debug("Fetched {} bids for user {}", bids.size(), userId);
        return mapper.toDto(bids);
    }

    // 3. Get Active/All Bids for an Auction (assuming owner/admin access)
    public List<GetBidDto> getBidsByAuctionId(Long auctionId) {
        List<Bid> bids = bidRepository.findByAuction_AuctionIdOrderByBidPriceDesc(auctionId);
        log.debug("Fetched {} bids for auction {}", bids.size(), auctionId);
        return mapper.toDto(bids);
    }
}
