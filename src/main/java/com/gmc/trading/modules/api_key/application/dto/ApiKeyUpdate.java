package com.gmc.trading.modules.api_key.application.dto;

import com.gmc.common.utils.PasswordEncoderUtils;
import javax.validation.constraints.NotBlank;
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
public class ApiKeyUpdate {

  @Size(max = 100)
  private String keyName;

  @NotBlank
  @Size(max = 100)
  private String accessKey;

  @NotBlank
  @Size(max = 100)
  private String secretKey; // 암호화 해서 저장 -> 100자리 이상이면 db 저장 안됨(데이터베이스 필드 사이즈 172)

  @Size(max = 100)
  private String passPhrase; // 암호화 해서 저장 -> 100자리 이상이면 db 저장 안됨(데이터베이스 필드 사이즈 172)

  public String getSecretKey() {
    return PasswordEncoderUtils.encrypt(secretKey, accessKey);
  }

  public String getPassPhrase() {
    return StringUtils.isNotBlank(passPhrase) ? PasswordEncoderUtils.encrypt(passPhrase, accessKey) : passPhrase;
  }
}
