package ai.hanzo;

/**
 * Endpoint, credential and tenant, resolved once.
 *
 * <p>Everything else in {@code ai.hanzo} is generated from the API document
 * hanzoai/cloud emits, by {@code scripts/generate.sh}. This file is not: it is
 * the hand-written entry point, and the only place in the module that reads the
 * environment. It sits beside the generated package rather than inside it
 * because {@code sdks.yaml} hands the generator {@code ai/hanzo} outright
 * — a file in there is deleted by the next regeneration.
 *
 * <p>The credential itself is the GENERATED client's, not this file's. The
 * document declares a {@code bearer} security scheme and applies it to every
 * operation except those that say {@code security: []}, so {@link ApiClient}
 * registers an {@code HttpBearerAuth} and each generated call names it; all this
 * does is hand it the token. The header goes out from
 * {@code HttpBearerAuth.applyToParams}, on the calls the document says need it
 * and not on the public ones. {@code X-Org-Id} is a default header because it is
 * a tenant selector rather than a credential: no scheme declares it and no
 * generated signature accepts it.
 *
 * <pre>
 *   ApiClient hanzo = Hanzo.client();
 *   new AccountApi(hanzo).getAccountKeys();
 * </pre>
 */
public final class Hanzo {

    /** The Hanzo gateway. Every route is {@code <base>/v1/<service>/...}. */
    public static final String DEFAULT_BASE_URL = "https://api.hanzo.ai";

    private Hanzo() {
    }

    /**
     * A client configured from the environment: {@code HANZO_API_KEY},
     * {@code HANZO_BASE_URL} (default {@link #DEFAULT_BASE_URL}) and
     * {@code HANZO_ORG_ID}.
     *
     * @return the client every generated API class takes
     */
    public static ApiClient client() {
        return client(env("HANZO_API_KEY"), env("HANZO_BASE_URL"), env("HANZO_ORG_ID"));
    }

    /**
     * A client for one tenant, for a program that serves more than one and so
     * cannot read a single set of variables.
     *
     * <p>A missing key sends no {@code Authorization} header rather than
     * raising: the API refusing is the truth, and inventing a client-side error
     * hides which routes need a credential and which do not. The public routes
     * still work without one.
     *
     * @param apiKey  bearer credential — an IAM access token or a Cloud API key;
     *                null or empty sends none
     * @param baseUrl gateway to talk to; null or empty means
     *                {@link #DEFAULT_BASE_URL}
     * @param orgId   org scope, sent as {@code X-Org-Id}; null or empty sends
     *                none, and the tenant comes from the token's {@code owner}
     *                claim
     * @return the client every generated API class takes
     */
    public static ApiClient client(String apiKey, String baseUrl, String orgId) {
        ApiClient client = new ApiClient();
        client.setBasePath(baseUrl == null || baseUrl.isEmpty() ? DEFAULT_BASE_URL : baseUrl);
        if (apiKey != null && !apiKey.isEmpty()) {
            client.setBearerToken(apiKey);
        }
        if (orgId != null && !orgId.isEmpty()) {
            client.addDefaultHeader("X-Org-Id", orgId);
        }
        return client;
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? null : value;
    }
}
