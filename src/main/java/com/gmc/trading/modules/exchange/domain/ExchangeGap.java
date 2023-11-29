package com.gmc.trading.modules.exchange.domain;

import com.gmc.common.embedded.CreatedAndUpdated;
import com.gmc.trading.modules.exchange.application.dto.ExchangeGapResponse;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(ExchangeGapPK.class)
public class ExchangeGap {

  @Id
  @ManyToOne
  private Exchange exchange;

  @Id
  private Integer addOrderCnt;

  @Column(nullable = false)
  private BigDecimal gap;

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
  public ExchangeGapResponse ofResponse() {
    ExchangeGapResponse response = ExchangeGapResponse.builder().addOrderCnt(addOrderCnt).gap(gap).build();
    response.setCreatedAndUpdated(createdAndUpdated);

    return response;
  }

  /* =================================================================
   * Business logic
   ================================================================= */
  public void update(@NonNull BigDecimal gap) {
    this.gap = gap;
  }
}