package com.thoughtworks.rslist.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository,
                   VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public void buy(Trade trade, int id) throws RsTradeFailureException, UserNotRegisterException {
    Optional<UserDto> eventBuyer = userRepository.findById(id);
    if(isEventExistsInOtherRank(trade) || isTradeGotLowerPrice(trade,id)) {
      throw new RsTradeFailureException("Trade Fail.Your event might exists or your price is not high enough");
    }else if(!eventBuyer.isPresent()) {
      throw new UserNotRegisterException("Please register as a user");
    }
    if(tradeRepository.existsById(id)){
      updateTradeEvent(trade);
    }
      addNewTradeEvent(trade);
    if(!rsEventRepository.existsByEventName(trade.getEventName())) {
      RsEventDto newBoughtEvent = RsEventDto.builder().eventName(trade.getEventName())
              .keyword(trade.getKeyWord())
              .user(eventBuyer.get())
              .build();
      rsEventRepository.save(newBoughtEvent);
    }
    renewRsEventRank();
  }

  private boolean isTradeGotLowerPrice(Trade trade, int id) {
    return tradeRepository.existsByRank(id)
            && (trade.getAmount() < tradeRepository.findByRank(trade.getRank()).getAmount());
  }

  private boolean isEventExistsInOtherRank(Trade trade) {
    return tradeRepository.existsByEventName(trade.getEventName())
            && (tradeRepository.findByEventName(trade.getEventName()).getRank() != trade.getRank());
  }

  private void updateTradeEvent(Trade trade) {
    TradeDto updateTrade = tradeRepository.findByRank(trade.getRank());
    updateTrade.setEventName(trade.getEventName());
    updateTrade.setAmount(trade.getAmount());
    updateTrade.setKeyWord(trade.getKeyWord());
    tradeRepository.save(updateTrade);
  }

  private void addNewTradeEvent(Trade trade) {
    TradeDto newTrade = TradeDto.builder().amount(trade.getAmount())
            .eventName(trade.getEventName())
            .keyWord(trade.getKeyWord())
            .rank(trade.getRank()).build();
    tradeRepository.save(newTrade);
  }

  private void renewRsEventRank() {
    List<RsEventDto> eventList = rsEventRepository.findAll();
    if(eventList.size() > 1) {
      eventList.sort((o1, o2) -> o2.getVoteNum() - o1.getVoteNum());
      rsEventRepository.deleteAll();
      int rank = 1;
      for(RsEventDto event: eventList) {
        if(tradeRepository.existsByEventName(event.getEventName())) {
          event.setRank(tradeRepository.findByEventName(event.getEventName()).getRank());
        }else {
          rank = decideRank(rank);
          event.setRank(rank);
        }
        rsEventRepository.save(event);
        rank++;
      }
    }
  }

  private int decideRank(int rank) {
    if(tradeRepository.existsByRank(rank)){
      decideRank(++rank);
    }
    return rank;
  }
}
