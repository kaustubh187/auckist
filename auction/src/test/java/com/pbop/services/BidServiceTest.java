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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepo bidRepository;

    @Mock
    private AuctionRepo auctionRepository;

    @Mock
    private UserRepo userRepository;

    @Mock
    private BidMapper mapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BidService bidService;

    // ---------- placeBid tests ----------

    @Test
    void placeBid_success_noExistingHighestBid() {
        Long auctionId = 1L;
        Long buyerId = 10L;

        CreateBidDto dto = new CreateBidDto(auctionId, BigDecimal.valueOf(200));

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setStatus(AuctionStatus.Open);
        auction.setStartingPrice(BigDecimal.valueOf(100));
        auction.setCurrentPrice(BigDecimal.valueOf(100));

        User buyer = new User();
        buyer.setUserId(buyerId);
        buyer.setUsername("buyerUser");

        Bid newBidEntity = new Bid();
        newBidEntity.setBidPrice(dto.bidPrice());

        Bid savedBid = new Bid();
        savedBid.setBidPrice(dto.bidPrice());
        savedBid.setStatus(BidStatus.Active);

        GetBidDto expectedDto = new GetBidDto(
                1L,
                auctionId,
                buyerId,
                "buyerUser",
                dto.bidPrice(),
                BidStatus.Active,
                LocalDateTime.now()
        );

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(bidRepository.findTopByAuction_AuctionIdAndStatusOrderByBidPriceDesc(
                auctionId, BidStatus.Active))
                .thenReturn(null);

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(mapper.toEntity(dto)).thenReturn(newBidEntity);
        when(bidRepository.save(newBidEntity)).thenReturn(savedBid);
        when(mapper.toDto(savedBid)).thenReturn(expectedDto);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PriceUpdateDto> payloadCaptor = ArgumentCaptor.forClass(PriceUpdateDto.class);

        GetBidDto result = bidService.placeBid(dto, buyerId);

        assertSame(expectedDto, result);

        assertEquals(dto.bidPrice(), auction.getCurrentPrice());
        assertSame(buyer, auction.getHighestBidder());
        verify(auctionRepository).save(auction);

        // no old highest bid existed, so we only expect save(newBidEntity)
        verify(bidRepository, times(1)).save(newBidEntity);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
        assertEquals("/topic/auction/" + auctionId, destinationCaptor.getValue());
        PriceUpdateDto payload = payloadCaptor.getValue();
        assertEquals(auctionId, payload.auctionId());
        assertEquals(dto.bidPrice(), payload.currentPrice());
        assertEquals("buyerUser", payload.highestBidderUsername());
    }

    @Test
    void placeBid_success_withExistingHighestBid_marksOldInactive() {
        Long auctionId = 1L;
        Long buyerId = 20L;

        CreateBidDto dto = new CreateBidDto(auctionId, BigDecimal.valueOf(300));

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setStatus(AuctionStatus.Open);
        auction.setStartingPrice(BigDecimal.valueOf(100));
        auction.setCurrentPrice(BigDecimal.valueOf(200));

        User buyer = new User();
        buyer.setUserId(buyerId);
        buyer.setUsername("newBuyer");

        Bid existingHighest = new Bid();
        existingHighest.setBidPrice(BigDecimal.valueOf(200));
        existingHighest.setStatus(BidStatus.Active);

        Bid newBidEntity = new Bid();
        newBidEntity.setBidPrice(dto.bidPrice());

        Bid savedBid = new Bid();
        savedBid.setBidPrice(dto.bidPrice());
        savedBid.setStatus(BidStatus.Active);

        GetBidDto expectedDto = new GetBidDto(
                2L,
                auctionId,
                buyerId,
                "newBuyer",
                dto.bidPrice(),
                BidStatus.Active,
                LocalDateTime.now()
        );

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(bidRepository.findTopByAuction_AuctionIdAndStatusOrderByBidPriceDesc(
                auctionId, BidStatus.Active))
                .thenReturn(existingHighest);

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(mapper.toEntity(dto)).thenReturn(newBidEntity);
        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);
        when(mapper.toDto(savedBid)).thenReturn(expectedDto);

        GetBidDto result = bidService.placeBid(dto, buyerId);

        assertSame(expectedDto, result);

        // existing highest bid should be marked inactive and saved
        assertEquals(BidStatus.Inactive, existingHighest.getStatus());
        verify(bidRepository).save(existingHighest);

        assertEquals(dto.bidPrice(), auction.getCurrentPrice());
        assertSame(buyer, auction.getHighestBidder());
        verify(auctionRepository).save(auction);
    }

    @Test
    void placeBid_throwsWhenAuctionNotFound() {
        Long auctionId = 1L;
        Long buyerId = 10L;
        CreateBidDto dto = new CreateBidDto(auctionId, BigDecimal.valueOf(150));

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        assertThrows(AuctionNotFoundException.class,
                () -> bidService.placeBid(dto, buyerId));

        verifyNoInteractions(bidRepository, userRepository, mapper, messagingTemplate);
    }

    @Test
    void placeBid_throwsWhenAuctionNotOpen() {
        Long auctionId = 1L;
        Long buyerId = 10L;
        CreateBidDto dto = new CreateBidDto(auctionId, BigDecimal.valueOf(150));

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setStatus(AuctionStatus.Closed);

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        assertThrows(BiddingRuleException.class,
                () -> bidService.placeBid(dto, buyerId));

        verifyNoInteractions(bidRepository, userRepository, mapper, messagingTemplate);
    }

    @Test
    void placeBid_throwsWhenBidNotHigherThanExistingHighestBid() {
        Long auctionId = 1L;
        Long buyerId = 10L;
        CreateBidDto dto = new CreateBidDto(auctionId, BigDecimal.valueOf(150));

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setStatus(AuctionStatus.Open);

        Bid existingHighest = new Bid();
        existingHighest.setBidPrice(BigDecimal.valueOf(200));
        existingHighest.setStatus(BidStatus.Active);

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(bidRepository.findTopByAuction_AuctionIdAndStatusOrderByBidPriceDesc(
                auctionId, BidStatus.Active))
                .thenReturn(existingHighest);

        assertThrows(BiddingRuleException.class,
                () -> bidService.placeBid(dto, buyerId));

        verify(userRepository, never()).findById(anyLong());
        verify(mapper, never()).toEntity(any());
        verify(bidRepository, never()).save(any(Bid.class));
        // Avoid overloaded method ambiguity: just assert no interactions at all
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void placeBid_throwsWhenBidNotHigherThanStartingPriceIfNoExistingBid() {
        Long auctionId = 1L;
        Long buyerId = 10L;
        CreateBidDto dto = new CreateBidDto(auctionId, BigDecimal.valueOf(100)); // = starting price

        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setStatus(AuctionStatus.Open);
        auction.setStartingPrice(BigDecimal.valueOf(100));

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(bidRepository.findTopByAuction_AuctionIdAndStatusOrderByBidPriceDesc(
                auctionId, BidStatus.Active))
                .thenReturn(null);

        assertThrows(BiddingRuleException.class,
                () -> bidService.placeBid(dto, buyerId));

        verify(userRepository, never()).findById(anyLong());
        verify(mapper, never()).toEntity(any());
        verify(bidRepository, never()).save(any(Bid.class));
        verifyNoInteractions(messagingTemplate);
    }

    // ---------- getBidsByUserId tests ----------

    @Test
    void getBidsByUserId_returnsMappedDtos() {
        Long userId = 5L;

        Bid bid1 = new Bid();
        Bid bid2 = new Bid();
        List<Bid> bids = List.of(bid1, bid2);

        GetBidDto dto1 = new GetBidDto(
                1L, 100L, userId, "user1",
                BigDecimal.TEN, BidStatus.Active, LocalDateTime.now()
        );
        GetBidDto dto2 = new GetBidDto(
                2L, 100L, userId, "user1",
                BigDecimal.ONE, BidStatus.Inactive, LocalDateTime.now()
        );
        List<GetBidDto> mappedDtos = List.of(dto1, dto2);

        when(bidRepository.findByBuyer_UserIdOrderByCreatedAtDesc(userId))
                .thenReturn(bids);
        when(mapper.toDto(bids)).thenReturn(mappedDtos);

        List<GetBidDto> result = bidService.getBidsByUserId(userId);

        assertSame(mappedDtos, result);
        verify(bidRepository).findByBuyer_UserIdOrderByCreatedAtDesc(userId);
        verify(mapper).toDto(bids);
    }

    // ---------- getBidsByAuctionId tests ----------

    @Test
    void getBidsByAuctionId_returnsMappedDtos() {
        Long auctionId = 3L;

        Bid bid1 = new Bid();
        Bid bid2 = new Bid();
        List<Bid> bids = List.of(bid1, bid2);

        GetBidDto dto1 = new GetBidDto(
                10L, auctionId, 100L, "u1",
                BigDecimal.TEN, BidStatus.Active, LocalDateTime.now()
        );
        GetBidDto dto2 = new GetBidDto(
                11L, auctionId, 101L, "u2",
                BigDecimal.ONE, BidStatus.Inactive, LocalDateTime.now()
        );
        List<GetBidDto> mappedDtos = List.of(dto1, dto2);

        when(bidRepository.findByAuction_AuctionIdOrderByBidPriceDesc(auctionId))
                .thenReturn(bids);
        when(mapper.toDto(bids)).thenReturn(mappedDtos);

        List<GetBidDto> result = bidService.getBidsByAuctionId(auctionId);

        assertSame(mappedDtos, result);
        verify(bidRepository).findByAuction_AuctionIdOrderByBidPriceDesc(auctionId);
        verify(mapper).toDto(bids);
    }
}
