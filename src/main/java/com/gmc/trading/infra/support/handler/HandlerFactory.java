package com.gmc.trading.infra.support.handler;

import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.code.trading.ModeCode;
import com.gmc.common.exception.BizException;
import com.gmc.common.utils.ApplicationContextUtils;
import com.gmc.trading.infra.support.handler.exchange.ExchangeHandler;
import com.gmc.trading.infra.support.handler.mode.ModeHandler;

public class HandlerFactory {

  private HandlerFactory() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 거래소 코드에 해당하는 {@link ExchangeHandler} 리턴
   */
  public static ExchangeHandler getExchangeHandler(ExchangeCode code) {
    return getHandler(code.getCode(), ExchangeHandler.class);
  }

  /**
   * 트레이딩 모드코드에 해당하는 {@link ModeHandler} 리턴
   */
  public static ModeHandler getModeHandler(ModeCode code) {
    return getHandler(code.name(), ModeHandler.class);
  }

  private static <T> T getHandler(String beanPrefix, Class<T> requiredType) {
    String name = beanPrefix + requiredType.getSimpleName();

    return ApplicationContextUtils.getBean(name, requiredType).orElseThrow(() -> new BizException(name + " 핸들러를 찾을 수 없습니다."));
  }
}
