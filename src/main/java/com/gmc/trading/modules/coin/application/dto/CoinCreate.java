package com.gmc.trading.modules.coin.application.dto;

import com.gmc.trading.modules.coin.domain.Coin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinCreate {

  @NotNull
  private Long marketId;

  @NotBlank
  @Size(max = 10)
  private String code;

  @NotBlank
  @Size(max = 30)
  private String coinNm;

  @Size(max = 30)
  private String coinNmEn;

  @Size(max = 255)
  private String remark;

  public Coin ofEntity() {
    return Coin.builder().code(code).coinNm(coinNm).coinNmEn(StringUtils.isBlank(coinNmEn) ? code : coinNmEn).remark(remark).build();
  }
}
