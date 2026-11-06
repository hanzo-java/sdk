package ai.hanzo.examples;

import ai.hanzo.Hanzo;
import ai.hanzo.ApiException;
import ai.hanzo.api.AccountApi;
import ai.hanzo.model.AccountApiKey;
import ai.hanzo.model.AccountApiKeyList;

import java.util.List;

/**
 * hello — prove the key works, and print what it can reach.
 *
 * <p>Operation: {@code get_account_keys} — GET /v1/account/keys.
 *
 * <p>This is the call that says no. With no key, or a bogus one, the route
 * answers 403 {@code {"status":403,"code":"forbidden","error":"sign in to
 * manage API keys"}} while GET /v1/account/keys-zzq9 answers 404 — so the refusal is
 * this route refusing rather than a wildcard door, which is what makes it a
 * usable proof that a credential works. The three obvious identity routes were
 * disqualified for answering 200 to a caller with no credential at all;
 * {@code flows.yaml} records the probe.
 *
 * <pre>
 *   HANZO_API_KEY=hk-... ./gradlew :examples:hello
 * </pre>
 */
public final class Hello {

    public static void main(String[] args) {
        AccountApi keys = new AccountApi(Hanzo.client());
        try {
            AccountApiKeyList mine = keys.getAccountKeys();
            List<AccountApiKey> owned = mine.getKeys();
            if (owned == null || owned.isEmpty()) {
                System.out.println("the key is good, and it owns no keys of its own");
                return;
            }
            for (AccountApiKey key : owned) {
                System.out.printf("%-8s %-24s %s%n", key.getType(), key.getPrefix(), key.getCreated());
            }
        } catch (ApiException e) {
            System.err.printf("keys refused: HTTP %d %s%n", e.getCode(), e.getResponseBody());
            System.exit(1);
        }
    }
}
