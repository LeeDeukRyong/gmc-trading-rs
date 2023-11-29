package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbitWithdrawResponse {

  private String type; // 입출금 종류
  private String uuid; // 출금의 고유 아이디
  private String curency; // 화폐를 의미하는 영문 대문자 코드
  private String txid; // 출금의 트랜잭션 아이디
  private String state; // 출금 상태
  private BigDecimal amount; // 출금 금액/수량
  private BigDecimal fee; // 수수료
  private String transaction_type; // default: 일반출금, internal: 바로출금(업비트 지갑주소로만 가능)
}
