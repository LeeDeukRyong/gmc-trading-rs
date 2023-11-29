package com.gmc.trading.modules.coin.application.dto;

import com.gmc.common.code.common.IsYn;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinUpdate {

  @Size(max = 30)
  private String coinNm;

  @Size(max = 30)
  private String coinNmEn;

  private IsYn warningYn;

  private IsYn useYn;

  @Size(max = 255)
  private String remark;
}
