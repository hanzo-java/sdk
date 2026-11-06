package ai.hanzo.examples;

import ai.hanzo.Hanzo;
import ai.hanzo.ApiException;
import ai.hanzo.api.BillingApi;

/**
 * money — the balance, then the usage that moved it.
 *
 * <p>Operations: {@code get_billing_balance} — GET /v1/billing/balance, and
 * {@code get_billing_usage} — GET /v1/billing/usage.
 *
 * <p>Neither takes an org: both derive the tenant server-side from the token's
 * {@code owner} claim, so a key can only ever read its own money.
 *
 * <p>Both are published with no response schema, so the generator types them
 * {@code void}: there is no accessor to print a balance with, and no query
 * parameter to narrow the ledger with, because the document declares neither.
 * The example reports the status each route gave, which is all the document
 * supports. Decoding a body the client was never told the shape of would be an
 * opinion about the API living inside a generated client — the drift these SDKs
 * exist to prevent. When the source declares the shapes, a regeneration prints
 * them.
 *
 * <pre>
 *   HANZO_API_KEY=hk-... ./gradlew :examples:money
 * </pre>
 */
public final class Money {

    public static void main(String[] args) {
        BillingApi billing = new BillingApi(Hanzo.client());
        try {
            System.out.printf("balance  HTTP %d%n", billing.getBillingBalanceWithHttpInfo().getStatusCode());
            System.out.printf("usage    HTTP %d%n", billing.getBillingUsageWithHttpInfo().getStatusCode());
        } catch (ApiException e) {
            System.err.printf("billing failed: HTTP %d %s%n", e.getCode(), e.getResponseBody());
            System.exit(1);
        }
    }
}
