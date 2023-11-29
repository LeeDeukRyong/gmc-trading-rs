package com.gmc.trading.modules.volume_bot.infra;

import com.gmc.trading.modules.volume_bot.domain.VolumeBotOrder;
import java.util.Set;
import org.hibernate.annotations.NamedNativeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VolumeBotOrderRepository extends JpaRepository<VolumeBotOrder, Long> {

  @Query(value = "SELECT * FROM tt_volume_bot_order WHERE volume_bot_id = :volumeBotId AND buy_order_qty=14.1", nativeQuery = true)
  Set<VolumeBotOrder> getNotCompleteOrders(@Param(value = "volumeBotId") long volumeBotId);
}