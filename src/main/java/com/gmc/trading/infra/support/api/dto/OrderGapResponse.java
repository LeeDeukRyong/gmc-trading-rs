package com.gmc.trading.infra.support.api.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderGapResponse {

  private Integer count; // 주문목록 넘버 0 = 시장가 주문 이후 순차적으로
  private BigDecimal interval; // 시장가 주문 단가로부터 간격
  private BigDecimal price; // 주문 단가
  private BigDecimal unit; // 주문 수량
  private BigDecimal total_unit; // 시장가 주문으로 부터 누적 주문 수량
  private BigDecimal funds; // 주문 금액
  private BigDecimal total_funds; // 시장가 주문으로 부터 누적 주문 금액
}
