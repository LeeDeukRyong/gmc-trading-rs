package com.gmc.trading.modules.volume_bot.infra;

import com.gmc.common.code.common.IsYn;
import com.gmc.trading.modules.volume_bot.domain.VolumeBot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface VolumeBotRepository extends JpaRepository<VolumeBot, Long> {

  List<VolumeBot> findAllByWorkYnAndDelYn(@NonNull IsYn workYn, @NonNull IsYn delYn);
}
