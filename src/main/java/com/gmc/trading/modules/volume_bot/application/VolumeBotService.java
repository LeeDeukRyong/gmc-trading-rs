package com.gmc.trading.modules.volume_bot.application;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.code.common.MessageCode;
import com.gmc.common.exception.BizException;
import com.gmc.common.redis.annotation.DistributedLock;
import com.gmc.common.utils.SecurityUtils;
import com.gmc.trading.modules.account.application.AccountService;
import com.gmc.trading.modules.coin.application.CoinService;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotCreate;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotResponse;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotSearchRequest;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotSearchResponse;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotUpdate;
import com.gmc.trading.modules.volume_bot.domain.VolumeBot;
import com.gmc.trading.modules.volume_bot.infra.VolumeBotMapper;
import com.gmc.trading.modules.volume_bot.infra.VolumeBotRepository;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

@Slf4j
@RequiredArgsConstructor
@Service
public class VolumeBotService {

  private final VolumeBotRepository repository;
  private final VolumeBotMapper mapper;
  private final AccountService accountService;
  private final CoinService coinService;

  @Transactional(readOnly = true)
  public List<VolumeBotSearchResponse> list(VolumeBotSearchRequest request) {
    return mapper.searchVolumeBotList(request);
  }

  public VolumeBot detail(long id) {
    VolumeBot volumeBot = repository.findById(id).orElseThrow(() -> new BizException(MessageCode.NOT_EXIST_DATA));

    if (!SecurityUtils.hasRoleAdmin() && volumeBot.isDeleted()) {
      throw new BizException("사용할 수 없는 거래량 봇 입니다.");
    }

    if (!SecurityUtils.hasRoleAdminOrPlatform() &&
        ((SecurityUtils.hasRoleOnlyUser() && !volumeBot.getAccount().getUserId().equals(SecurityUtils.getUserId())) ||
            (SecurityUtils.hasRoleOperator() && !volumeBot.getAccount().getOperatorId().equals(SecurityUtils.getOperatorId())) ||
            (!SecurityUtils.hasRoleOperator() && SecurityUtils.hasRoleCenter() && !volumeBot.getAccount().getCenterId()
                .equals(SecurityUtils.getCenterId())))) {
      throw new BizException("거래량 봇 정보 접근 권한이 없습니다.");
    }

    return volumeBot;
  }

  public VolumeBot detailWithCheckOwner(long id) {
    VolumeBot volumeBot = detail(id);
    if (!SecurityUtils.hasRoleAdmin() && !volumeBot.getAccount().getUserId().equals(SecurityUtils.getUserId())) {
      throw new BizException("본인이 소유한 거래량 봇이 아닙니다.");
    }

    return volumeBot;
  }

  @Transactional(readOnly = true)
  public VolumeBotResponse detailResponse(long id) {
    return detail(id).ofDetailResponse();
  }

  @Transactional
  public VolumeBotResponse create(@NonNull VolumeBotCreate create) {
    VolumeBot volumeBot = create.ofEntity();
    volumeBot.setAccount(accountService.detail(create.getUserId()));
    volumeBot.setCoin(coinService.detail(create.getCoinId()));
    volumeBot.setApiKey(create.getApiKeyId());
    volumeBot.create();

    return repository.save(volumeBot).ofDetailResponse();
  }

  @Transactional
  public VolumeBotResponse update(long id, @NonNull VolumeBotUpdate update) {
    VolumeBot volumeBot = detailWithCheckOwner(id);
    volumeBot.update(update);

    repository.saveAndFlush(volumeBot);

    return volumeBot.ofDetailResponse();
  }

  @Transactional
  public void delete(long id) {
    VolumeBot volumeBot = detailWithCheckOwner(id);
    volumeBot.delete();

    repository.saveAndFlush(volumeBot);
  }

  @DistributedLock(prefix = "VolumeBotService", key = "#id", lockWait = false, leaseTime = 1, timeUnit = TimeUnit.DAYS)
  public void monitoring(long id) {
    log.info("Volume Bot {} monitoring start", id);
    StopWatch stopWatch = new StopWatch("Volume Bot id : " + id);
    stopWatch.start();

    VolumeBot volumeBot = detail(id);
    volumeBot.monitoring();
    repository.saveAndFlush(volumeBot);

    stopWatch.stop();
    log.info("Volume Bot {} monitoring stop : {} sec", id, stopWatch.getTotalTimeSeconds());
  }

  @Transactional(readOnly = true)
  public List<VolumeBot> getWorkingBotList() {
    return repository.findAllByWorkYnAndDelYn(IsYn.Y, IsYn.N);
  }
}