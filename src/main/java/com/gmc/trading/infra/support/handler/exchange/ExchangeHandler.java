package com.gmc.trading.infra.support.handler.exchange;

import com.gmc.common.code.common.CurrencyCode;
import com.gmc.common.code.trading.MarketType;
import com.gmc.common.code.trading.TradingStatus;
import com.gmc.common.exception.BizException;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeApiKey;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinBalanceResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderBookResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeTradeResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeWalletResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeWithdrawRequest;
import com.gmc.trading.modules.bot.domain.Bot;
import com.gmc.trading.modules.coin.domain.Coin;
import com.gmc.trading.modules.mode.domain.Mode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.lang.NonNull;

/**
 * 거래소 인터페이스
 */
public interface ExchangeHandler {

  /**
   * @return 거래소 명
   */
  String getExchangeName();

  /**
   * @param coin 코인
   * @return 거래소 마켓별 코인 ID로 변환 ex) 코인이 BTC인 경우 업비트 -> KRW-BTC, OKX -> BTC-USDT
   */
  String converterMarket(@NonNull Coin coin);

  /**
   * @param coin                 코인
   * @param stopLossTargetAmount 손절 목표 금액
   * @param tradingQty           사이클 체결 수량
   * @return 손절 단가
   */
  BigDecimal getStopLossPrice(@NonNull Coin coin, @NonNull BigDecimal stopLossTargetAmount, @NonNull BigDecimal tradingQty);

  /**
   * @param apiKey api key
   * @param coin   코인
   * @param mode   모드
   * @return 청산 예상가
   */
  BigDecimal getLiquidationPrice(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NonNull Mode mode);

  /**
   * 코인모드 매수 해야될 수량 구하기
   *
   * <pre>
   *   수수료를 매수한 코인에서 가져가는 거래소로 인해 매수 수량을 수수료 포함해서 매수 수량을 구함
   * </pre>
   *
   * @param coin           코인
   * @param sellTradingQty 매도 체결 합계
   * @param buyTradingQty  매수 체결 합계
   * @return 매수 해야될 수량
   */
  BigDecimal getBuyQty(@NonNull Coin coin, @NonNull BigDecimal sellTradingQty, @NonNull BigDecimal buyTradingQty);

  /**
   * @param coin  코인
   * @param price 주문 금액
   * @return 기준 호가단위
   */
  BigDecimal getQuoteUnit(@NonNull Coin coin, @NonNull BigDecimal price);

  /**
   * 코인 현재가 - 레디스에 해당 코인 가격 불러 오지 못하는 경우 사용
   *
   * @param coin 코인
   * @return 코인 현재가
   * @throws BizException 거래소 현재가 조회할 수 없는 경우
   */
  BigDecimal getCoinPrice(@NonNull Coin coin) throws BizException;

  /**
   * @param marketType 마켓 타입
   * @param marketCode 마켓 코드
   * @return 거래소 코인 목록
   * @throws BizException 거래소 코인 목록 조회할 수 없는 경우
   */
  Set<ExchangeCoinResponse> getCoins(@NonNull MarketType marketType, @NonNull String marketCode) throws BizException;

  /**
   * 코인별 거래소 최근 거래 내역
   *
   * @param coin 코인
   * @return 거래소 최근 거래 내역
   * @throws BizException 거래소 최근 거래 내역 조회할 수 없는 경우
   */
  ExchangeTradeResponse getRecentTrade(@NonNull Coin coin) throws BizException;

  /**
   * @param coin 코인
   * @return 거래소 오더북 정보
   * @throws BizException 거래소 오더북 조회할 수 없는 경우
   */
  List<ExchangeOrderBookResponse> getOrderBook(@NonNull Coin coin) throws BizException;

  /**
   * 봇 생성 시 거래소에서 기본적으로 설정되어 있어야 하는 정보 체크
   *
   * @param bot 봇
   * @throws BizException 모드 별 거래소 설정 정보 확인 실패하는 경우
   */
  void checkAccountConfig(@NonNull Bot bot) throws BizException;

  /**
   * 봇 생성시 레저리지 정보 변경
   *
   * @param bot 봇
   * @throws BizException 레버리지 변경 실패하는 경우
   */
  void changeLeverage(@NonNull Bot bot) throws BizException;

  /**
   * @param apiKey     거래소 api key
   * @param marketCode 마켓 코드
   * @param coinCode   코인 코드
   * @return 거래소 마켓의 사용 가능한 코인 잔액
   * @throws BizException 거래소 계좌 정보 조회할 수 없는 경우
   */
  ExchangeCoinBalanceResponse getCoinBalance(@NonNull ExchangeApiKey apiKey, @NonNull String marketCode, @NonNull String coinCode)
      throws BizException;

