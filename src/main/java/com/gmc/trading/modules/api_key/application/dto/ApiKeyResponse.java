package com.gmc.trading.modules.api_key.application.dto;

import com.gmc.common.dto.common.BaseResponse;
import com.gmc.trading.modules.arbitrage_bot.application.dto.ArbitrageBotResponse;
import com.gmc.trading.modules.bot.application.dto.BotResponse;
import com.gmc.trading.modules.buy_bot.application.dto.BuyBotResponse;
import com.gmc.trading.modules.exchange.application.dto.ExchangeResponse;
import com.gmc.trading.modules.grid_bot.application.dto.GridBotResponse;
import com.gmc.trading.modules.list_bot.application.dto.ListBotResponse;
import com.gmc.trading.modules.sell_bot.application.dto.SellBotResponse;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ApiKeyResponse extends BaseResponse {

  private Long id;
  private String keyName;
  private String accessKey;
  private String secretKey;
  private String passPhrase;
  private LocalDateTime expireDt;

  private Long userId;

  private ExchangeResponse exchange;

  private List<BotResponse> bots;
  private List<VolumeBotResponse> volumeBots;
  private List<GridBotResponse> gridBots;
  private List<ListBotResponse> listBots;
  private List<BuyBotResponse> buyBots;
  private List<SellBotResponse> sellBots;
  private List<ArbitrageBotResponse> arbitrageBotResponses;
}
