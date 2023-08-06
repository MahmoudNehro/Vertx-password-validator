package com.app.authentication;

import com.app.authentication.helpers.EventAddress;
import com.app.authentication.helpers.Utils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ResponseVerticle extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(ResponseVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var eventBus = vertx.eventBus();
    eventBus.<JsonObject>consumer(EventAddress.FAILURE_RESPONSE, message -> {
      RoutingContext routingContext = getRoutingContext(message);
      if (routingContext == null)
      {
        startPromise.fail("Unknown request");
        return;
      }
      returnsResponse(routingContext, message.body().getString("message"), HttpResponseStatus.UNPROCESSABLE_ENTITY);
    });

    eventBus.<JsonObject>consumer(EventAddress.SUCCESS, message -> {
      RoutingContext routingContext = getRoutingContext(message);
      if (routingContext == null) {
        startPromise.fail("Unknown request");
        return;
      }
      returnsResponse(routingContext, message.body().getString("message"), HttpResponseStatus.OK);
    });
    startPromise.complete();
  }

  private static RoutingContext getRoutingContext(Message<JsonObject> message) {
    LOG.debug("Body received in Response Verticle :: {}" , message.body().encode());
    UUID requestID = UUID.fromString(message.body().getString(Utils.REQUEST_ID));
    RoutingContext routingContext = Utils.routingContextMap.remove(requestID);
    if (routingContext == null) {
      LOG.error("Unknown request id");
      return null;
    }
    LOG.debug("Body after getting context :: {}" , message.body().encode());
    return routingContext;
  }

  private static void returnsResponse(RoutingContext routingContext, String message, HttpResponseStatus status) {
    LOG.debug("Message {} ", message);
    if (routingContext.response().ended()) {
      LOG.info("Response ended ");
      return;
    }
    routingContext.response().putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .setStatusCode(status.code())
      .end(new JsonObject().put("message", message).toBuffer()).onComplete(voidAsyncResult -> {
        LOG.info("Response completed {}", voidAsyncResult);
      });
  }
}
