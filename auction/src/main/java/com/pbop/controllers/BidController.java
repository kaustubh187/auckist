package com.pbop.controllers;

import com.pbop.dtos.bid.CreateBidDto;
import com.pbop.dtos.bid.GetBidDto;
import com.pbop.models.User;
import com.pbop.repositories.UserRepo;
import com.pbop.services.BidService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("bids")
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private UserRepo userRepository;

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        return user.getUserId();
    }

    // 1. POST: Place a Bid on an Auction
    @PostMapping
    public ResponseEntity<GetBidDto> placeBid(
            @RequestBody @Valid CreateBidDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long buyerId = getAuthenticatedUserId(userDetails);
        GetBidDto bidDto = bidService.placeBid(dto, buyerId);
        return new ResponseEntity<>(bidDto, HttpStatus.CREATED);
    }

    // 2. GET: Get Bid History for the Authenticated User
    @GetMapping("/my-history")
    public ResponseEntity<List<GetBidDto>> getMyBidHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getAuthenticatedUserId(userDetails);
        List<GetBidDto> bids = bidService.getBidsByUserId(userId);
        return ResponseEntity.ok(bids);
    }

    // 3. GET: Get Bid History for a Specific Auction (Requires Authorization Check)
    // NOTE: This should typically be restricted to the seller or an admin.
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<GetBidDto>> getAuctionBidHistory(
            @PathVariable Long auctionId) {

        // Authorization check must be performed in the service layer
        List<GetBidDto> bids = bidService.getBidsByAuctionId(auctionId);
        return ResponseEntity.ok(bids);
    }
}
