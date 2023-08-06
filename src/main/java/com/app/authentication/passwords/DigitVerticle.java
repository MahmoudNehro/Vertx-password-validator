package com.app.authentication.passwords;

import com.app.authentication.helpers.EventAddress;
import com.app.authentication.helpers.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class DigitVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var eventBus = vertx.eventBus();

    eventBus.<JsonObject>consumer(EventAddress.DIGIT_ADDRESS, message -> {
      String password = message.body().getString("password");
      String requestID = message.body().getString(Utils.REQUEST_ID);

      for (char c : password.toCharArray()) {
        if (Character.isDigit(c)) {
          eventBus.publish(EventAddress.SPECIAL_CHARACTER, message.body());
          return;
        }
      }
      eventBus.publish(EventAddress.FAILURE_RESPONSE,

        new JsonObject()
          .put("message", "Password must have one digit")
          .put(Utils.REQUEST_ID, requestID)
      );
    });

  }
}
