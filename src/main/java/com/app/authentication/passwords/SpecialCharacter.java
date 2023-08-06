package com.app.authentication.passwords;

import com.app.authentication.helpers.EventAddress;
import com.app.authentication.helpers.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecialCharacter extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(SpecialCharacter.class);

  public void start(Promise<Void> startPromise) throws Exception {
    var eventBus = vertx.eventBus();

    eventBus.<JsonObject>consumer(EventAddress.SPECIAL_CHARACTER, message -> {
      String requestID = message.body().getString(Utils.REQUEST_ID);

      if (!message.body().getString("password").matches("[A-Za-z0-9]+")) {
        LOG.info("There is a special character");
        eventBus.publish(EventAddress.SUCCESS,
          new JsonObject()
            .put("message", "Password is strong!")
            .put(Utils.REQUEST_ID, requestID)
          );
        return;
      }

      eventBus.publish(EventAddress.FAILURE_RESPONSE,
        new JsonObject()
          .put("message", "Password must have one special character")
          .put(Utils.REQUEST_ID, requestID)
      );
    });

  }

}
