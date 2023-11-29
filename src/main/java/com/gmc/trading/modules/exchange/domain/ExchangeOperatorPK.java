package com.gmc.trading.modules.exchange.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class ExchangeOperatorPK implements Serializable {

  @Serial
  private static final long serialVersionUID = -8150383150490763250L;
  @ManyToOne
  @JoinColumn(name = "exchangeId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk01_tt_exchange_operator"), nullable = false)
  private Exchange exchange;

  @Column(nullable = false)
  private Long operatorId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExchangeOperatorPK that = (ExchangeOperatorPK) o;
    return Objects.equals(exchange.getId(), that.exchange.getId()) && Objects.equals(operatorId, that.operatorId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exchange.getId(), operatorId);
  }
}