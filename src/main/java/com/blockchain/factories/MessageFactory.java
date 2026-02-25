package com.blockchain.factories;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.blockchain.pojos.BlockPojo;
import com.blockchain.pojos.BlockchainPojo;
import com.blockchain.pojos.MessagePojo;
import com.blockchain.pojos.PayloadPojo;
import com.blockchain.pojos.TransactionPojo;
import com.blockchain.utils.ProtocolUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageFactory {
    private MessageFactory() {
    }

    /**
     * Cria uma mensagem de nova transação
     */
    public static MessagePojo buildNewTransaction(TransactionPojo transaction, String senderHost, Integer senderPort) {
        String sender = senderHost + ":" + senderPort;

        PayloadPojo payload = PayloadPojo.builder()
                .transaction(transaction)
                .build();

        return MessagePojo.builder()
                .type(ProtocolUtil.NEW_TRANSACTION)
                .payload(payload)
                .sender(sender)
                .timestamp(System.currentTimeMillis() / 1000.0)
                .build();
    }

    /**
     * Cria uma mensagem de novo bloco
     */
    public static MessagePojo buildNewBlock(BlockPojo block, String senderHost, Integer senderPort) {
        String sender = senderHost + ":" + senderPort;

        PayloadPojo payload = PayloadPojo.builder()
                .block(block)
                .build();

        return MessagePojo.builder()
                .type(ProtocolUtil.NEW_BLOCK)
                .payload(payload)
                .sender(sender)
                .timestamp(System.currentTimeMillis() / 1000.0)
                .build();
    }

    /**
     * Cria uma mensagem de requisição de chain
     */
    public static MessagePojo buildRequestChain(String senderHost, Integer senderPort) {
        String sender = senderHost + ":" + senderPort;

        return MessagePojo.builder()
                .type(ProtocolUtil.REQUEST_CHAIN)
                .payload(null)
                .sender(sender)
                .timestamp(System.currentTimeMillis() / 1000.0)
                .build();
    }

    /**
     * Cria uma mensagem de resposta com a chain completa
     */
    public static MessagePojo buildResponseChain(BlockchainPojo blockchain, String senderHost, Integer senderPort) {
        String sender = senderHost + ":" + senderPort;

        PayloadPojo payload = PayloadPojo.builder()
                .blockchain(blockchain)
                .build();

        return MessagePojo.builder()
                .type(ProtocolUtil.RESPONSE_CHAIN)
                .payload(payload)
                .sender(sender)
                .timestamp(System.currentTimeMillis() / 1000.0)
                .build();
    }

    /**
     * Deserializa bytes JSON para MessagePojo
     */
    public static MessagePojo fromJson(byte[] jsonBytes) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        return mapper.readValue(json, MessagePojo.class);
    }
}
