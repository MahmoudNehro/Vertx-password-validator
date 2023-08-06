package com.app.authentication.passwords;

import com.app.authentication.helpers.EventAddress;
import com.app.authentication.helpers.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LettersVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(LettersVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var eventBus = vertx.eventBus();

    eventBus.<JsonObject>consumer(EventAddress.LETTER_ADDRESS, message -> {
      JsonObject body = message.body();
      String password = body.getString("password");
      String requestID = body.getString(Utils.REQUEST_ID);
      if (password.length() < 8) {
        eventBus.publish(EventAddress.FAILURE_RESPONSE,
          new JsonObject()
            .put("message", "Password must be 8 characters!")
            .put(Utils.REQUEST_ID, requestID)
        );
        return;
      } else {
        eventBus.publish(EventAddress.CASE_ADDRESS, body);
      }
      LOG.info("password received on address {}  {}", EventAddress.LETTER_ADDRESS, password);
    });

  }
}
