package com.gmc.trading.modules.account.application;

import com.gmc.common.code.admin.UserStatus;
import com.gmc.common.dto.admin.user.UserResponse;
import com.gmc.common.exception.BizException;
import com.gmc.common.support.api.AdminApi;
import com.gmc.common.utils.SecurityUtils;
import com.gmc.trading.modules.account.application.dto.AccountResponse;
import com.gmc.trading.modules.account.domain.Account;
import com.gmc.trading.modules.account.infra.AccountRepository;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyResponse;
import com.gmc.trading.modules.coin.application.dto.CoinResponse;
import com.gmc.trading.modules.coin.application.dto.CoinSearchRequest;
import java.util.List;
import java.util.TimeZone;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccountService {

  private final AccountRepository repository;
  private final AdminApi adminApi;

  @Lazy
  @Resource
  private AccountService self;

  public Account detail(long userId) {
    if (SecurityUtils.hasRoleOnlyUser() && !SecurityUtils.getUserId().equals(userId)) {
      throw new BizException("트레이딩 계좌 정보 접근 권한이 없습니다.");
    }

    return repository.findById(userId).orElseGet(() -> {
      UserResponse user = adminApi.getUser(userId).getResponse();
      if (UserStatus.isNotNormalMember(user.getStatus())) {
        throw new BizException("해당 사용자는 정상 회원이 아닙니다.");
      }

      return self.create(Account.builder().userId(userId).email(user.getEmail()).userNm(user.getUserNm()).operatorId(user.getOperator().getId())
          .centerId(user.getCenter().getId()).build());
    });
  }

  @Transactional
  public Account create(@NonNull Account account) {
    return repository.save(account);
  }

  @Transactional(readOnly = true)
  public AccountResponse detailResponse(long userId, @NonNull TimeZone timeZone) {
    return detail(userId).ofDetailResponse(timeZone);
  }

  @Transactional
  public AccountResponse updateCenter(long userId, long centerId) {
    Account account = detail(userId);
    account.updateCenter(centerId);

    return account.ofResponse();
  }

  @Transactional(readOnly = true)
  public List<CoinResponse> getTradingCoins(long userId, CoinSearchRequest request) {
    return detail(userId).getTradingCoins(request);
  }

  @Transactional(readOnly = true)
  public List<ApiKeyResponse> getCreatableBotApiKeys(long userId, long exchangeId) {
    return detail(userId).getCreatableBotApiKeys(exchangeId);
  }
}
