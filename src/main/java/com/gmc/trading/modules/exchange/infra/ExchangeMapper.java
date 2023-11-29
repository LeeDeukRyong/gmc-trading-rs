package com.gmc.trading.modules.exchange.infra;


import com.gmc.trading.modules.exchange.application.dto.ExchangeSearchRequest;
import com.gmc.trading.modules.exchange.application.dto.ExchangeSearchResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExchangeMapper {

  List<ExchangeSearchResponse> searchExchangeList(ExchangeSearchRequest request);
}
