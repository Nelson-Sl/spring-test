package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<UserDto, Integer> {
    List<UserDto> findAll();
}
