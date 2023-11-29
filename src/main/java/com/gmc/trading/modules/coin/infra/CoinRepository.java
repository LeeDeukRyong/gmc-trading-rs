package com.gmc.trading.modules.coin.infra;

import com.gmc.trading.modules.coin.domain.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> {

  boolean existsByCode(String code);
}
