package com.gmc.trading.modules.api_key.infra;


import com.gmc.trading.modules.api_key.application.dto.ApiKeySearchRequest;
import com.gmc.trading.modules.api_key.application.dto.ApiKeySearchResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiKeyMapper {

  List<ApiKeySearchResponse> searchApiKeyList(ApiKeySearchRequest request);
}
