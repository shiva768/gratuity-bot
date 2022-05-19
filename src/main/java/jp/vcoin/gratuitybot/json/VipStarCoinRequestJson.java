package jp.vcoin.gratuitybot.json;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class VirtualCoinRequestJson {

    private final static String RPC_VERSION = "1.0";

    private final String jsonrpc;
    private final String id;
    private final String method;
    private final List<Object> params;

    public VirtualCoinRequestJson(String id, String method, List<Object> params) {
        this.jsonrpc = RPC_VERSION;
        this.id = id;
        this.method = method;
        this.params = params;

    }

    public VirtualCoinRequestJson(String id, String method, Object... params) {
        this.jsonrpc = RPC_VERSION;
        this.id = id;
        this.method = method;
        this.params = Arrays.asList(params);

    }
}
