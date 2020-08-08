package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Id
    @GeneratedValue
    private int id;
    @Column(name = "name")
    private String userName;
    private String gender;
    private int age;
    private String email;
    private String phone;
    @Builder.Default
    private int voteNum =10;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
    private List<RsEventDto> rsEventDtos;
}
