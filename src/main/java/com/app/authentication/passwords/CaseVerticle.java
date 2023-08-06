package com.app.authentication.passwords;

import com.app.authentication.helpers.EventAddress;
import com.app.authentication.helpers.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseVerticle extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(CaseVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var eventBus = vertx.eventBus();

    eventBus.<JsonObject>consumer(EventAddress.CASE_ADDRESS, message -> {
      String password = message.body().getString("password");
      String requestID = message.body().getString(Utils.REQUEST_ID);
      LOG.debug("Received message {} ", password);
      boolean hasOneUpperCase = false;
      boolean hasOneLowerCase = false;
      for (char c : password.toCharArray()) {
          if (Character.isUpperCase(c)) {
            hasOneUpperCase = true;
          } else if (Character.isLowerCase(c)) {
            hasOneLowerCase = true;
          }

        if (hasOneUpperCase && hasOneLowerCase) {
          eventBus.publish(EventAddress.DIGIT_ADDRESS,message.body());
          LOG.debug("Sent success");
          return;
        }
      }

      eventBus.publish(EventAddress.FAILURE_RESPONSE,
        new JsonObject()
          .put("message", "Password must have at least one upper case and one lower case!")
          .put(Utils.REQUEST_ID, requestID)

      );
      LOG.debug("Sent error");

    });

  }
}
