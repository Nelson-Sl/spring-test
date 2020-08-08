package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TradeRepository extends CrudRepository<TradeDto,Integer> {
    List<TradeDto> findAll();
    boolean existsByEventName(String eventName);
    TradeDto findByEventName(String eventName);
    boolean existsByRank(int rank);
    TradeDto findByRank(int rank);
}
