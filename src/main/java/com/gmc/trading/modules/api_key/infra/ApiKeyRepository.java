package com.gmc.trading.modules.api_key.infra;

import com.gmc.trading.modules.api_key.domain.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

}
