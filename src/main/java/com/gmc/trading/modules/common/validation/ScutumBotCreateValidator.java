package com.gmc.trading.modules.common.validation;

import com.gmc.common.service.cache.CacheMessageSource;
import com.gmc.common.validator.AbstractValidation;
import com.gmc.trading.modules.coin.application.CoinService;
import com.gmc.trading.modules.coin.application.dto.CoinResponse;
import com.gmc.trading.modules.common.dto.ScutumBotCreate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScutumBotCreateValidator extends AbstractValidation implements ConstraintValidator<ValidScutumBotCreate, ScutumBotCreate> {

  private final CoinService coinService;
  private final CacheMessageSource cacheMessageSource;

  @Override
  public boolean isValid(ScutumBotCreate value, ConstraintValidatorContext context) {
    boolean valid = true;
    try {
      CoinResponse coinResponse = coinService.detailResponse(value.getCoinId());

      if (value.getMinOrderAmount().compareTo(coinResponse.getMinOrderPrice()) < 0) {
        setMessage(context, "minOrderAmount", cacheMessageSource.getMessage("거래소 최소 주문 금액보다 커야 합니다."));
        valid = false;
      }

      if (value.getMaxOrderAmount().compareTo(value.getMinOrderAmount()) < 0) {
        setMessage(context, "maxOrderAmount", cacheMessageSource.getMessage("minOrderAmount 보다 커야 합니다."));
        valid = false;
      }

      if (value.getMaxCycleSecond().compareTo(value.getMinCycleSecond()) < 0) {
        setMessage(context, "maxCycleSecond", cacheMessageSource.getMessage("minCycleSecond 보다 커야 합니다."));
        valid = false;
      }
    } catch (Exception e) {
      setMessage(context, "coinId", cacheMessageSource.getMessage("코인 정보를 찾을 수 없습니다."));
      valid = false;
    }

    return valid;
  }
}