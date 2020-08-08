package com.thoughtworks.rslist.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trade {
    @NotNull  private double amount;
    @NotNull  private int rank;
    @NotNull  private String eventName;
    @NotNull  private String keyWord;
}
