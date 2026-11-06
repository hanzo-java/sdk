# Hanzo Cloud — Java SDK

Java client for the [Hanzo Cloud](https://hanzo.ai) API, covering every `/v1`
route the gateway serves. It is generated from the API's own OpenAPI document —
the one published at
[`/v1/openapi.json`](https://api.hanzo.ai/v1/openapi.json) — so method names are
that document's operation ids camel-cased: `get_account_keys` → `getAccountKeys`,
`get_kv_by_bucket_by_key` → `getKvByBucketByKey`.

[`.spec-lock`](.spec-lock) names the ref and the digest of the bytes this client
was cut from.

## Install

On [Maven Central](https://central.sonatype.com/artifact/ai.hanzo/hanzo-java-cloud).
Java 11 or newer. Every cloud release cuts a patch, so both lines below take the
newest one of the 8.5 line:

```groovy
repositories { mavenCentral() }

dependencies { implementation 'ai.hanzo:hanzo-java-cloud:8.5.+' }
```

```xml
<dependency>
  <groupId>ai.hanzo</groupId>
  <artifactId>hanzo-java-cloud</artifactId>
  <version>[8.5,8.6)</version>
</dependency>
```

To build this tree instead, `./gradlew :hanzo-java-cloud:publishToMavenLocal`
writes the same coordinates, with sources and javadoc, to `~/.m2`.

## Authenticate

The document declares one security scheme — `bearer` — and applies it to every
operation but the few it marks open. `setBearerToken` is the one place a token
goes in:

```java
ApiClient hanzo = new ApiClient();
hanzo.setBearerToken(token);          // Authorization: Bearer …
new AiApi(hanzo).getModels();         // answers with or without a token
```

[`ai.hanzo.Hanzo`](hanzo-java-cloud/src/main/java/ai/hanzo/Hanzo.java) is the
same three lines fed from the environment, and is the only place in the module
that reads it.

| variable | meaning |
| --- | --- |
| `HANZO_API_KEY` | bearer credential, handed to `setBearerToken` |
| `HANZO_BASE_URL` | gateway to talk to; default `https://api.hanzo.ai` |
| `HANZO_ORG_ID` | org scope, sent as `X-Org-Id`; the KV and agents routes refuse without it. Everything else takes the tenant from the token's `owner` claim |

For a program that serves more than one tenant, pass them instead of reading a
single set of variables: `Hanzo.client(token, null, "acme")`.

The token is a Cloud API key, or an access token from Hanzo IAM — which is what
a service holding client credentials mints for itself:

```bash
export HANZO_API_KEY=$(curl -s https://api.hanzo.ai/v1/iam/oauth/token \
  -d grant_type=client_credentials -d client_id="$CLIENT_ID" -d client_secret="$CLIENT_SECRET" \
  | jq -r .access_token)
```

`X-Org-Id` stays a default header rather than a second credential: it selects a
tenant, and no scheme declares it.

## Quickstart

```java
import ai.hanzo.Hanzo;
import ai.hanzo.ApiClient;
import ai.hanzo.ApiException;
import ai.hanzo.api.AccountApi;
import ai.hanzo.model.AccountApiKey;

import java.util.List;
import java.util.Objects;

public class Whoami {
    public static void main(String[] args) throws ApiException {
        ApiClient hanzo = Hanzo.client();

        List<AccountApiKey> keys = Objects.requireNonNullElse(new AccountApi(hanzo).getAccountKeys().getKeys(), List.of());
        keys.forEach(key -> System.out.println(key.getType() + " " + key.getPrefix()));
    }
}
```

```bash
HANZO_API_KEY=… ./gradlew :examples:hello   # the same call, in this repo
```

`GET /v1/account/keys` refuses without a credential — `403
{"code":"forbidden","detail":"sign in to manage API keys","status":403,…}` — so
reaching the loop at all proves the key works.

Getters carry the document's own answer about a field:
`@javax.annotation.Nullable` unless it is required, which most are not — hence
the `requireNonNullElse`. A refusal arrives as a checked `ApiException` carrying
`getCode()` and `getResponseBody()`.

## Examples

Six flows, one per directory, each a complete program. `flows.yaml` in
hanzoai/openapi prescribes them for every Hanzo SDK, so a reader who knows one
language's set can find their way around another's. The build compiles them
against the client.

| flow | what it does |
|---|---|
| [`hello`](examples/hello) | `GET /v1/account/keys` — prove the key works |
| [`chat`](examples/chat) | `POST /v1/chat/completions` — one completion |
| [`money`](examples/money) | `GET /v1/billing/balance`, `GET /v1/billing/usage` |
| [`store`](examples/store) | `POST /v1/provisioning/kv`, then `GET` and `DELETE /v1/provisioning/kv/{name}` |
| [`agent`](examples/agent) | `POST /v1/agent`, `.../run`, poll `.../runs` until terminal |
| [`tools`](examples/tools) | `GET /v1/tool` — the tools this key can reach |

One command each, against the live gateway:

```bash
export HANZO_API_KEY=…
export HANZO_ORG_ID=my-org      # store and agent only
./gradlew :examples:hello       # or tools, money, chat, store, agent
```

```
$ ./gradlew --console=plain -q :examples:hello
the key is good, and it owns no keys of its own
$ ./gradlew --console=plain -q :examples:tools
no tools reachable with this key
$ ./gradlew --console=plain -q :examples:money
balance  HTTP 200
usage    HTTP 200
```

`agent` asks for `zen5`; `HANZO_MODEL` overrides it, and
`curl https://catalog.hanzo.ai/v1/models` lists the rest.

`money` prints a status rather than a body: those routes are published with no
response schema, so the generated methods return `void`. `chat` is typed — it
sends an `OpenaiChatCompletionRequest` and prints `choices[0].message.content`.

## Build

```bash
./gradlew build                          # client + examples
./gradlew :hanzo-java-cloud:assemble     # the jar
```

## Regenerate

Nothing under `hanzo-java-cloud/src/main/java/ai/hanzo` but `Hanzo.java` is written by
hand; to change it, change the code that emits the document.

```bash
SPEC=/path/to/openapi.yaml OPENAPI=/path/to/hanzoai/openapi ./scripts/generate.sh
```

`scripts/generate.sh` is a call site: the invocation lives once in
`hanzoai/openapi/generate.py`, and every knob — generator, HTTP library,
coordinates, packages — is data in `sdks.yaml` beside it.

## Docs

[docs.hanzo.ai](https://docs.hanzo.ai) for the API itself.
[`/v1/openapi.json`](https://api.hanzo.ai/v1/openapi.json) is the document this
client is cut from.

## License

Apache-2.0. See [LICENSE](LICENSE).
