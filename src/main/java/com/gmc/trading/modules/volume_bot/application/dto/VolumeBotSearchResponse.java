package com.gmc.trading.modules.volume_bot.application.dto;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.code.trading.MarketType;
import com.gmc.common.dto.common.BaseResponse;
import java.math.BigDecimal;
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
public class VolumeBotSearchResponse extends BaseResponse {

  private Long id;
  private Integer minCycleSecond; // 최소 작동 주기(초)
  private Integer maxCycleSecond; // 최대 작동 주기(초)
  private BigDecimal minOrderAmount; // 최소 주문 금액
  private BigDecimal maxOrderAmount; // 최대 주문 금액
  private IsYn workYn; // 작동 여부
  private IsYn delYn; // 삭제 여부
  private String remark;

  private Long userId;
  private String email;
  private String userNm;
  private Long operatorId;
  private Long centerId;

  private Long exchangeId;
  private ExchangeCode exchangeCode;

  private Long marketId;
  private MarketType marketType;
  private String marketCode;

  private Long apiKeyId;
  private String keyName;

  private Long coinId;
  private String coinCode;
}
