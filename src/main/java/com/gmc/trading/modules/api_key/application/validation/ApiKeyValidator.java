package com.gmc.trading.modules.api_key.application.validation;

import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.service.cache.CacheMessageSource;
import com.gmc.common.validator.AbstractValidation;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyCreate;
import com.gmc.trading.modules.exchange.application.ExchangeService;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class ApiKeyValidator extends AbstractValidation implements ConstraintValidator<ValidApiKey, ApiKeyCreate> {

  private final ExchangeService exchangeService;
  private final CacheMessageSource cacheMessageSource;
  private ValidApiKey validApiKey;

  @Override
  public void initialize(ValidApiKey validApiKey) {
    this.validApiKey = validApiKey;
  }

  @Override
  public boolean isValid(ApiKeyCreate value, ConstraintValidatorContext context) {
    boolean valid = true;

    try {
      ExchangeCode exchangeCode = exchangeService.detailResponse(value.getExchangeId()).getCode();

      if (ExchangeCode.OKX.equals(exchangeCode) && StringUtils.isBlank(value.getPassPhrase())) {
        setMessage(context, "passPhrase", validApiKey.message());
        valid = false;
      }
    } catch (Exception e) {
      setMessage(context, "exchangeId", cacheMessageSource.getMessage("거래소 정보를 찾을 수 없습니다."));
      valid = false;
    }

    return valid;
  }
}
