package jp.vcoin.gratuitybot.exception;

import java.io.IOException;

public class RPCException extends IOException {

    public RPCException(String s) {
        super(s);
    }
}
