package com.gmc.trading.modules.coin.application;

import static java.util.stream.Collectors.toSet;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.code.common.MessageCode;
import com.gmc.common.exception.BizException;
import com.gmc.common.utils.MessageUtils;
import com.gmc.common.utils.SecurityUtils;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinResponse;
import com.gmc.trading.modules.coin.application.dto.CoinResponse;
import com.gmc.trading.modules.coin.application.dto.CoinSearchRequest;
import com.gmc.trading.modules.coin.application.dto.CoinSearchResponse;
import com.gmc.trading.modules.coin.application.dto.CoinUpdate;
import com.gmc.trading.modules.coin.application.dto.OperatorBotCoinSearchRequest;
import com.gmc.trading.modules.coin.domain.Coin;
import com.gmc.trading.modules.coin.infra.CoinMapper;
import com.gmc.trading.modules.coin.infra.CoinRepository;
import com.gmc.trading.modules.exchange.application.ExchangeService;
import com.gmc.trading.modules.exchange.domain.Exchange;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CoinService {

  private final CoinRepository repository;
  private final CoinMapper mapper;
  private final ExchangeService exchangeService;

  @Transactional(readOnly = true)
  public List<CoinSearchResponse> list(CoinSearchRequest request) {
    return mapper.searchCoinList(request);
  }

  public Coin detail(long id) {
    Coin coin = repository.findById(id).orElseThrow(() -> new BizException(MessageCode.NOT_EXIST_DATA));

    if (!SecurityUtils.hasRoleAdmin() && IsYn.N.equals(coin.getUseYn())) {
      throw new BizException("사용할 수 없는 코인 입니다.");
    }

    return coin;
  }

  @Transactional(readOnly = true)
  public CoinResponse detailResponse(long id) {
    return detail(id).ofDetailResponse();
  }

  @Transactional
  public CoinResponse update(long id, CoinUpdate update) {
    Coin coin = detail(id);
    coin.update(update);

    return coin.ofDetailResponse();
  }

  @Transactional(readOnly = true)
  public List<CoinSearchResponse> getOperatorBotCoins(OperatorBotCoinSearchRequest request) {
    return mapper.searchOperatorBotCoinList(request);
  }

  @Transactional
  public void coinSynchronized() {
    exchangeCoinSynchronized();
  }

  @Async
  @Transactional
  public void asyncCoinSynchronized() {
    exchangeCoinSynchronized();
  }

  /**
   * 거래소 코인 동기화 -> 거래소에서 사용하는 코인 목록 중 시스템에 추가 되지 않은 항목 추가
   */
  public void exchangeCoinSynchronized() {
    Set<Exchange> exchanges = exchangeService.findAll();

    exchanges.forEach(exchange -> {
      try {
        exchange.getMarkets().forEach(market -> {
          Set<ExchangeCoinResponse> exchangeCoins = exchange.getExchangeHandler().getCoins(market.getMarketType(), market.getCode());

          Set<Coin> coins = exchangeCoins.stream().filter(f -> f.getMarketCode().equalsIgnoreCase(market.getCode()))
              .map(ExchangeCoinResponse::ofEntity)
              .collect(toSet());
          if (CollectionUtils.isNotEmpty(coins)) {
            market.addCoins(coins);
          }

          // 거래소에서 지원하지 않는 코인 useYn N 처리
          market.getCoins().forEach(coin -> {
            if (exchangeCoins.stream().noneMatch(ec -> ec.getCoinCode().equals(coin.getCode()))) {
              coin.delete();
            }
          });
        });
      } catch (Exception e) {
        MessageUtils.sendErrorMessageToSlack(e);
      }
    });
  }
}
