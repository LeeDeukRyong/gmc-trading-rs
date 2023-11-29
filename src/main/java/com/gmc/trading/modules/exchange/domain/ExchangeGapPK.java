package com.gmc.trading.modules.exchange.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class ExchangeGapPK implements Serializable {

  @Serial
  private static final long serialVersionUID = 2441869558616552154L;
  @ManyToOne
  @JoinColumn(name = "exchangeId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk01_tt_exchange_gap"), nullable = false)
  private Exchange exchange;

  @Column(nullable = false)
  private Integer addOrderCnt;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExchangeGapPK that = (ExchangeGapPK) o;
    return Objects.equals(exchange.getId(), that.exchange.getId()) && Objects.equals(addOrderCnt, that.addOrderCnt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exchange.getId(), addOrderCnt);
  }
}