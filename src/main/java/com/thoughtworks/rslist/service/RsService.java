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
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;
  final Tools tools;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository,
                   VoteRepository voteRepository, TradeRepository tradeRepository, Tools tools) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
    this.tools = tools;
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
    if(isEventExistsInOtherRank(trade) || isTradeGotLowerPrice(trade)) {
      throw new RsTradeFailureException("Trade Fail.Your event might exists or your price is not high enough");
    }else if(!eventBuyer.isPresent()) {
      throw new UserNotRegisterException("Please register as a user");
    }
    if(tradeRepository.findByRank(trade.getRank()).isPresent()){
      updateTradeEvent(trade);
    }
      addNewTradeEvent(trade);
    if(!rsEventRepository.findByEventName(trade.getEventName()).isPresent()) {
      RsEventDto newBoughtEvent = RsEventDto.builder().eventName(trade.getEventName())
              .keyword(trade.getKeyWord())
              .user(eventBuyer.get())
              .build();
      rsEventRepository.save(newBoughtEvent);
    }
    tools.renewRsEventRank();
  }

  private boolean isTradeGotLowerPrice(Trade trade) {
    return tradeRepository.findByRank(trade.getRank()).isPresent()
            && (trade.getAmount() < tradeRepository.findByRank(trade.getRank()).get().getAmount());
  }

  private boolean isEventExistsInOtherRank(Trade trade) {
    return tradeRepository.findByEventName(trade.getEventName()).isPresent()
            && (tradeRepository.findByEventName(trade.getEventName()).get().getRank() != trade.getRank());
  }

  private void updateTradeEvent(Trade trade) {
    TradeDto updateTrade = tradeRepository.findByRank(trade.getRank()).get();
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
}
