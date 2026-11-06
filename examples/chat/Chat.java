package ai.hanzo.examples;

import ai.hanzo.Hanzo;
import ai.hanzo.ApiException;
import ai.hanzo.api.AiApi;
import ai.hanzo.model.OpenaiChatCompletionChoice;
import ai.hanzo.model.OpenaiChatCompletionMessage;
import ai.hanzo.model.OpenaiChatCompletionRequest;
import ai.hanzo.model.PostChat200Response;

import java.util.List;

/**
 * chat — one completion.
 *
 * <p>Operation: {@code post_chat_completions} — POST /v1/chat/completions,
 * the gateway's own inference route.
 *
 * <p>Non-streaming on purpose: streaming is SSE, a different transport that a
 * generated client hands back as an opaque body, so demonstrating it here would
 * teach the wrong shape.
 *
 * <p>The document types the route — {@code OpenaiChatCompletionRequest} in,
 * {@code PostChat200Response} out (the completion plus the {@code routing} the
 * gateway chose) — so the prompt goes in as a value
 * and the reply is {@code choices[0].message.content}.
 *
 * <pre>
 *   HANZO_API_KEY=hk-... ./gradlew :examples:chat
 * </pre>
 */
public final class Chat {

    /** A model the gateway serves. {@code HANZO_MODEL} overrides it. */
    private static final String DEFAULT_MODEL = "zen5";

    public static void main(String[] args) {
        AiApi ai = new AiApi(Hanzo.client());
        String model = System.getenv("HANZO_MODEL");
        if (model == null || model.trim().isEmpty()) {
            model = DEFAULT_MODEL;
        }
        try {
            // The two nulls are the optional X-Max-Cost and X-Max-Latency-Ms
            // routing bounds; unset, the gateway routes without a ceiling.
            PostChat200Response reply = ai.postChatCompletions(new OpenaiChatCompletionRequest()
                    .model(model)
                    .messages(List.of(new OpenaiChatCompletionMessage()
                            .role("user")
                            .content("Say hello in one short sentence."))), null, null);
            List<OpenaiChatCompletionChoice> choices = reply.getChoices();
            String content = choices == null || choices.isEmpty() || choices.get(0).getMessage() == null
                    ? ""
                    : choices.get(0).getMessage().getContent();
            System.out.printf("model    %s%n", reply.getModel());
            System.out.printf("reply    %s%n", content);
        } catch (ApiException e) {
            System.err.printf("chat failed: HTTP %d %s%n", e.getCode(), e.getResponseBody());
            System.exit(1);
        }
    }
}
