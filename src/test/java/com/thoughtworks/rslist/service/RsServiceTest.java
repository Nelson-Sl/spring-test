package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.Component.Tools;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.RsTradeFailureException;
import com.thoughtworks.rslist.exception.UserNotRegisterException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;
  @Mock Tools tools;
  LocalDateTime localDateTime;
  Vote vote;
  Trade trade;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository,tradeRepository,tools);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
    trade = trade.builder().eventName("event2").keyWord("无分类").amount(100).rank(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }

  /*
  Trade Possibilities:
    1. Successfully add the trade
    2. Throw Exception when user not exist
    3. Throw Exception when user vote second time with a lower price
    4. Throw Exception when user vote second time with the same event in different rank
   */
  @Test
  void shouldTradeSuccessfully() throws RsTradeFailureException, UserNotRegisterException {
    UserDto user = UserDto.builder().userName("Nelson").age(23).gender("Male")
            .email("nelson@a.com").phone("11234567890").voteNum(10).build();
    int userId = user.getId();
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
    when(rsEventRepository.findByEventName(anyString())).thenReturn(Optional.empty());
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.empty());
    when(tradeRepository.findByEventName(anyString())).thenReturn(Optional.empty());
    rsService.buy(trade,userId);
    verify(tradeRepository).save(
            TradeDto.builder().eventName("event2").keyWord("无分类")
            .amount(100).rank(1).build());
    verify(rsEventRepository).save(
            RsEventDto.builder().eventName("event2").keyword("无分类")
            .user(user).build());
    verify(tools).renewRsEventRank();
  }

  @Test
  void shouldNotTradeEventsWhenUsersAreNotExists() {
    UserDto user = UserDto.builder().id(1).userName("Nelson").age(23).gender("Male")
            .email("nelson@a.com").phone("11234567890").voteNum(10).build();
    int userId = user.getId();
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(rsEventRepository.findByEventName(anyString())).thenReturn(Optional.empty());
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.empty());
    when(tradeRepository.findByEventName(anyString())).thenReturn(Optional.empty());
    assertThrows(UserNotRegisterException.class,
            () -> {
              rsService.buy(trade,userId);
            });
  }

  @Test
  void shouldNotTradeEventsWhenTradePriceIsLower() {
    UserDto user = UserDto.builder().id(1).userName("Nelson").age(23).gender("Male")
            .email("nelson@a.com").phone("11234567890").voteNum(10).build();
    TradeDto currentTrade = TradeDto.builder().amount(200).eventName("event1").keyWord("无分类")
            .rank(1).build();
    int userId = user.getId();
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
    when(rsEventRepository.findByEventName(anyString())).thenReturn(Optional.empty());
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.of(currentTrade));
    when(tradeRepository.findByEventName(anyString())).thenReturn(Optional.empty());
    assertThrows(RsTradeFailureException.class,
            () -> {
              rsService.buy(trade,userId);
            });
  }

  @Test
  void shouldNotTradeEventsWhenEventExistsInOtherRank() {
    UserDto user = UserDto.builder().id(1).userName("Nelson").age(23).gender("Male")
            .email("nelson@a.com").phone("11234567890").voteNum(10).build();
    TradeDto currentTrade = TradeDto.builder().amount(500).eventName("event2").keyWord("无分类")
            .rank(2).build();
    int userId = user.getId();
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
    when(rsEventRepository.findByEventName(anyString())).thenReturn(Optional.empty());
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.empty());
    when(tradeRepository.findByEventName(anyString())).thenReturn(Optional.of(currentTrade));
    assertThrows(RsTradeFailureException.class,
            () -> {
              rsService.buy(trade,userId);
            });
  }
}
