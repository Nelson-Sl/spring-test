package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VoteControllerTest {
  @Autowired MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    UserDto userDto;
    RsEventDto rsEventDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder().userName("idolice").age(19).email("a@b.com").gender("female")
                .phone("18888888888").voteNum(10).build();
        userDto = userRepository.save(userDto);
        rsEventDto = RsEventDto.builder().user(userDto).eventName("event name").keyword("keyword").voteNum(0)
                    .build();
        rsEventDto = rsEventRepository.save(rsEventDto);
        VoteDto voteDto = VoteDto.builder().user(userDto).rsEvent(rsEventDto).localDateTime(LocalDateTime.now())
                .num(5).build();
        voteRepository.save(voteDto);
      }

    @AfterEach
    void tearDown() {
        voteRepository.deleteAll();
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
      }

    @Test
    public void shouldGetVoteRecord() throws Exception {
      VoteDto voteDto = VoteDto.builder().user(userDto).rsEvent(rsEventDto).localDateTime(LocalDateTime.now())
              .num(1).build();
      voteRepository.save(voteDto);
      voteDto = VoteDto.builder().user(userDto).rsEvent(rsEventDto).localDateTime(LocalDateTime.now())
              .num(2).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().user(userDto).rsEvent(rsEventDto).localDateTime(LocalDateTime.now())
              .num(3).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().user(userDto).rsEvent(rsEventDto).localDateTime(LocalDateTime.now())
              .num(4).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().user(userDto).rsEvent(rsEventDto).localDateTime(LocalDateTime.now())
              .num(6).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().user(userDto).rsEvent(rsEventDto).localDateTime(LocalDateTime.now())
              .num(7).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().user(userDto).rsEvent(rsEventDto).localDateTime(LocalDateTime.now())
              .num(8).build();
      voteRepository.save(voteDto);



      mockMvc.perform(get("/voteRecord").param("userId",String.valueOf(userDto.getId()))
      .param("rsEventId",String.valueOf(rsEventDto.getId())).param("pageIndex", "1"))
              .andExpect(jsonPath("$", hasSize(5)))
              .andExpect(jsonPath("$[0].userId",is(userDto.getId())))
              .andExpect(jsonPath("$[0].rsEventId",is(rsEventDto.getId())))
              .andExpect(jsonPath("$[0].voteNum",is(5)))
              .andExpect(jsonPath("$[1].voteNum",is(1)))
              .andExpect(jsonPath("$[2].voteNum",is(2)))
              .andExpect(jsonPath("$[3].voteNum",is(3)))
              .andExpect(jsonPath("$[4].voteNum",is(4)));

      mockMvc.perform(get("/voteRecord").param("userId",String.valueOf(userDto.getId()))
              .param("rsEventId",String.valueOf(rsEventDto.getId())).param("pageIndex", "2"))
              .andExpect(jsonPath("$", hasSize(3)))
              .andExpect(jsonPath("$[0].userId",is(userDto.getId())))
              .andExpect(jsonPath("$[0].rsEventId",is(rsEventDto.getId())))
              .andExpect(jsonPath("$[0].voteNum",is(6)))
              .andExpect(jsonPath("$[1].voteNum",is(7)))
              .andExpect(jsonPath("$[2].voteNum",is(8)));


    }
}
