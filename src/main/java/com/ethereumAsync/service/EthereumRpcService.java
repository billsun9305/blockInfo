package com.ethereumAsync.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EthereumRpcService {

    private final WebClient webClient;

    public EthereumRpcService() {
        this.webClient = WebClient.create("http://your-eth-node-url:port");
    }

    public Mono<String> getBlockByNumber(String blockNumber) {
        return webClient.post()
                .uri("/")
                .bodyValue(createRequestBody(blockNumber))
                .retrieve()
                .bodyToMono(String.class);
    }

    private String createRequestBody(String blockNumber) {
        return "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockByNumber\",\"params\":[\"" + blockNumber + "\", true],\"id\":1}";
    }
}
