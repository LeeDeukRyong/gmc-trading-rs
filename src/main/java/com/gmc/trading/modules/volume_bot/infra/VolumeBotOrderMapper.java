package com.gmc.trading.modules.volume_bot.infra;


import com.gmc.trading.modules.common.dto.ScutumBotOrderStatistics;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotOrderSearchRequest;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotOrderSearchResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VolumeBotOrderMapper {

  List<VolumeBotOrderSearchResponse> searchVolumeBotOrderList(VolumeBotOrderSearchRequest request);

  ScutumBotOrderStatistics getOrderStatistics(@Param("botId") long botId, @Param("zoneId") String zoneId);
}
