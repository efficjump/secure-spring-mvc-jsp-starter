package com.example.webstarter.web;

public final class RequestContext {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_ATTRIBUTE = RequestContext.class.getName() + ".requestId";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    private RequestContext() {
    }
}

