package com.blockchain.helpers;

import com.blockchain.pojos.BlockPojo;
import com.blockchain.pojos.BlockchainPojo;
import com.blockchain.pojos.MessagePojo;
import com.blockchain.pojos.PayloadPojo;
import com.blockchain.pojos.TransactionPojo;
import com.blockchain.utils.ProtocolUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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

        PayloadPojo payloadPojo = mapper.convertValue(message.getPayload(), PayloadPojo.class);

        return payloadPojo.getTransaction();
    }

    public static BlockPojo getBlock(MessagePojo message) {
        if (!ProtocolUtil.NEW_BLOCK.equals(message.getType())) {
            throw new IllegalStateException("Tipo de mensagem não é NEW_BLOCK");
        }

        PayloadPojo payloadPojo = mapper.convertValue(message.getPayload(), PayloadPojo.class);

        return payloadPojo.getBlock();
    }

    public static BlockchainPojo getBlockchain(MessagePojo message) {
        if (!ProtocolUtil.RESPONSE_CHAIN.equals(message.getType())) {
            throw new IllegalStateException("Tipo de mensagem não é RESPONSE_CHAIN");
        }

        PayloadPojo payloadPojo = mapper.convertValue(message.getPayload(), PayloadPojo.class);

        return payloadPojo.getBlockchain();
    }
}
