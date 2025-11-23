package com.pbop.services;

import com.pbop.dtos.auction.CreateAuctionDto;
import com.pbop.dtos.auction.GetAuctionDto;
import com.pbop.enums.AuctionStatus;
import com.pbop.enums.UserRole;
import com.pbop.exceptions.AuctionCreationException;
import com.pbop.exceptions.AuctionNotFoundException;
import com.pbop.mappers.AuctionMapper;
import com.pbop.models.Auction;
import com.pbop.models.Product;
import com.pbop.models.User;
import com.pbop.repositories.AuctionRepo;
import com.pbop.repositories.ProductRepo;
import com.pbop.repositories.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionService {
    @Autowired
    private AuctionRepo auctionRepository;
    @Autowired private ProductRepo productRepository;
    @Autowired private AuctionMapper mapper;
    @Autowired private UserRepo userRepository;

    // --- Public methods from the Service Interface (not shown for brevity) ---

    // 1. Create Auction (Handles Product Ownership and Multiple Products)
    @Transactional
    public GetAuctionDto createAuction(CreateAuctionDto dto, Long sellerId) {
        if (dto.productIds().size() != 1) {
            // Restriction enforced: Only one product per auction for simplicity
            throw new AuctionCreationException("Auctions must be created with exactly one product.");
        }

        // 1. Validate and fetch Product
        Long productId = dto.productIds().get(0);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AuctionCreationException("Product not found with ID: " + productId));

        // 2. Validate Seller Ownership (Business Logic)
        if (!product.getOwner().getUserId().equals(sellerId)) {
            throw new AuctionCreationException("You can only create an auction for products you own.");
        }

        // 3. Fetch Seller (Owner)
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found.")); // Should not happen

        // 4. Map and Set Final Entity Properties
        Auction auction = mapper.toEntity(dto);
        auction.setProduct(product);
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.Upcoming); // Force default status

        Auction savedAuction = auctionRepository.save(auction);
        return mapper.toDto(savedAuction);
    }

    // 2. Get Auction by ID (Requires LAZY loading fix if not using JOIN FETCH)
    @Transactional
    public GetAuctionDto getAuctionById(Long id) {
        // Use JOIN FETCH to load product, owner, and bids for a single view
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + id));

        // NOTE: Since this is getById, we need to ensure LAZY loading works,
        // or we use a custom JOIN FETCH method here too (best practice).
        return mapper.toDto(auction);
    }

    // 3. Get All Auctions
    public List<GetAuctionDto> getAllAuctions() {
        try{
            List<Auction> auctions = auctionRepository.findAll();
            return mapper.toDto(auctions);
        }
        catch (Exception e){
            throw new RuntimeException("Unfortunately we got this error : " + e);
        }

        // NOTE: This will trigger N+1 if the DTO requires lazy fields.
        // Best practice is to use a specific DTO or a fetch query here.

    }

    // 4. Get Upcoming Auctions
    public List<GetAuctionDto> getUpcomingAuctions() {
        List<Auction> auctions = auctionRepository.findByStatus(AuctionStatus.Upcoming);
        return mapper.toDto(auctions);
    }

    // 5. Delete Auction (Only if UPCOMING or OPEN, and by owner)
    @Transactional
    public void deleteAuction(Long auctionId, Long userId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + auctionId));

        if (!auction.getSeller().getUserId().equals(userId)) {
            throw new AuctionCreationException("You are not authorized to delete this auction.");
        }

        if (auction.getStatus() == AuctionStatus.Closed) {
            throw new AuctionCreationException("Cannot delete a closed auction.");
        }

        auctionRepository.delete(auction);
    }

    // 6. Close Auction (Admin or Seller can manually close an OPEN auction)
    @Transactional
    public GetAuctionDto closeAuction(Long auctionId, Long userId, UserRole role) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + auctionId));

        if (auction.getStatus() != AuctionStatus.Open) {
            throw new AuctionCreationException("Auction is not currently open and cannot be closed.");
        }

        // Authorization check: Must be the seller OR an admin
        if (!auction.getSeller().getUserId().equals(userId) && role != UserRole.ADMIN) {
            throw new AuctionCreationException("Authorization required to close auction.");
        }

        auction.setStatus(AuctionStatus.Closed);
        // NOTE: Real system needs logic here to determine winner and finalize payment.

        return mapper.toDto(auctionRepository.save(auction));
    }

    @Transactional
    public GetAuctionDto startAuction(Long auctionId, Long userId, UserRole role) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + auctionId));

        if (auction.getStatus() == AuctionStatus.Open) {
            throw new AuctionCreationException("Auction is already currently open.");
        }

        if (!auction.getSeller().getUserId().equals(userId) && role != UserRole.ADMIN) {
            throw new AuctionCreationException("Authorization required to close auction.");
        }

        auction.setStatus(AuctionStatus.Open);
        // NOTE: Real system needs logic here to determine winner and finalize payment.

        return mapper.toDto(auctionRepository.save(auction));
    }
}
