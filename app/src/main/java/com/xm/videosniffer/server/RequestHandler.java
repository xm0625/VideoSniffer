package com.xm.videosniffer.server;

import fi.iki.elonen.NanoHTTPD;

import java.util.List;
import java.util.Map;

/**
 * Created by xm on 17-11-1.
 */
public interface RequestHandler {
    Object server(NanoHTTPD.IHTTPSession session, NanoHTTPD.Method method, String url, Map<String, List<String>> parameters);
}
