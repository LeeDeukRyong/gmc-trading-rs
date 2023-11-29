package com.gmc.trading.modules.api_key.application.dto;

import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.dto.common.BaseResponse;
import com.gmc.trading.modules.bot.application.dto.BotSearchResponse;
import java.time.LocalDateTime;
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
public class ApiKeySearchResponse extends BaseResponse {

  private Long id;
  private String keyName;
  private String accessKey;
  private String secretKey;
  private String passPhrase;
  private LocalDateTime expireDt;

  private Long userId;
  private String email;
  private String userNm;
  private String operatorId;
  private String centerId;

  private Long exchangeId;
  private ExchangeCode exchangeCode;

  private List<BotSearchResponse> bots;
}
