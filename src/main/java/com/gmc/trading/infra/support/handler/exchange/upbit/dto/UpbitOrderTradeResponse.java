package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbitOrderTradeResponse {

  private String market; // 마켓의 유일키
  private String uuid; // 체결의 고유 아이디
  private BigDecimal price; // 체결 가격
  private BigDecimal volume; // 체결 양
  private BigDecimal funds; // 체결된 총 가격
  private String side; // 체결 종류
  private String created_at; // 체결 시각 yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX or yyyy-MM-dd'T'HH:mm:ss.XXX
}