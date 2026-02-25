package com.blockchain.pojos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import com.blockchain.helpers.BlockHelper;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockPojo {
    private int index;

    private double timestamp;

    private List<TransactionPojo> transactions;

    private String hash;

    private int nonce;

    @JsonProperty("previous_hash")
    private String previousHash;

    public BlockPojo(int index, List<TransactionPojo> transactions, String previousHash) {
        this.index = index;
        this.timestamp = System.currentTimeMillis() / 1000.0; // Converte ms para segundos
        this.transactions = new ArrayList<>(transactions);
        this.previousHash = previousHash;
        this.nonce = 0;
        this.hash = "";
    }

    public List<Map<String, Object>> transactionsToMap() {
        List<Map<String, Object>> mapList = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            TransactionPojo t = transactions.get(i);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("destino", t.getDestino());
            map.put("id", t.getId());
            map.put("origem", t.getOrigem());
            map.put("timestamp", BigDecimal.valueOf(t.getTimestamp()));
            map.put("valor", BigDecimal.valueOf(t.getValor()));
            mapList.add(map);
        }

        return mapList;
    }

    public String transactionsToString() {
        return transactions.stream()
                .map(t -> t.getOrigem() + t.getDestino() + t.getValor() + t.getTimestamp())
                .collect(Collectors.joining());
    }

    public boolean isValid(String difficulty) {
        String calculatedHash = BlockHelper.calculateHash(this);
        String target = new String(new char[difficulty.length()]).replace('\0', '0');

        // Check if hash has sufficient length
        if (hash == null || hash.length() < difficulty.length()) {
            return false;
        }

        return calculatedHash.equals(hash) &&
                hash.substring(0, difficulty.length()).equals(target);
    }
}
