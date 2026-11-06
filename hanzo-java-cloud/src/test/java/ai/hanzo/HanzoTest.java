package ai.hanzo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ai.hanzo.cloud.ApiClient;
import ai.hanzo.cloud.ApiException;
import ai.hanzo.cloud.api.AccountApi;
import ai.hanzo.cloud.api.AiApi;
import ai.hanzo.cloud.api.OpenapiApi;
import ai.hanzo.cloud.model.AccountApiKey;
import ai.hanzo.cloud.model.AccountApiKeyList;
import ai.hanzo.cloud.model.OpenaiChatCompletionMessage;
import ai.hanzo.cloud.model.OpenaiChatCompletionRequest;
import ai.hanzo.cloud.model.PostChat200Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * What the client puts on the wire and what it reads back, against a local
 * server: the credential, the tenant, the routes the document marks public, a
 * response that grows a field, and a refusal. The compile gate proves the client
 * matches the document; this proves a request built from it is the one the
 * gateway expects.
 */
class HanzoTest {

    private MockWebServer server;

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    private ApiClient client(String key, String org) {
        String base = server.url("/").toString();
        return Hanzo.client(key, base.substring(0, base.length() - 1), org);
    }

    private static MockResponse json(String body) {
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
    }

    @Test
    void defaultsToTheGateway() {
        assertEquals("https://api.hanzo.ai", Hanzo.client(null, null, null).getBasePath());
        assertEquals("https://api.hanzo.ai", Hanzo.client("", "", "").getBasePath());
    }

    @Test
    void sendsTheBearerAndTheTenant() throws Exception {
        server.enqueue(json("{\"keys\":[{\"id\":\"k1\",\"prefix\":\"hk-ab\",\"created\":\"2026-11-01T00:00:00Z\"}]}"));

        AccountApiKeyList list = new AccountApi(client("hk-test", "acme")).getAccountKeys();

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("/v1/account/keys", req.getPath());
        assertEquals("Bearer hk-test", req.getHeader("Authorization"));
        assertEquals("acme", req.getHeader("X-Org-Id"));
        AccountApiKey key = list.getKeys().get(0);
        assertEquals("k1", key.getId());
        assertEquals("hk-ab", key.getPrefix());
        assertEquals("2026-11-01T00:00:00Z", key.getCreated());
    }

    @Test
    void sendsNoCredentialWithoutAKey() throws Exception {
        server.enqueue(json("{\"keys\":[]}"));

        new AccountApi(client(null, null)).getAccountKeys();

        RecordedRequest req = server.takeRequest();
        assertNull(req.getHeader("Authorization"));
        assertNull(req.getHeader("X-Org-Id"));
    }

    @Test
    void sendsNoCredentialWhereTheDocumentSaysNone() throws Exception {
        // GET /v1/openapi.json carries `security: []`: the document is public.
        server.enqueue(json("{}"));

        new OpenapiApi(client("hk-test", null)).getOpenapiJson();

        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/openapi.json", req.getPath());
        assertNull(req.getHeader("Authorization"));
    }

    @Test
    void readsAFieldTheDocumentDoesNotDeclare() throws Exception {
        server.enqueue(json("{\"keys\":[{\"id\":\"k1\",\"grown\":\"later\"}]}"));

        AccountApiKey key = new AccountApi(client("hk-test", null)).getAccountKeys().getKeys().get(0);

        assertEquals("k1", key.getId());
        assertEquals("later", key.getAdditionalProperty("grown"));
    }

    @Test
    void sendsTheCompletionAndItsBounds() throws Exception {
        server.enqueue(json("{\"id\":\"c1\",\"object\":\"chat.completion\",\"model\":\"zen5\","
                + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"hello\"}}]}"));

        PostChat200Response reply = new AiApi(client("hk-test", null)).postChatCompletions(
                new OpenaiChatCompletionRequest()
                        .model("zen5")
                        .messages(List.of(new OpenaiChatCompletionMessage().role("user").content("hi"))),
                new BigDecimal("0.5"), 2000);

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/v1/chat/completions", req.getPath());
        assertEquals("0.5", req.getHeader("X-Max-Cost"));
        assertEquals("2000", req.getHeader("X-Max-Latency-Ms"));
        JsonObject body = JsonParser.parseString(req.getBody().readUtf8()).getAsJsonObject();
        assertEquals("zen5", body.get("model").getAsString());
        assertEquals("hi", body.getAsJsonArray("messages").get(0).getAsJsonObject().get("content").getAsString());
        assertEquals("zen5", reply.getModel());
        assertEquals("hello", reply.getChoices().get(0).getMessage().getContent());
    }

    @Test
    void aRefusalCarriesItsStatusAndBody() {
        server.enqueue(json("{\"code\":\"forbidden\"}").setResponseCode(403));

        ApiException e = assertThrows(ApiException.class,
                () -> new AccountApi(client("hk-bad", null)).getAccountKeys());

        assertEquals(403, e.getCode());
        assertEquals("{\"code\":\"forbidden\"}", e.getResponseBody());
    }
}
