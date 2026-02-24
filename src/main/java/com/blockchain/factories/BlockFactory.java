package com.blockchain.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blockchain.pojos.BlockPojo;
import com.blockchain.pojos.TransactionPojo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BlockFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    private BlockFactory() {
    }

    public static BlockPojo fromMap(Map<String, Object> map) {
        int index = ((Number) map.get("index")).intValue();
        long timestamp = ((Number) map.get("timestamp")).longValue();

        Object transactionsObj = map.get("transactions");
        List<Map<String, Object>> transactionMaps = mapper.convertValue(
                transactionsObj,
                new TypeReference<List<Map<String, Object>>>() {
                });

        List<TransactionPojo> transactions = new ArrayList<>(transactionMaps.stream()
                .map(TransactionFactory::fromMap)
                .toList());

        String previousHash = (String) map.getOrDefault("previous_hash", map.get("previousHash"));
        int nonce = ((Number) map.get("nonce")).intValue();
        String hash = (String) map.get("hash");

        return BlockPojo.builder()
                .index(index)
                .timestamp(timestamp)
                .transactions(transactions)
                .previousHash(previousHash)
                .nonce(nonce)
                .hash(hash)
                .build();
    }
}
