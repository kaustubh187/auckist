package com.pbop.controllers;

import com.pbop.dtos.auction.CreateAuctionDto;
import com.pbop.dtos.auction.GetAuctionDto;
import com.pbop.models.User;
import com.pbop.repositories.UserRepo;
import com.pbop.services.AuctionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;
    @Autowired private UserRepo userRepository;

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        return user.getUserId();
    }


    @PostMapping
    public ResponseEntity<GetAuctionDto> createAuction(
            @RequestBody @Valid CreateAuctionDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getAuthenticatedUserId(userDetails);
        GetAuctionDto auctionDto = auctionService.createAuction(dto, sellerId);
        return new ResponseEntity<>(auctionDto, HttpStatus.CREATED);
    }

    // 2. GET: Get All Auctions
    @GetMapping
    public ResponseEntity<List<GetAuctionDto>> getAllAuctions() {
        List<GetAuctionDto> auctions = auctionService.getAllAuctions();
        return ResponseEntity.ok(auctions);
    }

    // 3. GET: Get Upcoming Auctions
    @GetMapping("/upcoming")
    public ResponseEntity<List<GetAuctionDto>> getUpcomingAuctions() {
        List<GetAuctionDto> auctions = auctionService.getUpcomingAuctions();
        return ResponseEntity.ok(auctions);
    }

    // 4. GET: Get Auction by ID
    @GetMapping("/{auctionId}")
    public ResponseEntity<GetAuctionDto> getAuctionById(@PathVariable Long auctionId) {
        GetAuctionDto auctionDto = auctionService.getAuctionById(auctionId);
        return ResponseEntity.ok(auctionDto);
    }

    // 5. DELETE: Delete Auction
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> deleteAuction(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getAuthenticatedUserId(userDetails);
        auctionService.deleteAuction(auctionId, userId);
        return ResponseEntity.noContent().build();
    }

    // 6. POST: Close Auction
    @PostMapping("/{auctionId}/close")
    public ResponseEntity<GetAuctionDto> closeAuction(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername());

        GetAuctionDto auctionDto = auctionService.closeAuction(auctionId, user.getUserId(), user.getRole());
        return ResponseEntity.ok(auctionDto);
    }

    @PostMapping("/{auctionId}/start")
    public ResponseEntity<GetAuctionDto> startAuction(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername());

        GetAuctionDto auctionDto = auctionService.startAuction(auctionId, user.getUserId(), user.getRole());
        return ResponseEntity.ok(auctionDto);
    }
}