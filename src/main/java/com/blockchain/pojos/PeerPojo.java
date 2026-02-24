package com.blockchain.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeerPojo {
    private String host;
    private int port;

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
