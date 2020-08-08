package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RsEventDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RsEventRepository extends CrudRepository<RsEventDto, Integer> {
  List<RsEventDto> findAll();

  @Transactional
  void deleteAllByUserId(int userId);

  boolean existsByEventName(String eventName);

  @Query(value = "SELECT * FROM rs_event ORDER BY rank ASC",nativeQuery = true)
  List<RsEventDto> findAllOrderByRank();
}
