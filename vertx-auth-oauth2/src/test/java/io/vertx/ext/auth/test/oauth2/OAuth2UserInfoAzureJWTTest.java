package io.vertx.ext.auth.test.oauth2;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.impl.http.SimpleHttpClient;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertNotEquals;

public class OAuth2UserInfoAzureJWTTest extends VertxTestBase {

  private static final String fixtureV1 = "eyJhbGciOiJub25lIn0.eyJhdWQiOiJiMTRhNzUwNS05NmU5LTQ5MjctOTFlOC0wNjAxZDBmYzljYWEiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9mYTE1ZDY5Mi1lOWM3LTQ0NjAtYTc0My0yOWYyOTU2ZmQ0MjkvIiwiaWF0IjoxNTM2Mjc1MTI0LCJuYmYiOjE1MzYyNzUxMjQsImV4cCI6MTUzNjI3OTAyNCwiYWlvIjoiQVhRQWkvOElBQUFBcXhzdUIrUjREMnJGUXFPRVRPNFlkWGJMRDlrWjh4ZlhhZGVBTTBRMk5rTlQ1aXpmZzN1d2JXU1hodVNTajZVVDVoeTJENldxQXBCNWpLQTZaZ1o5ay9TVTI3dVY5Y2V0WGZMT3RwTnR0Z2s1RGNCdGsrTExzdHovSmcrZ1lSbXY5YlVVNFhscGhUYzZDODZKbWoxRkN3PT0iLCJhbXIiOlsicnNhIl0sImVtYWlsIjoiYWJlbGlAbWljcm9zb2Z0LmNvbSIsImZhbWlseV9uYW1lIjoiTGluY29sbiIsImdpdmVuX25hbWUiOiJBYmUiLCJpZHAiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC83MmY5ODhiZi04NmYxLTQxYWYtOTFhYi0yZDdjZDAxMWRiNDcvIiwiaXBhZGRyIjoiMTMxLjEwNy4yMjIuMjIiLCJuYW1lIjoiYWJlbGkiLCJub25jZSI6IjEyMzUyMyIsIm9pZCI6IjA1ODMzYjZiLWFhMWQtNDJkNC05ZWMwLTFiMmJiOTE5NDQzOCIsInJoIjoiSSIsInN1YiI6IjVfSjlyU3NzOC1qdnRfSWN1NnVlUk5MOHhYYjhMRjRGc2dfS29vQzJSSlEiLCJ0aWQiOiJmYTE1ZDY5Mi1lOWM3LTQ0NjAtYTc0My0yOWYyOTU2ZmQ0MjkiLCJ1bmlxdWVfbmFtZSI6IkFiZUxpQG1pY3Jvc29mdC5jb20iLCJ1dGkiOiJMeGVfNDZHcVRrT3BHU2ZUbG40RUFBIiwidmVyIjoiMS4wIn0=";
  private static final String fixtureV2 = "eyJhbGciOiJub25lIn0.eyJ2ZXIiOiIyLjAiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vOTEyMjA0MGQtNmM2Ny00YzViLWIxMTItMzZhMzA0YjY2ZGFkL3YyLjAiLCJzdWIiOiJBQUFBQUFBQUFBQUFBQUFBQUFBQUFJa3pxRlZyU2FTYUZIeTc4MmJidGFRIiwiYXVkIjoiNmNiMDQwMTgtYTNmNS00NmE3LWI5OTUtOTQwYzc4ZjVhZWYzIiwiZXhwIjoxNTM2MzYxNDExLCJpYXQiOjE1MzYyNzQ3MTEsIm5iZiI6MTUzNjI3NDcxMSwibmFtZSI6IkFiZSBMaW5jb2xuIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiQWJlTGlAbWljcm9zb2Z0LmNvbSIsIm9pZCI6IjAwMDAwMDAwLTAwMDAtMDAwMC02NmYzLTMzMzJlY2E3ZWE4MSIsInRpZCI6IjkxMjIwNDBkLTZjNjctNGM1Yi1iMTEyLTM2YTMwNGI2NmRhZCIsIm5vbmNlIjoiMTIzNTIzIiwiYWlvIjoiRGYyVVZYTDFpeCFsTUNXTVNPSkJjRmF0emNHZnZGR2hqS3Y4cTVnMHg3MzJkUjVNQjVCaXN2R1FPN1lXQnlqZDhpUURMcSFlR2JJRGFreXA1bW5PcmNkcUhlWVNubHRlcFFtUnA2QUlaOGpZIn0";

  private String fixture;

  private static final JsonObject azureParams = new JsonObject()
    .put("alt", "json");

  private OAuth2Auth oauth2;
  private HttpServer server;

  private final OAuth2Options oauthConfig = new OAuth2Options()
    .setFlow(OAuth2FlowType.AUTH_CODE)
    .setClientId("client-id")
    .setClientSecret("client-secret")
    .setSite("http://localhost:8080")
    .setUserInfoPath("/oauth/userinfo")
    .setUserInfoParameters(azureParams);

