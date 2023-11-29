package com.gmc.trading.modules.account.application.dto;

import com.gmc.common.dto.common.BaseResponse;
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
public class AccountResponse extends BaseResponse {

  private Long userId;
  private String email;
  private String userNm;
  private Long operatorId;
  private Long centerId;
  private Integer totalBotCnt; // 거래소 상관 없이 삭제되지 않은 전체 봇 수량
  private Integer startBotCnt; // 거래소 상관 없이 동작중인 전체 봇 수량
  private BigDecimal totalFunds; // 거래소 상관 없이 전체 투자 금액

  private List<ExchangeBotResponse> exchangeBots; // 거래소 봇 목록
}
