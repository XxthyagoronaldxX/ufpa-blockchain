package com.blockchain.pojos;

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

    private PayloadPojo payload;

    private double timestamp;

    private String sender;
}
