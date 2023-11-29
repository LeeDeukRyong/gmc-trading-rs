package com.gmc.trading.modules.exchange.application;

import static java.util.stream.Collectors.toSet;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.code.common.MessageCode;
import com.gmc.common.exception.BizException;
import com.gmc.common.utils.SecurityUtils;
import com.gmc.trading.modules.exchange.application.dto.ExchangeGapUpdate;
import com.gmc.trading.modules.exchange.application.dto.ExchangeOperatorDto;
import com.gmc.trading.modules.exchange.application.dto.ExchangeResponse;
import com.gmc.trading.modules.exchange.application.dto.ExchangeSearchRequest;
import com.gmc.trading.modules.exchange.application.dto.ExchangeSearchResponse;
import com.gmc.trading.modules.exchange.application.dto.ExchangeUpdate;
import com.gmc.trading.modules.exchange.domain.Exchange;
import com.gmc.trading.modules.exchange.infra.ExchangeMapper;
import com.gmc.trading.modules.exchange.infra.ExchangeRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ExchangeService {

  private final ExchangeRepository repository;
  private final ExchangeMapper mapper;

  @Transactional(readOnly = true)
  public List<ExchangeSearchResponse> list(ExchangeSearchRequest request) {
    return mapper.searchExchangeList(request);
  }

  public Exchange detail(long id) {
    Exchange exchange = repository.findById(id).orElseThrow(() -> new BizException(MessageCode.NOT_EXIST_DATA));

    if (!SecurityUtils.hasRoleAdmin() && IsYn.N.equals(exchange.getUseYn())) {
      throw new BizException("사용할 수 없는 거래소 입니다.");
    }

    return exchange;
  }

  @Transactional(readOnly = true)
  public ExchangeResponse detailResponse(long id) {
    return detail(id).ofDetailResponse();
  }

  @Transactional
  public ExchangeResponse update(long id, ExchangeUpdate update) {
    Exchange exchange = detail(id);
    exchange.update(update);

    return exchange.ofDetailResponse();
  }

  @Transactional
  public ExchangeResponse updateGap(long id, @NonNull ExchangeGapUpdate update) {
    Exchange exchange = detail(id);
    exchange.updateGap(update);

    return exchange.ofDetailResponse();
  }

  @Transactional
  public ExchangeResponse createOperator(long id, @NonNull ExchangeOperatorDto exchangeOperatorDto) {
    Exchange exchange = detail(id);
    exchange.addOperators(exchangeOperatorDto.getOperatorIds());

    return exchange.ofDetailResponse();
  }

  @Transactional
  public ExchangeResponse deleteOperator(long id, @NonNull ExchangeOperatorDto exchangeOperatorDto) {
    Exchange exchange = detail(id);
    exchange.removeOperators(exchangeOperatorDto.getOperatorIds());

    return exchange.ofDetailResponse();
  }

  @Transactional(readOnly = true)
  public Set<Exchange> findAll() {
    return repository.findAll().stream().filter(Exchange::isUsable).collect(toSet());
  }
}
