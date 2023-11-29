package com.gmc.trading.modules.volume_bot.infra;


import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotSearchRequest;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotSearchResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VolumeBotMapper {

  List<VolumeBotSearchResponse> searchVolumeBotList(VolumeBotSearchRequest request);
}
