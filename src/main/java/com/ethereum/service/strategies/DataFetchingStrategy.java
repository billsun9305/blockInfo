package com.ethereum.service.strategies;

import com.ethereum.service.EthereumRpcService;
import org.springframework.web.reactive.function.client.WebClient;

public interface DataFetchingStrategy {
    EthereumRpcService.EthBlockResponse fetchData(String parameter);

    void setWebClient(WebClient webClient);
}
