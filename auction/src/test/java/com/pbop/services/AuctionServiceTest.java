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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepo auctionRepository;

    @Mock
    private ProductRepo productRepository;

    @Mock
    private AuctionMapper mapper;

    @Mock
    private UserRepo userRepository;

    @InjectMocks
    private AuctionService auctionService;

    // ---------- createAuction tests ----------

    @Test
    void createAuction_success_singleProduct_ownedBySeller() {
        Long sellerId = 10L;
        Long productId = 100L;

        CreateAuctionDto dto = new CreateAuctionDto(
                "Auction 1",
                "Desc",
                List.of(productId),
                BigDecimal.TEN,
                null
        );

        User owner = new User();
        owner.setUserId(sellerId);

        Product product = new Product();
        product.setProductId(productId);
        product.setOwner(owner);

        User seller = new User();
        seller.setUserId(sellerId);
        seller.setUsername("sellerUser");

        Auction auctionEntity = new Auction();
        auctionEntity.setAuctionName("Auction 1");
        auctionEntity.setStartingPrice(BigDecimal.TEN);

        Auction savedAuction = new Auction();
        savedAuction.setAuctionId(1L);
        savedAuction.setAuctionName("Auction 1");
        savedAuction.setStartingPrice(BigDecimal.TEN);
        savedAuction.setSeller(seller);
        savedAuction.setProduct(product);
        savedAuction.setStatus(AuctionStatus.Upcoming);

        GetAuctionDto expectedDto = new GetAuctionDto(
                1L,
                "Auction 1",
                "Desc",
                AuctionStatus.Upcoming,
                BigDecimal.TEN,
                sellerId,
                "sellerUser",
                List.of(productId),
                null,
                null
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(mapper.toEntity(dto)).thenReturn(auctionEntity);
        when(auctionRepository.save(auctionEntity)).thenReturn(savedAuction);
        when(mapper.toDto(savedAuction)).thenReturn(expectedDto);

        GetAuctionDto result = auctionService.createAuction(dto, sellerId);

        assertSame(expectedDto, result);
        assertEquals(AuctionStatus.Upcoming, auctionEntity.getStatus());
        assertSame(product, auctionEntity.getProduct());
        assertSame(seller, auctionEntity.getSeller());

        verify(productRepository).findById(productId);
        verify(userRepository).findById(sellerId);
        verify(auctionRepository).save(auctionEntity);
        verify(mapper).toDto(savedAuction);
    }

    @Test
    void createAuction_multipleProducts_throwsAuctionCreationException() {
        Long sellerId = 10L;
        CreateAuctionDto dto = new CreateAuctionDto(
                "Auction 1",
                "Desc",
                List.of(1L, 2L),
                BigDecimal.TEN,
                null
        );

        assertThrows(AuctionCreationException.class,
                () -> auctionService.createAuction(dto, sellerId));

        verifyNoInteractions(productRepository, userRepository, mapper, auctionRepository);
    }

    @Test
    void createAuction_productNotFound_throwsAuctionCreationException() {
        Long sellerId = 10L;
        Long productId = 100L;

        CreateAuctionDto dto = new CreateAuctionDto(
                "Auction 1",
                "Desc",
                List.of(productId),
                BigDecimal.TEN,
                null
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(AuctionCreationException.class,
                () -> auctionService.createAuction(dto, sellerId));

        verify(productRepository).findById(productId);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(userRepository, mapper, auctionRepository);
    }

    @Test
    void createAuction_notOwner_throwsAuctionCreationException() {
        Long sellerId = 10L;
        Long productId = 100L;

        CreateAuctionDto dto = new CreateAuctionDto(
                "Auction 1",
                "Desc",
                List.of(productId),
                BigDecimal.TEN,
                null
        );

        User differentOwner = new User();
        differentOwner.setUserId(99L);

        Product product = new Product();
        product.setProductId(productId);
        product.setOwner(differentOwner);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(AuctionCreationException.class,
                () -> auctionService.createAuction(dto, sellerId));

        verify(productRepository).findById(productId);
        verifyNoInteractions(userRepository, mapper, auctionRepository);
    }

    // ---------- getAuctionById tests ----------

    @Test
    void getAuctionById_success() {
        Long auctionId = 1L;

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setAuctionName("Auction 1");

        GetAuctionDto dto = new GetAuctionDto(
                auctionId,
                "Auction 1",
                "Desc",
                AuctionStatus.Upcoming,
                BigDecimal.TEN,
                10L,
                "sellerUser",
                List.of(100L),
                null,
                null
        );

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(mapper.toDto(auction)).thenReturn(dto);

        GetAuctionDto result = auctionService.getAuctionById(auctionId);

        assertSame(dto, result);
        verify(auctionRepository).findById(auctionId);
        verify(mapper).toDto(auction);
    }

    @Test
    void getAuctionById_notFound_throwsAuctionNotFoundException() {
        Long auctionId = 99L;

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        assertThrows(AuctionNotFoundException.class,
                () -> auctionService.getAuctionById(auctionId));

        verify(mapper, never()).toDto(any(Auction.class));
    }

    // ---------- getAllAuctions tests ----------

    @Test
    void getAllAuctions_success() {
        Auction a1 = new Auction();
        Auction a2 = new Auction();
        List<Auction> auctions = List.of(a1, a2);

        GetAuctionDto d1 = new GetAuctionDto(
                1L, "A1", "D1", AuctionStatus.Upcoming,
                BigDecimal.ONE, 10L, "u1", List.of(100L), null, null
        );
        GetAuctionDto d2 = new GetAuctionDto(
                2L, "A2", "D2", AuctionStatus.Open,
                BigDecimal.TEN, 11L, "u2", List.of(101L), null, null
        );
        List<GetAuctionDto> dtoList = List.of(d1, d2);

        when(auctionRepository.findAll()).thenReturn(auctions);
        when(mapper.toDto(auctions)).thenReturn(dtoList);

        List<GetAuctionDto> result = auctionService.getAllAuctions();

        assertSame(dtoList, result);
        assertEquals(2, result.size());
        verify(auctionRepository).findAll();
        verify(mapper).toDto(auctions);
    }

    // ---------- getUpcomingAuctions tests ----------

    @Test
    void getUpcomingAuctions_success() {
        Auction a1 = new Auction();
        a1.setStatus(AuctionStatus.Upcoming);
        Auction a2 = new Auction();
        a2.setStatus(AuctionStatus.Upcoming);
        List<Auction> auctions = List.of(a1, a2);

        GetAuctionDto d1 = new GetAuctionDto(
                1L, "A1", "D1", AuctionStatus.Upcoming,
                BigDecimal.ONE, 10L, "u1", List.of(100L), null, null
        );
        GetAuctionDto d2 = new GetAuctionDto(
                2L, "A2", "D2", AuctionStatus.Upcoming,
                BigDecimal.TEN, 11L, "u2", List.of(101L), null, null
        );
        List<GetAuctionDto> dtoList = List.of(d1, d2);

        when(auctionRepository.findByStatus(AuctionStatus.Upcoming)).thenReturn(auctions);
        when(mapper.toDto(auctions)).thenReturn(dtoList);

        List<GetAuctionDto> result = auctionService.getUpcomingAuctions();

        assertSame(dtoList, result);
        assertEquals(2, result.size());
        verify(auctionRepository).findByStatus(AuctionStatus.Upcoming);
        verify(mapper).toDto(auctions);
    }

    // ---------- deleteAuction tests ----------

    @Test
    void deleteAuction_success_ownerAndNotClosed() {
        Long auctionId = 1L;
        Long userId = 10L;

        User seller = new User();
        seller.setUserId(userId);

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.Upcoming);

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        auctionService.deleteAuction(auctionId, userId);

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository).delete(auction);
    }

    @Test
    void deleteAuction_notOwner_throwsAuctionCreationException() {
        Long auctionId = 1L;
        Long userId = 10L; // caller

        User seller = new User();
        seller.setUserId(99L); // different owner

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.Upcoming);

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        assertThrows(AuctionCreationException.class,
                () -> auctionService.deleteAuction(auctionId, userId));

        verify(auctionRepository, never()).delete(any());
    }

    @Test
    void deleteAuction_closed_throwsAuctionCreationException() {
        Long auctionId = 1L;
        Long userId = 10L;

        User seller = new User();
        seller.setUserId(userId);

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.Closed);

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        assertThrows(AuctionCreationException.class,
                () -> auctionService.deleteAuction(auctionId, userId));

        verify(auctionRepository, never()).delete(any());
    }

    // ---------- closeAuction tests ----------

    @Test
    void closeAuction_success_whenOpen_andSeller() {
        Long auctionId = 1L;
        Long userId = 10L;

        User seller = new User();
        seller.setUserId(userId);

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.Open);

        Auction saved = new Auction();
        saved.setAuctionId(auctionId);
        saved.setSeller(seller);
        saved.setStatus(AuctionStatus.Closed);

        GetAuctionDto dto = new GetAuctionDto(
                auctionId, "A1", "D1", AuctionStatus.Closed,
                BigDecimal.TEN, userId, "sellerUser", List.of(100L), null, null
        );

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(auction)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(dto);

        GetAuctionDto result = auctionService.closeAuction(auctionId, userId, UserRole.ADMIN);

        assertSame(dto, result);
        assertEquals(AuctionStatus.Closed, auction.getStatus());
        verify(auctionRepository).save(auction);
        verify(mapper).toDto(saved);
    }

    @Test
    void closeAuction_notFound_throwsAuctionNotFoundException() {
        Long auctionId = 1L;
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        assertThrows(AuctionNotFoundException.class,
                () -> auctionService.closeAuction(auctionId, 10L, UserRole.ADMIN));

        verify(auctionRepository, never()).save(any());
    }

    @Test
    void closeAuction_notOpen_throwsAuctionCreationException() {
        Long auctionId = 1L;

        User seller = new User();
        seller.setUserId(10L);

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.Closed);

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        assertThrows(AuctionCreationException.class,
                () -> auctionService.closeAuction(auctionId, 10L, UserRole.ADMIN));

        verify(auctionRepository, never()).save(any());
    }

    // ---------- startAuction tests ----------

    @Test
    void startAuction_success_fromUpcoming_bySeller() {
        Long auctionId = 1L;
        Long userId = 10L;

        User seller = new User();
        seller.setUserId(userId);

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.Upcoming);

        Auction saved = new Auction();
        saved.setAuctionId(auctionId);
        saved.setSeller(seller);
        saved.setStatus(AuctionStatus.Open);

        GetAuctionDto dto = new GetAuctionDto(
                auctionId, "A1", "D1", AuctionStatus.Open,
                BigDecimal.TEN, userId, "sellerUser", List.of(100L), null, null
        );

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(auction)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(dto);

        GetAuctionDto result = auctionService.startAuction(auctionId, userId, UserRole.ADMIN);

        assertSame(dto, result);
        assertEquals(AuctionStatus.Open, auction.getStatus());
        verify(auctionRepository).save(auction);
        verify(mapper).toDto(saved);
    }

    @Test
    void startAuction_alreadyOpen_throwsAuctionCreationException() {
        Long auctionId = 1L;

        User seller = new User();
        seller.setUserId(10L);

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.Open);

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        assertThrows(AuctionCreationException.class,
                () -> auctionService.startAuction(auctionId, 10L, UserRole.ADMIN));

        verify(auctionRepository, never()).save(any());
    }
}
