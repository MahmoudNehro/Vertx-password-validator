package com.app.authentication.helpers;

import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Utils {

  public static final String REQUEST_ID = "requestID";

  public static final Map<UUID, RoutingContext> routingContextMap = new HashMap<>();
}
