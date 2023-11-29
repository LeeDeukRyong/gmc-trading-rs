package com.gmc.trading.modules.volume_bot.domain;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.code.mms.MessageType;
import com.gmc.common.embedded.CreatedAndUpdated;
import com.gmc.common.exception.BizException;
import com.gmc.common.utils.ApplicationContextUtils;
import com.gmc.common.utils.CustomWebUtils;
import com.gmc.common.utils.MessageUtils;
import com.gmc.trading.infra.support.handler.exchange.ExchangeHandler;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeApiKey;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderBookResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeTradeResponse;
import com.gmc.trading.modules.account.domain.Account;
import com.gmc.trading.modules.api_key.domain.ApiKey;
import com.gmc.trading.modules.coin.domain.Coin;
import com.gmc.trading.modules.exchange.domain.Exchange;
import com.gmc.trading.modules.market.domain.Market;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotResponse;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotUpdate;
import com.gmc.trading.modules.volume_bot.infra.VolumeBotOrderMapper;
import com.gmc.trading.modules.volume_bot.infra.VolumeBotOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class VolumeBot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "userId", referencedColumnName = "userId", foreignKey = @ForeignKey(name = "fk01_tt_volume_bot"), nullable = false)
  private Account account;

  @ManyToOne
  @JoinColumn(name = "coinId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk02_tt_volume_bot"), nullable = false)
  private Coin coin;

  @ManyToOne
  @JoinColumn(name = "apiKeyId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk03_tt_volume_bot"))
  private ApiKey apiKey;

  @Column(nullable = false)
  private Integer minCycleSecond;

  @Column(nullable = false)
  private Integer maxCycleSecond;

  @Column(nullable = false)
  private BigDecimal minOrderAmount;

  @Column(nullable = false)
  private BigDecimal maxOrderAmount;

  @Builder.Default
  @Column(length = 1, nullable = false)
  @Enumerated(EnumType.STRING)
  private IsYn workYn = IsYn.N;

  @Builder.Default
  @Column(length = 1, nullable = false)
  @Enumerated(EnumType.STRING)
  private IsYn delYn = IsYn.N;

  private String remark;

  /* =================================================================
   * Default columns
   ================================================================= */
  @Embedded
  @Builder.Default
  private final CreatedAndUpdated createdAndUpdated = new CreatedAndUpdated();

  /* =================================================================
   * Domain mapping
   ================================================================= */

  /* =================================================================
   * Relation method
   ================================================================= */
  public void setAccount(@NonNull Account account) {
    this.account = account;
  }

  public void setCoin(@NonNull Coin coin) {
    coin.validCoin();
    this.coin = coin;
  }

  public void setApiKey(long apiKeyId) {
    apiKey = account.getApiKey(apiKeyId);
    apiKey.checkCreatableBot();
  }

  public void removeApiKey() {
    if (IsYn.Y.equals(workYn)) {
      throw new BizException("거래량 봇이 api key를 사용중 입니다.");
    }

    apiKey = null;
  }

  public boolean isDeleted() {
    return IsYn.Y.equals(delYn);
  }

  public boolean isNotDeleted() {
    return !isDeleted();
  }

  public Exchange getExchange() {
    return coin.getExchange();
  }

  public ExchangeHandler getExchangeHandler() {
    return getExchange().getExchangeHandler();
  }

  public ExchangeApiKey getExchangeApiKey() {
    return apiKey.getExchangeApiKey();
  }

  public Market getMarket() {
    return coin.getMarket();
  }

  private void addVolumeBotOrder(@NonNull VolumeBotOrder volumeBotOrder) {
    volumeBotOrder.setVolumeBot(this);
  }

  public VolumeBotResponse ofResponse() {
    VolumeBotResponse response = VolumeBotResponse.builder().id(id).minCycleSecond(minCycleSecond).maxCycleSecond(maxCycleSecond)
        .minOrderAmount(minOrderAmount).maxOrderAmount(maxOrderAmount).workYn(workYn).delYn(delYn).remark(remark).build();
    response.setCreatedAndUpdated(createdAndUpdated);

    return response;
  }

  public VolumeBotResponse ofDetailResponse() {
    VolumeBotResponse response = ofResponse();
    ApplicationContextUtils.getBean("volumeBotOrderMapper", VolumeBotOrderMapper.class)
        .ifPresent(mapper -> response.setStatistics(mapper.getOrderStatistics(id, CustomWebUtils.getClientTimezone().getID())));

    response.setAccount(account.ofWitOutTradingBotResponse());
    response.setExchange(coin.getExchange().ofResponse());
    response.setMarket(coin.getMarket().ofResponse());
    response.setCoin(coin.ofResponse());
    response.setApiKey(apiKey.ofResponse());

    return response;
  }

  /* =================================================================
   * Business logic
   ================================================================= */
  public void create() {
    if (getMarket().isSwap()) {
      throw new BizException("선물 마켓에서는 거래량 봇을 생성할 수 없습니다.");
    }

    checkMinOrderAmount();

    checkBot();
  }

  public void update(@NonNull VolumeBotUpdate update) {
    if (update.getMinCycleSecond() != null) {
      minCycleSecond = update.getMinCycleSecond();
    }

    if (update.getMaxCycleSecond() != null) {
      maxCycleSecond = update.getMaxCycleSecond();
    }

    if (update.getMinOrderAmount() != null) {
      minOrderAmount = update.getMinOrderAmount();
      checkMinOrderAmount();
    }

    if (update.getMaxOrderAmount() != null) {
      maxOrderAmount = update.getMaxOrderAmount();
      checkMinOrderAmount();
    }

    if (update.getWorkYn() != null && !update.getWorkYn().equals(workYn)) {
      if (IsYn.Y.equals(update.getWorkYn())) {
        workYn = update.getWorkYn();
        remark = null;
      } else {
        stop("봇 종료");
      }
    }
  }

  public void delete() {
    stop("봇 삭제");
    delYn = IsYn.Y;
  }

  public void monitoring() {
    if (validBot()) {
      VolumeBotOrder volumeBotOrder = VolumeBotOrder.builder().build();
      addVolumeBotOrder(volumeBotOrder);

      try {
        // 미체결 주문이 있으면 취소
        cancel();

        BigDecimal exchangeCurrency = apiKey.getExchangeCurrency();
        BigDecimal exchangeCoinQty = apiKey.getExchangeCoinBalance(coin).getBalance();

        if (exchangeCurrency.compareTo(minOrderAmount) < 0) {
          stop("거래소에 주문 가능 금액이 부족합니다.");
        } else {
          // 거래소에서 사용가능한 코인수량 체크
          // 최소 주문 수량 = 최소 주문금액 / 코인의 현재가
          if (exchangeCoinQty.compareTo(minOrderAmount.divide(coin.getCurrentPrice(), coin.getLotSz().scale(), RoundingMode.DOWN)) < 0) {
            stop("거래소 사용 가능 코인 수량이 부족합니다.");
          } else {
            // 오더북 조회
            List<ExchangeOrderBookResponse> orderBooks = getExchangeHandler().getOrderBook(coin);

            // 매수호가중 제일 비싼 가격
            Optional<BigDecimal> buyPrice = orderBooks.stream().filter(ExchangeOrderBookResponse::isBuy).map(ExchangeOrderBookResponse::getPrice)
                .max(Comparator.naturalOrder());

            // 매도호가중 제일 싼 가격
            Optional<BigDecimal> sellPrice = orderBooks.stream().filter(ExchangeOrderBookResponse::isSell).map(ExchangeOrderBookResponse::getPrice)
                .min(Comparator.naturalOrder());

            if (buyPrice.isPresent() && sellPrice.isPresent()) {
              // 최근 거래 가격
              ExchangeTradeResponse recentTrade = getExchangeHandler().getRecentTrade(coin);

              // 매수호가, 매도호가 사이 비어있는 호가 목록
              List<BigDecimal> orderPriceList = getOrderPriceList(buyPrice.get(), sellPrice.get(),
                  getExchangeHandler().getQuoteUnit(coin, recentTrade.getPrice()));

              // 매도호가, 매수호가 사이 값이 있으면
              if (CollectionUtils.isNotEmpty(orderPriceList)) {
                // 주문 단가
                BigDecimal orderPrice;

                // 분봉있기
                // 최근 거래 내역 체결 시간이 현재 시간과 다르고 해당 가격이 매도호가, 매수호가 사이 빈 값이면 가져온 거래내역과 동일한 가격에 주문
                if (LocalDateTime.now().getHour() != recentTrade.getTradingDt().getHour() && orderPriceList.contains(recentTrade.getPrice())) {
                  orderPrice = recentTrade.getPrice();
                } else {
                  // 비어 있는 호가 목록 중 최근 거래 내역 체결 가격이 있으면
                  if (orderPriceList.contains(recentTrade.getPrice())) {
                    // 최근 거래 내역의 체결 가격 위 3호가
                    Set<BigDecimal> targetOrderPriceList = orderPriceList.stream().filter(f -> f.compareTo(recentTrade.getPrice()) > -1).sorted()
                        .limit(3)
                        .collect(
                            Collectors.toSet());

                    // 최근 거래 내역의 체결 가격 아래 3호가
                    targetOrderPriceList.addAll(
                        orderPriceList.stream().filter(f -> f.compareTo(recentTrade.getPrice()) < 1).sorted(Comparator.reverseOrder()).limit(3)
                            .collect(
                                Collectors.toSet()));

                    // 최근 거래 내역의 체결 가격 위, 아래 3호가 사이 값에서 랜덤
                    orderPrice = targetOrderPriceList.stream().toList().get(ThreadLocalRandom.current().nextInt(targetOrderPriceList.size()));
                  } else {
                    // 비어 있는 호가 목록 중 랜덤
                    orderPrice = orderPriceList.get(ThreadLocalRandom.current().nextInt(orderPriceList.size()));
                  }
                }

                // 주문 가능 수량
                BigDecimal qty = getOrderQty(orderPrice, exchangeCurrency, exchangeCoinQty);

                // 주문단가가 최근 거래단가보다 낮으면 사고 팔고, 아니면 팔고 사고
                if (orderPrice.compareTo(recentTrade.getPrice()) < 0) {
                  volumeBotOrder.setBuyInfo(getExchangeHandler().limitBuy(getExchangeApiKey(), coin, null, orderPrice, qty));
                  volumeBotOrder.setSellInfo(getExchangeHandler().limitSell(getExchangeApiKey(), coin, null, orderPrice, qty));
                } else {
                  volumeBotOrder.setSellInfo(getExchangeHandler().limitSell(getExchangeApiKey(), coin, null, orderPrice, qty));
                  volumeBotOrder.setBuyInfo(getExchangeHandler().limitBuy(getExchangeApiKey(), coin, null, orderPrice, qty));
                }

                TimeUnit.SECONDS.sleep(1);
                volumeBotOrder.orderCheck();
                volumeBotOrder.orderCancel();

                if (volumeBotOrder.isNotCrossTrading()) {
                  stop("거래소에서 자전 거래 가능 여부를 확인하세요.");
                }
              }
            }
          }
        }
      } catch (InterruptedException ie) {
        log.warn(ie.getMessage(), ie);
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

      if (volumeBotOrder.hasSave()) {
        ApplicationContextUtils.getBean(VolumeBotOrderRepository.class).saveAndFlush(volumeBotOrder);
      }
    }
  }

  private boolean validBot() {
    if (IsYn.Y.equals(workYn)) {
      try {
        checkBot();

        return true;
      } catch (Exception e) {
        stop(e.getMessage());
      }
    }

    return false;
  }

  private void checkBot() {
    if (isDeleted()) {
      throw new BizException("삭제된 봇 입니다.");
    }

    coin.validCoin();
    apiKey.validApiKey();
  }

  private void stop(@NonNull String remark) {
    try {
      cancel();
    } catch (Exception e) {
      MessageUtils.sendMessage(MessageType.ALERT, remark, "미체결 주문은 거래소에서 정리하세요.", account.getUserId());
    }

    this.remark = remark;
    workYn = IsYn.N;

    MessageUtils.sendMessage(MessageType.NOTIFICATION, "봇 종료", remark, account.getUserId());
  }

  private void cancel() {
    ApplicationContextUtils.getBean(VolumeBotOrderRepository.class).getNotCompleteOrders(id).forEach(VolumeBotOrder::orderCancel);
  }

  private void checkMinOrderAmount() {
    if (minOrderAmount.compareTo(coin.getMinOrderPrice()) < 0 || maxOrderAmount.compareTo(coin.getMinOrderPrice()) < 0) {
      throw new BizException("거래소 최소 주문 금액 보다 커야 합니다.");
    }
  }

  /**
   * @param min       최소값
   * @param max       최대값
   * @param quoteUnit 호가 단위
   * @return 최소값 최대값 사이 호가단위 목록
   */
  private List<BigDecimal> getOrderPriceList(@NonNull BigDecimal min, @NonNull BigDecimal max, @NonNull BigDecimal quoteUnit) {
    List<BigDecimal> orderPriceList = new ArrayList<>();
    BigDecimal current = min;

    while (current.compareTo(max.subtract(quoteUnit)) < 0) {
      current = current.add(quoteUnit);
      orderPriceList.add(current);
    }

    return orderPriceList;
  }

  /**
   * @param price 주문 단가
   * @return minOrderAmount ~ maxOrderAmount 사이 랜덤 금액으로 주문할 수 있는 수량
   */
  private BigDecimal getOrderQty(@NonNull BigDecimal price, @NonNull BigDecimal exchangeCurrency, @NonNull BigDecimal exchangeCoinQty) {
    // 주문 최소 금액
    BigDecimal availableMinOrderAmount = minOrderAmount;
    // 주문 최대 금액
    BigDecimal availableMaxOrderAmount = maxOrderAmount;

    // 주문 최대 금액이 거래소 잔액 보다 크면
    if (availableMaxOrderAmount.compareTo(exchangeCurrency) > 0) {
      availableMaxOrderAmount = exchangeCurrency;
    }

    // 주문 최대 금액이 주문 최소 금액 보다 작거나 주문 최소 금액이 거래소 잔액보다 크면
    if (availableMaxOrderAmount.compareTo(availableMinOrderAmount) < 0 || availableMinOrderAmount.compareTo(exchangeCurrency) > 0) {
      availableMinOrderAmount = availableMaxOrderAmount;
    }

    // 주문 최소금액, 최대금액 사이 랜덤 금액으로 계산 한 주문수량
    BigDecimal orderQty = BigDecimal.valueOf(
            availableMinOrderAmount.doubleValue() + (availableMaxOrderAmount.doubleValue() - availableMinOrderAmount.doubleValue()) * Math.random())
        .divide(price, coin.getLotSz().scale(), RoundingMode.DOWN);

    // 거래소 보유 수량보다 주문 수량이 크면
    if (orderQty.compareTo(exchangeCoinQty) > 0) {
      orderQty = exchangeCoinQty;
    }

    return orderQty.setScale(coin.getLotSz().scale(), RoundingMode.DOWN);
  }
}