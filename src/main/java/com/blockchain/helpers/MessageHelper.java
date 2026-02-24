package com.blockchain.helpers;

import java.util.List;
import java.util.Map;

import com.blockchain.factories.BlockFactory;
import com.blockchain.factories.TransactionFactory;
import com.blockchain.pojos.BlockPojo;
import com.blockchain.pojos.BlockchainPojo;
import com.blockchain.pojos.MessagePojo;
import com.blockchain.pojos.TransactionPojo;
import com.blockchain.utils.ProtocolUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageHelper {
    private static final ObjectMapper mapper = new ObjectMapper();

    private MessageHelper() {
    }

    public static byte[] toJson(MessagePojo message) throws JsonProcessingException {
        return mapper.writeValueAsBytes(message);
    }

    public static String toJsonString(MessagePojo message) throws JsonProcessingException {
        return mapper.writeValueAsString(message);
    }

    public static TransactionPojo getTransaction(MessagePojo message) {
        if (!ProtocolUtil.NEW_TRANSACTION.equals(message.getType())) {
            throw new IllegalStateException("Tipo de mensagem não é NEW_TRANSACTION");
        }

        Map<String, Object> map = mapper.convertValue(message.getData(), new TypeReference<Map<String, Object>>() {
        });

        return TransactionFactory.fromMap(map);
    }

    public static BlockPojo getBlock(MessagePojo message) {
        if (!ProtocolUtil.NEW_BLOCK.equals(message.getType())) {
            throw new IllegalStateException("Tipo de mensagem não é NEW_BLOCK");
        }

        Map<String, Object> map = mapper.convertValue(message.getData(), new TypeReference<Map<String, Object>>() {
        });

        return BlockFactory.fromMap(map);
    }

    public static BlockchainPojo getBlockchain(MessagePojo message) {
        if (!ProtocolUtil.RESPONSE_CHAIN.equals(message.getType())) {
            throw new IllegalStateException("Tipo de mensagem não é RESPONSE_CHAIN");
        }

        List<BlockPojo> chain = mapper.convertValue(message.getData(), new TypeReference<List<BlockPojo>>() {
        });

        BlockchainPojo blockchainPojo = new BlockchainPojo();
        blockchainPojo.setChain(chain);

        return blockchainPojo;
    }
}
