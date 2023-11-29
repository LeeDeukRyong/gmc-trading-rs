package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import com.gmc.common.code.trading.OrderType;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderBookResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitOrderBookResponse {

  private String market;
  private List<OrderBookUnit> orderbook_units;

  public List<ExchangeOrderBookResponse> toList() {
    return orderbook_units.stream().flatMap(OrderBookUnit::toStream).toList();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderBookUnit {

    private BigDecimal ask_price;
    private BigDecimal ask_size;
    private BigDecimal bid_price;
    private BigDecimal bid_size;

    public Stream<ExchangeOrderBookResponse> toStream() {
      List<ExchangeOrderBookResponse> orderBooks = new ArrayList<>();

      if (ask_price != null && ask_size != null) {
        orderBooks.add(ExchangeOrderBookResponse.builder().orderType(OrderType.SELL).price(ask_price).qty(ask_size).build());
      }

      if (bid_price != null && bid_size != null) {
        orderBooks.add(ExchangeOrderBookResponse.builder().orderType(OrderType.BUY).price(bid_price).qty(bid_size).build());
      }

      return orderBooks.stream();
    }
  }
}
