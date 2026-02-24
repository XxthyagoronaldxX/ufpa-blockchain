package com.blockchain.old;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.blockchain.utils.ProtocolUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

public class Message {

    private final String type;
    private final Object data;
    private final long timestamp;

    @SerializedName("sender_host")
    private final String senderHost;

    @SerializedName("sender_port")
    private final Integer senderPort;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Construtor privado - use os métodos estáticos buildXXX()
     */
    private Message(String type, Object data, String senderHost, Integer senderPort) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.senderHost = senderHost;
        this.senderPort = senderPort;
    }

    /**
     * Cria uma mensagem de nova transação
     */
    public static Message buildNewTransaction(Transaction transaction, String senderHost, Integer senderPort) {
        return new Message(ProtocolUtil.NEW_TRANSACTION, transaction.toMap(), senderHost, senderPort);
    }

    /**
     * Cria uma mensagem de novo bloco
     */
    public static Message buildNewBlock(Block block, String senderHost, Integer senderPort) {
        return new Message(ProtocolUtil.NEW_BLOCK, block.toMap(), senderHost, senderPort);
    }

    /**
     * Cria uma mensagem de requisição de chain
     */
    public static Message buildRequestChain(String senderHost, Integer senderPort) {
        return new Message(ProtocolUtil.REQUEST_CHAIN, null, senderHost, senderPort);
    }

    /**
     * Cria uma mensagem de resposta com a chain completa
     */
    public static Message buildResponseChain(Blockchain blockchain, String senderHost, Integer senderPort) {
        return new Message(ProtocolUtil.RESPONSE_CHAIN, blockchain.toMapList(), senderHost, senderPort);
    }

    /**
     * Serializa a mensagem para JSON (bytes)
     * Equivalente ao to_json() do Python
     */
    public byte[] toJson() {
        String json = gson.toJson(this);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Serializa a mensagem para String JSON
     */
    public String toJsonString() {
        return gson.toJson(this);
    }

    /**
     * Deserializa bytes JSON para Message
     * Equivalente ao from_json() do Python
     */
    public static Message fromJson(byte[] jsonBytes) {
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        return gson.fromJson(json, Message.class);
    }

    /**
     * Deserializa String JSON para Message
     */
    public static Message fromJsonString(String json) {
        return gson.fromJson(json, Message.class);
    }

    /**
     * Extrai a transação do data (quando type = NEW_TRANSACTION)
     */
    @SuppressWarnings("unchecked")
    public Transaction getTransaction() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        if (!ProtocolUtil.NEW_TRANSACTION.equals(type)) {
            throw new IllegalStateException("Tipo de mensagem não é NEW_TRANSACTION");
        }

        if (data instanceof Map) {
            return Transaction.fromMap((Map<String, Object>) data);
        }

        // Se data for LinkedTreeMap do Gson
        String json = gson.toJson(data);
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
        return Transaction.fromMap(map);
    }

    /**
     * Extrai o bloco do data (quando type = NEW_BLOCK)
     */
    @SuppressWarnings("unchecked")
    public Block getBlock() {
        if (!ProtocolUtil.NEW_BLOCK.equals(type)) {
            throw new IllegalStateException("Tipo de mensagem não é NEW_BLOCK");
        }

        if (data instanceof Map) {
            return Block.fromMap((Map<String, Object>) data);
        }

        // Se data for LinkedTreeMap do Gson
        String json = gson.toJson(data);
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
        return Block.fromMap(map);
    }

    /**
     * Extrai a blockchain do data (quando type = RESPONSE_CHAIN)
     */
    @SuppressWarnings("unchecked")
    public Blockchain getBlockchain() {
        if (!ProtocolUtil.RESPONSE_CHAIN.equals(type)) {
            throw new IllegalStateException("Tipo de mensagem não é RESPONSE_CHAIN");
        }

        if (data instanceof List) {
            return Blockchain.fromMapList((List<Map<String, Object>>) data);
        }

        // Se data for ArrayList do Gson
        String json = gson.toJson(data);
        List<Map<String, Object>> mapList = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>() {}.getType());
        return Blockchain.fromMapList(mapList);
    }

    // Getters e Setters
    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderHost() {
        return senderHost;
    }

    public Integer getSenderPort() {
        return senderPort;
    }

    @Override
    public String toString() {
        return String.format("Message{type='%s', timestamp=%d, sender=%s:%s}", type, timestamp, senderHost, senderPort);
    }
}
