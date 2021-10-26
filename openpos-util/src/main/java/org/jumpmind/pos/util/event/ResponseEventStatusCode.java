package org.jumpmind.pos.util.event;

import org.jumpmind.pos.util.model.AbstractTypeCode;

public class ResponseEventStatusCode extends AbstractTypeCode {

    public static final ResponseEventStatusCode OK = new ResponseEventStatusCode("OK");
    public static final ResponseEventStatusCode ERROR = new ResponseEventStatusCode("ERROR");

    protected ResponseEventStatusCode(String value) {
        super(value);
    }

    public final static ResponseEventStatusCode of(String value) {
        return AbstractTypeCode.of(ResponseEventStatusCode.class, value);
    }
}
