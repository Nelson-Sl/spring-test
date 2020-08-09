package com.thoughtworks.rslist.Component;

import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Tools {
    private final RsEventRepository rsEventRepository;
    private final TradeRepository tradeRepository;

    public Tools(RsEventRepository rsEventRepository, TradeRepository tradeRepository) {
        this.rsEventRepository = rsEventRepository;
        this.tradeRepository = tradeRepository;
    }

    public void renewRsEventRank() {
        List<RsEventDto> eventList = rsEventRepository.findAll();
        if(eventList.size() > 1) {
            eventList.sort((o1, o2) -> o2.getVoteNum() - o1.getVoteNum());
            rsEventRepository.deleteAll();
            int rank = 1;
            for(RsEventDto event: eventList) {
                if(tradeRepository.findByEventName(event.getEventName()).isPresent()) {
                    event.setRank(tradeRepository.findByEventName(event.getEventName()).get().getRank());
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
        if(tradeRepository.findByRank(rank).isPresent()){
            decideRank(++rank);
        }
        return rank;
    }
}
