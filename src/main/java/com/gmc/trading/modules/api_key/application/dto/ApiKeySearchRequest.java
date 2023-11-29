package com.gmc.trading.modules.api_key.application.dto;

import com.gmc.common.dto.common.SearchRequestDto;
import java.time.LocalDateTime;
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
public class ApiKeySearchRequest extends SearchRequestDto {

  private String email;
  private String userNm;
  private String keyName;
  private String accessKey;
  private LocalDateTime expireDt;

  private Long exchangeId;
}
