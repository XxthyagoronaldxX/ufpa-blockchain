package com.blockchain.pojos;

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
public class MessagePojo {

    private String type;

    private Object data;

    private long timestamp;

    @JsonProperty("sender_host")
    private String senderHost;

    @JsonProperty("sender_port")
    private Integer senderPort;
}
