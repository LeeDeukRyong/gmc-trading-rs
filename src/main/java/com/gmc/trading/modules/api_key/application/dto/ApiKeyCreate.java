package com.gmc.trading.modules.api_key.application.dto;

import com.gmc.common.dto.common.CreateDtoWithUserId;
import com.gmc.common.utils.PasswordEncoderUtils;
import com.gmc.trading.modules.api_key.application.validation.ValidApiKey;
import com.gmc.trading.modules.api_key.domain.ApiKey;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

@ValidApiKey
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ApiKeyCreate extends CreateDtoWithUserId {

  @NotNull
  private Long exchangeId;

  @NotBlank
  @Size(max = 100)
  private String keyName;

  @NotBlank
  @Size(max = 100)
  private String accessKey;

  @NotBlank
  @Size(max = 100)
  private String secretKey; // 암호화 해서 저장 -> 100자리 이상이면 db 저장 안됨(데이터베이스 필드 사이즈 172) 100자리 평문 암호화시 172자리 나옴

  @Size(max = 100)
  private String passPhrase; // 암호화 해서 저장 -> 100자리 이상이면 db 저장 안됨(데이터베이스 필드 사이즈 172)

  public String getSecretKey() {
    return PasswordEncoderUtils.encrypt(secretKey, accessKey);
  }

  public String getPassPhrase() {
    return StringUtils.isNotBlank(passPhrase) ? PasswordEncoderUtils.encrypt(passPhrase, accessKey) : passPhrase;
  }

  public ApiKey ofEntity() {
    return ApiKey.builder().keyName(keyName).accessKey(accessKey).secretKey(getSecretKey()).passPhrase(getPassPhrase()).build();
  }
}
