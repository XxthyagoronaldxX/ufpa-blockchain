package com.blockchain.helpers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import com.blockchain.exceptions.CryptoException;
import com.blockchain.pojos.BlockPojo;
import com.blockchain.utils.CryptoUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlockHelper {
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

    private BlockHelper() {
    }

    /**
     * Calcula hash usando JSON com chaves ordenadas alfabeticamente
     */
    public static String calculateHash(BlockPojo block) {
        try {
            // Cria mapa ordenado alfabeticamente (como sort_keys=True do Python)
            Map<String, Object> blockData = new LinkedHashMap<>();
            blockData.put("index", block.getIndex());
            blockData.put("nonce", block.getNonce());
            blockData.put("previous_hash", block.getPreviousHash());
            blockData.put("timestamp", BigDecimal.valueOf(block.getTimestamp()));
            blockData.put("transactions", block.transactionsToMap());

            // Serializa para JSON com espaçamento (como Python json.dumps)
            String jsonData = mapper.writeValueAsString(blockData)
                    .replace(",", ", ")
                    .replace("\":", "\": ");

            log.debug("JSON para hash: {}", jsonData);

            return CryptoUtil.applySHA256(jsonData);
        } catch (JsonProcessingException e) {
            log.error("Erro ao calcular hash: {}", e.getMessage());
            throw new CryptoException("Erro ao calcular hash do bloco", e);
        }
    }

    public static void mineBlock(BlockPojo block, String difficulty) {
        String target = new String(new char[difficulty.length()]).replace('\0', '0');

        // Calculate initial hash before checking
        String hash = calculateHash(block);

        while (!hash.substring(0, difficulty.length()).equals(target)) {
            block.setNonce(block.getNonce() + 1);
            hash = calculateHash(block);
        }

        block.setHash(hash);
        log.debug("Bloco minerado: {}", hash);
    }
}
