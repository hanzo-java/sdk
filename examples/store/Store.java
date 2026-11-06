package ai.hanzo.examples;

import ai.hanzo.Hanzo;
import ai.hanzo.ApiException;
import ai.hanzo.api.ProvisioningApi;
import ai.hanzo.model.ProvisioningProvisionRequest;
import ai.hanzo.model.ProvisioningProvisionResult;
import ai.hanzo.model.ProvisioningProvisionedResource;

import java.util.UUID;

/**
 * store — provision a KV store, read it back, drop it.
 *
 * <p>Operations: {@code post_kv} — POST /v1/kv,
 * {@code get_kv_by_name} — GET /v1/kv/{name},
 * {@code delete_kv_by_name} — DELETE /v1/kv/{name}.
 *
 * <p>This is the provisioning plane — make a store, read it, drop it — and it
 * is all there is: the document declares {@code /v1/kv} and {@code /v1/kv/{name}}
 * and nothing else, because that is what apps/provisioning registers. The value
 * plane (/v1/kv/keys/{key}) was authored in a hand-written spec that is gone,
 * and the addresses went with it.
 *
 * <p>KV is org-scoped — without {@code X-Org-Id} every call here is
 * 403 {@code "X-Org-Id required"}. The delete is in a {@code finally} so a
 * failed read still cleans up instead of leaving a store behind for the next
 * run to collide with.
 *
 * <pre>
 *   HANZO_API_KEY=hk-... HANZO_ORG_ID=my-org ./gradlew :examples:store
 * </pre>
 */
public final class Store {

    public static void main(String[] args) {
        ProvisioningApi kv = new ProvisioningApi(Hanzo.client());

        // Names are org-unique, so the example makes a fresh one rather than
        // colliding with whatever the last run left.
        String name = "sdk-example-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            ProvisioningProvisionResult created = kv.postProvisioningKv(new ProvisioningProvisionRequest().name(name));
            System.out.printf("created  %s (%s)%n", created.getName(), created.getStatus());

            ProvisioningProvisionedResource read = kv.getProvisioningKvByName(name);
            System.out.printf("read     %s host=%s port=%s status=%s%n",
                    read.getName(), read.getHost(), read.getPort(), read.getStatus());
        } catch (ApiException e) {
            System.err.printf("kv failed: HTTP %d %s%n", e.getCode(), e.getResponseBody());
            System.exit(1);
        } finally {
            try {
                kv.deleteProvisioningKvByName(name);
                System.out.printf("deleted  %s%n", name);
            } catch (ApiException e) {
                System.err.printf("delete %s failed: HTTP %d %s%n", name, e.getCode(), e.getResponseBody());
            }
        }
    }
}