  /**
   * @param apiKey       거래소 api key
   * @param currencyCode 통화 코드
   * @return 거래소 사용 가능한 통화 잔액
   * @throws BizException 거래소 계좌 정보 조회할 수 없는 경우
   */
  BigDecimal getUsableCurrency(@NonNull ExchangeApiKey apiKey, @NonNull CurrencyCode currencyCode) throws BizException;

  /**
   * @param apiKey 거래소 api key
   * @return 거래소 api key 만료시간 nullable
   * @throws BizException 거래소 api key 확인할 수 없는 경우
   */
  LocalDateTime getApiKeyExpireAt(@NonNull ExchangeApiKey apiKey) throws BizException;

  /**
   * @param apiKey   거래소 api key
   * @param coinCode 코인 코드
   * @return 거래소 코인 지갑 목록
   * @throws BizException 거래소 코인 지갑 확인할 수 없는 경우
   */
  List<ExchangeWalletResponse> getCoinWallets(@NonNull ExchangeApiKey apiKey, @NonNull String coinCode) throws BizException;

  /**
   * 지정가 매수
   *
   * @param apiKey    api key
   * @param coin      코인
   * @param mode      모드
   * @param coinPrice 코인 단가
   * @param qty       수량
   * @return 주문 결과
   * @throws BizException 거래소 주문할 수 없는 경우
   */
  ExchangeOrderResponse limitBuy(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, Mode mode, @NonNull BigDecimal coinPrice,
      @NonNull BigDecimal qty) throws BizException;

  /**
   * 지정가 매도
   *
   * @param apiKey    api key
   * @param coin      코인
   * @param mode      모드
   * @param coinPrice 코인 단가
   * @param qty       수량
   * @return 주문 결과
   * @throws BizException 거래소 주문할 수 없는 경우
   */
  ExchangeOrderResponse limitSell(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, Mode mode, @NonNull BigDecimal coinPrice,
      @NonNull BigDecimal qty) throws BizException;

  /**
   * 시장가 매수
   *
   * @param apiKey     api key
   * @param coin       코인
   * @param mode       모드
   * @param amount     매수 금액
   * @param withOutFee 수수료 제외 여부
   * @return 주문 결과
   * @throws BizException 거래소 주문할 수 없는 경우
   */
  ExchangeOrderResponse marketBuy(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, Mode mode, @NonNull BigDecimal amount, boolean withOutFee)
      throws BizException;

  /**
   * 시장가 매도
   *
   * @param apiKey api key
   * @param coin   코인
   * @param mode   모드
   * @param qty    매도 수량
   * @return 주문 결과
   * @throws BizException 거래소 주문할 수 없는 경우
   */
  ExchangeOrderResponse marketSell(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, Mode mode, @NonNull BigDecimal qty) throws BizException;

  /**
   * 주문 취소
   *
   * @param apiKey  api key
   * @param coin    코인
   * @param orderId 주문 아이디
   * @return 취소 결과
   * @throws BizException 거래소 주문 취소할 수 없는 경우
   */
  ExchangeOrderResponse orderCancel(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NonNull String orderId) throws BizException;

  /**
   * 주문 정보 조회
   *
   * @param apiKey  api key
   * @param coin    코인
   * @param orderId 주문 아이디
   * @return 주문 결과
   * @throws BizException 거래소 주문 정보 확인할 수 없는 경우
   */
  ExchangeOrderResponse getOrder(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NonNull String orderId) throws BizException;

  /**
   * 주문 목록 조회
   *
   * @param apiKey     api key
   * @param marketType 마켓 타입
   * @return 주문 목록
   * @throws BizException 거래소 주문 정보 확인할 수 없는 경우
   */
  List<ExchangeOrderResponse> getOrders(@NonNull ExchangeApiKey apiKey, MarketType marketType, TradingStatus tradingStatus) throws BizException;

  /**
   * 청산시 들어간 주문 정보 조회
   *
   * @param apiKey   api key
   * @param coin     코인
   * @param position 포지션
   * @param qty      수량
   * @return 주문 결과
   * @throws BizException 거래소 주문 정보 확인할 수 없는 경우
   */
  ExchangeOrderResponse getLiquidationOrder(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NonNull String position, @NonNull BigDecimal qty)
      throws BizException;

  /**
   * 코인 출금
   *
   * @param apiKey  거래소 api key
   * @param request 요청정보
   * @return 거래소 출금아이디
   * @throws BizException 출금 실패할 경우
   */
  String withdraw(@NonNull ExchangeApiKey apiKey, @NonNull ExchangeWithdrawRequest request) throws BizException;

  /**
   * 출금 완료 여부
   *
   * @param apiKey             거래소 api key
   * @param exchangeWithdrawId 거래소 출금아이디
   * @return 출금완료 여부
   * @throws BizException 거래소 출금 정보 확인할 수 없는 경우
   */
  boolean isWithdrawComplete(@NonNull ExchangeApiKey apiKey, @NonNull String exchangeWithdrawId) throws BizException;
}
