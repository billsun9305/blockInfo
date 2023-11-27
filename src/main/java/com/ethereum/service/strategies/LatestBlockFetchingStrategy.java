package com.ethereum.service.strategies;

import com.ethereum.service.EthereumRpcService;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

public class LatestBlockFetchingStrategy implements DataFetchingStrategy {
    private WebClient webClient;

    @Override
    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Boolean - If true it returns the full transaction objects, if false only the hashes of the transactions.
     */
    @Override
    public EthereumRpcService.EthBlockResponse fetchData(String blockNumber) {
        EthereumRpcService.EthBlockResponse response = webClient.post()
                .bodyValue(new EthereumRpcService.EthRequest("2.0", "eth_getBlockByNumber", List.of(blockNumber, false), 1))
                .retrieve()
                .bodyToMono(EthereumRpcService.EthBlockResponse.class)
                .block();
        return response;
    }
}

