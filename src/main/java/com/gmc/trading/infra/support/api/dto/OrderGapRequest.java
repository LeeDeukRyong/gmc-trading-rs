package com.gmc.trading.infra.support.api.dto;

import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderGapRequest {

  @NotBlank
  private String exchange; // 거래소명
  @NotBlank
  private String position; // long, shor, pay, coin
  @NotBlank
  private String payment; // 사용통화 USDT, USD, KRW
  @NotNull
  private BigDecimal price; // 코인 현재 가격
  @NotNull
  private BigDecimal amount; // 전체 주문금액
  private String symbol; // 마켓 심볼 ex) BTC-USDT, BTC-USDT-SWAP
  @NotEmpty
  private List<BigDecimal> low_per; // 저점, 고점 간격
  @NotNull
  private BigDecimal minsz; // 거래소 코인별 최소 주문 금액
  private BigDecimal min_amount; // 최소 주문금액
  private Integer max_count; // 주문 횟수
  private BigDecimal lotsz; // okx 선물 주문 자릿수 업비트 사용 안함
  private BigDecimal ticksz; // okx 선물 주문 호가단위 업비트 사용 안함
  private BigDecimal units; // 업비트 코인모드만 사용
  private BigDecimal exit_pnlrate; // ??? 일단 사용 안함
  private BigDecimal pnlrate_interval; // 간격 증가폭 %
  private String interval_type; // 물타기 배율 기준 가중치(weight) or 균일한증가(equal)
  private String priority; // 주문 목록 생성 기준 amount, interval
}
