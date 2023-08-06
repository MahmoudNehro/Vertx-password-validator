package com.app.authentication;

import com.app.authentication.helpers.EventAddress;
import com.app.authentication.helpers.Utils;
import com.app.authentication.passwords.CaseVerticle;
import com.app.authentication.passwords.DigitVerticle;
import com.app.authentication.passwords.LettersVerticle;
import com.app.authentication.passwords.SpecialCharacter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


public class MainVerticle extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);
  private static final int PORT = 8080;


  public static void main(String[] args) {
    var vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle(), MainVerticle::handleError);
    vertx.deployVerticle(new LettersVerticle(), MainVerticle::handleError);
    vertx.deployVerticle(new CaseVerticle(), MainVerticle::handleError);
    vertx.deployVerticle(new DigitVerticle(), MainVerticle::handleError);
    vertx.deployVerticle(new SpecialCharacter(), MainVerticle::handleError);
    vertx.deployVerticle(new ResponseVerticle(), MainVerticle::handleError);
  }

  private static void handleError(AsyncResult<String> result) {
    if (result.failed()) {
      LOG.error("Error in deploying Main Verticle ", result.cause());
      return;
    }
    LOG.info("Success deployed Main verticle");
  }

  @Override
  public void start(Promise<Void> startPromise) {
    var router = Router.router(vertx);
    var eventBus = vertx.eventBus();

    createRouter(router);

    router.post("/password").handler(routingContext -> requestHandler(eventBus, routingContext));

    createServer(startPromise, router);
  }

  private static void requestHandler(EventBus eventBus, RoutingContext routingContext) {
    LOG.debug("Request body {}", routingContext.body().asString());
    JsonObject requestBody;
    UUID requestID = UUID.randomUUID();
    String requestIDAsString = requestID.toString();
    Utils.routingContextMap.put(requestID, routingContext);

    try {
      requestBody = routingContext.body().asJsonObject();
    } catch (Exception e) {
      eventBus.publish(EventAddress.FAILURE_RESPONSE,
        new JsonObject()
          .put("message", "Please enter a password!")
          .put(Utils.REQUEST_ID, requestIDAsString)
      );
      return;
    }

    if (requestBody.getString("password") == null) {
      eventBus.publish(EventAddress.FAILURE_RESPONSE,
        new JsonObject()
          .put("message", "Please enter a password!")
          .put(Utils.REQUEST_ID, requestIDAsString)

      );
      return;
    }

    LOG.info("Received Body from request Body info {}", requestBody.encode());

    JsonObject requestBodyAsJson = new JsonObject()
      .put("password", requestBody.getString("password"))
      .put(Utils.REQUEST_ID, requestIDAsString);

    eventBus.publish(EventAddress.LETTER_ADDRESS, requestBodyAsJson);
  }


  private void createServer(Promise<Void> startPromise, Router router) {
    vertx.createHttpServer().requestHandler(router).listen(PORT, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        LOG.debug("HTTP server started on port {}", PORT);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void createRouter(Router router) {
    router.route()
      .handler(
        CorsHandler.create()
          .addOrigin("http://localhost:5500")
          .addOrigin("http://127.0.0.1:5500")
          .allowedMethod(HttpMethod.POST)
          .allowCredentials(true)
          .allowedHeader("Access-Control-Allow-Headers")
          .allowedHeader("Access-Control-Allow-Method")
          .allowedHeader("Access-Control-Allow-Origin")
          .allowedHeader("Content-Type")
      );

    router.route().handler(
      BodyHandler.create()
    ).failureHandler(errorContext -> {
      if (errorContext.response().ended()) {
        LOG.info("Current Response ended");
        return;
      }
      LOG.error("Error in router", errorContext.failure());
      errorContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(new JsonObject().put("message", "Something went wrong").toBuffer());
    });
  }
}
