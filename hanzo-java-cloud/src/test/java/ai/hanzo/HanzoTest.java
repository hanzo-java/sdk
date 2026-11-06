package ai.hanzo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ai.hanzo.ApiClient;
import ai.hanzo.ApiException;
import ai.hanzo.api.AccountApi;
import ai.hanzo.api.AiApi;
import ai.hanzo.api.OpenapiApi;
import ai.hanzo.model.AccountApiKey;
import ai.hanzo.model.AccountApiKeyList;
import ai.hanzo.model.AiDecisionsChoice;
import ai.hanzo.model.AiDecisionsQuestion;
import ai.hanzo.model.AiDecisionsRequest;
import ai.hanzo.model.AiDecisionsResponse;
import ai.hanzo.model.OpenaiChatCompletionMessage;
import ai.hanzo.model.OpenaiChatCompletionRequest;
import ai.hanzo.model.PostChat200Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
        // Exactly what was set: an unset list or map is absent, not `[]` or `{}`,
        // which the gateway refuses.
        assertEquals(java.util.Set.of("model", "messages"), body.keySet());
        assertEquals(java.util.Set.of("role", "content"),
                body.getAsJsonArray("messages").get(0).getAsJsonObject().keySet());
        assertEquals("zen5", body.get("model").getAsString());
        assertEquals("hi", body.getAsJsonArray("messages").get(0).getAsJsonObject().get("content").getAsString());
        assertEquals("zen5", reply.getModel());
        assertEquals("hello", reply.getChoices().get(0).getMessage().getContent());
    }

    @Test
    void sendsADecisionWithItsQuestion() throws Exception {
        // A reply in the shape /v1/decisions answers with, captured live.
        server.enqueue(json("{\"id\":\"dec_1\",\"model\":\"kai\",\"provider\":\"Hanzo\",\"answers\":{\"move\":{\"type\":\"choice\",\"choice\":\"b\",\"confidence\":0.5,\"probabilities\":{\"a\":0.25,\"b\":0.75}}},\"usage\":{\"input_tokens\":10,\"output_tokens\":0},\"routing\":{\"backend\":\"kai\",\"checkpoint\":\"kai-1.10\",\"sha256\":\"8069bf5cc5e897ce28399d3a5d2a17097e3859d85e5a4cda50c7f19f1e60e9f4\",\"representation\":\"6e386752d1525bf8ea0d90f6a82ec89091b32514ccbc7cb5acf5ac332a95f613\",\"calibration\":\"cal_c6ffe2e7b0c8819a\",\"device\":\"cpu\",\"reach\":32768,\"trained\":1024,\"positions\":8192,\"extrapolated\":false,\"reason\":\"explicit model='kai'\"},\"state_hash\":\"sha256:8770c944ed8823932f7b488043302076da0aec08b9bc4ea8506cf3ea7d0bdbc5\",\"latency_ms\":67.436068}"));

        AiDecisionsResponse reply = new AiApi(client("hk-test", null)).postDecisions(new AiDecisionsRequest()
                .model("kai")
                .state(Map.of("board", "x"))
                .putQuestionsItem("move", new AiDecisionsQuestion(new AiDecisionsChoice()
                        .type(AiDecisionsChoice.TypeEnum.CHOICE)
                        .criteria(Map.of("a", "first", "b", "second")))));

        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/decisions", req.getPath());
        JsonObject body = JsonParser.parseString(req.getBody().readUtf8()).getAsJsonObject();
        assertEquals("kai", body.get("model").getAsString());
        assertEquals("x", body.getAsJsonObject("state").get("board").getAsString());
        JsonObject move = body.getAsJsonObject("questions").getAsJsonObject("move");
        assertEquals("choice", move.get("type").getAsString());
        assertEquals("second", move.getAsJsonObject("criteria").get("b").getAsString());
        assertEquals("b", reply.getAnswers().get("move").getChoice());
        assertEquals(0, new java.math.BigDecimal("0.75").compareTo(reply.getAnswers().get("move").getProbabilities().get("b")));
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
