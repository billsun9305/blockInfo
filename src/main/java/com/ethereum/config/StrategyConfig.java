package com.ethereum.config;

import com.ethereum.service.strategies.DataFetchingStrategy;
import com.ethereum.service.strategies.LatestBlockFetchingStrategy;
import com.ethereum.service.strategies.SpecificBlockFetchingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StrategyConfig {

    @Bean
    public DataFetchingStrategy latestBlockFetchingStrategy() {
        return new LatestBlockFetchingStrategy();
    }

    @Bean
    public DataFetchingStrategy SpecificBlockFetchingStrategy() {
        return new SpecificBlockFetchingStrategy();
    }
}
