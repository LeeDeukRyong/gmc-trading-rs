package com.gmc.trading.modules.account.application.dto;

import com.gmc.common.dto.common.BaseResponse;
import com.gmc.trading.modules.bot.application.dto.BotResponse;
import com.gmc.trading.modules.exchange.application.dto.ExchangeResponse;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ExchangeBotResponse extends BaseResponse {
  // 거래소 별 정보

  private Integer totalBotCnt; // 삭제되지 않은 전체 봇 수량
  private Integer startBotCnt; // 동작(투자)중인 전체 봇 수량
  private BigDecimal totalFunds; // 작동중인 봇의 운용금액
  private BigDecimal revenueOfMonth; // 이번달 총 수익금
  private BigDecimal revenueOfToday; // 오늘 총 수익금

  private ExchangeResponse exchange; // 거래소 정보

  private List<BotResponse> bots; // Bot 목록
}
