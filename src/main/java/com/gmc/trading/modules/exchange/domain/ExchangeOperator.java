package com.gmc.trading.modules.exchange.domain;

import com.gmc.common.embedded.Created;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
@IdClass(ExchangeOperatorPK.class)
public class ExchangeOperator {

  @Id
  @ManyToOne
  private Exchange exchange;

  @Id
  private Long operatorId;

  /* =================================================================
   * Default columns
   ================================================================= */
  @Embedded
  @Builder.Default
  private final Created created = new Created();

  /* =================================================================
   * Domain mapping
   ================================================================= */

  /* =================================================================
   * Relation method
   ================================================================= */

  /* =================================================================
   * Business logic
   ================================================================= */
}