package com.gmc.trading.modules.coin.infra;


import com.gmc.trading.modules.coin.application.dto.CoinSearchRequest;
import com.gmc.trading.modules.coin.application.dto.CoinSearchResponse;
import com.gmc.trading.modules.coin.application.dto.OperatorBotCoinSearchRequest;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoinMapper {

  List<CoinSearchResponse> searchCoinList(CoinSearchRequest request);
  List<CoinSearchResponse> searchOperatorBotCoinList(OperatorBotCoinSearchRequest request);
}