  @Override
  public void setUp() throws Exception {
    super.setUp();
    oauth2 = OAuth2Auth.create(vertx, oauthConfig);

    final CountDownLatch latch = new CountDownLatch(1);

    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.GET && "/oauth/userinfo".equals(req.path())) {
        assertTrue(req.getHeader("Authorization").contains("Bearer "));

        try {
          assertEquals(azureParams, SimpleHttpClient.queryToJson(Buffer.buffer(req.query())));
        } catch (UnsupportedEncodingException e) {
          fail(e);
        }

        req.response().putHeader("Content-Type", "application/jwt").end(fixture);
      } else {
        req.response().setStatusCode(400).end();
      }
    }).listen(8080, ready -> {
      if (ready.failed()) {
        throw new RuntimeException(ready.cause());
      }
      // ready
      latch.countDown();
    });

    latch.await();
  }

  @Override
  public void tearDown() throws Exception {
    server.close();
    super.tearDown();
  }

  @Test
  public void getUserInfoV1() {
    final User user = User.create(new JsonObject("{\"access_token\":\"eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJhdXRob3JpemF0aW9uIjp7InBlcm1pc3Npb25zIjpbeyJyZXNvdXJjZV9zZXRfaWQiOiJkMmZlOTg0My02NDYyLTRiZmMtYmFiYS1iNTc4N2JiNmUwZTciLCJyZXNvdXJjZV9zZXRfbmFtZSI6IkhlbGxvIFdvcmxkIFJlc291cmNlIn1dfSwianRpIjoiZDYxMDlhMDktNzhmZC00OTk4LWJmODktOTU3MzBkZmQwODkyLTE0NjQ5MDY2Nzk0MDUiLCJleHAiOjk5OTk5OTk5OTksIm5iZiI6MCwiaWF0IjoxNDY0OTA2NjcxLCJzdWIiOiJmMTg4OGY0ZC01MTcyLTQzNTktYmUwYy1hZjMzODUwNWQ4NmMiLCJ0eXAiOiJrY19ldHQiLCJhenAiOiJoZWxsby13b3JsZC1hdXRoei1zZXJ2aWNlIn0\",\"active\":true,\"scope\":\"scopeA scopeB\",\"client_id\":\"client-id\",\"username\":\"username\",\"token_type\":\"bearer\",\"expires_at\":99999999999000}"));

    assertNotEquals("abeli", user.attributes().getString("name"));
    assertNotEquals("abeli@microsoft.com", user.attributes().getString("email"));

    fixture = fixtureV1;

    oauth2.userInfo(user, userInfo -> {
      if (userInfo.failed()) {
        fail(userInfo.cause().getMessage());
      } else {
        // assert that the user has now a few extra fields on the attributes
        assertEquals("abeli", user.attributes().getString("name"));
        assertEquals("", user.attributes().getString("picture", ""));
        assertEquals("abeli@microsoft.com", user.attributes().getString("email"));
        testComplete();
      }
    });
    await();
  }

  @Test
  public void getUserInfoV2() {
    final User user = User.create(new JsonObject("{\"access_token\":\"eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJhdXRob3JpemF0aW9uIjp7InBlcm1pc3Npb25zIjpbeyJyZXNvdXJjZV9zZXRfaWQiOiJkMmZlOTg0My02NDYyLTRiZmMtYmFiYS1iNTc4N2JiNmUwZTciLCJyZXNvdXJjZV9zZXRfbmFtZSI6IkhlbGxvIFdvcmxkIFJlc291cmNlIn1dfSwianRpIjoiZDYxMDlhMDktNzhmZC00OTk4LWJmODktOTU3MzBkZmQwODkyLTE0NjQ5MDY2Nzk0MDUiLCJleHAiOjk5OTk5OTk5OTksIm5iZiI6MCwiaWF0IjoxNDY0OTA2NjcxLCJzdWIiOiJmMTg4OGY0ZC01MTcyLTQzNTktYmUwYy1hZjMzODUwNWQ4NmMiLCJ0eXAiOiJrY19ldHQiLCJhenAiOiJoZWxsby13b3JsZC1hdXRoei1zZXJ2aWNlIn0\",\"active\":true,\"scope\":\"scopeA scopeB\",\"client_id\":\"client-id\",\"username\":\"username\",\"token_type\":\"bearer\",\"expires_at\":99999999999000}"));

    assertNotEquals("Abe Lincoln", user.attributes().getString("name"));

    fixture = fixtureV2;

    oauth2.userInfo(user, userInfo -> {
      if (userInfo.failed()) {
        fail(userInfo.cause().getMessage());
      } else {
        // assert that the user has now a few extra fields on the attributes
        assertEquals("Abe Lincoln", user.attributes().getString("name"));
        assertEquals("", user.attributes().getString("picture", ""));
        assertEquals("", user.attributes().getString("email", ""));
        testComplete();
      }
    });
    await();
  }
}
