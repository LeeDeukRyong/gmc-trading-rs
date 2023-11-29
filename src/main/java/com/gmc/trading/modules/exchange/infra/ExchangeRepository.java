package com.gmc.trading.modules.exchange.infra;

import com.gmc.trading.modules.exchange.domain.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

}
