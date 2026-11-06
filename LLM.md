# hanzo-java/sdk — notes for the next person in here

Home: `github.com/hanzo-java/sdk` (was `hanzoai/java-sdk`, which now redirects).
Checkout: `~/work/hanzo-java/sdk`.

One Gradle module, `hanzo-java-cloud`, published as `ai.hanzo:hanzo-java-cloud`.
`examples/` is a second module that exists only to be compiled against it.

## What is generated and what is not

`hanzo-java-cloud/src/main/java/ai/hanzo/cloud/**` is generated and is **deleted
and rewritten wholesale** on every regeneration — `sdks.yaml`'s `take:` hands
the generator that directory outright. Never edit a file in it; a hand edit is
caught by `generate.py --check` and fails CI.

Everything else is the repo's, including `ai/hanzo/Hanzo.java`, which sits
*beside* the generated package for exactly that reason.

## The shape, and how to measure it

```
find hanzo-java-cloud/src/main/java/ai/hanzo/cloud -name '*.java' | wc -l   # 3385
ls hanzo-java-cloud/src/main/java/ai/hanzo/cloud/api/*.java | wc -l         # 130
ls hanzo-java-cloud/src/main/java/ai/hanzo/cloud/model/*.java | wc -l       # 3238
```

Measured at hanzo-inc/cloud v8.5.988 (`a779b8b2`): the document (`openapi.yaml`,
pinned in `.spec-lock`) declares **1,877 paths, 2,603 operations, 130 tags,
3,146 schemas**. The numbers move with every release; the commands do not.
A method name is its operation id camel-cased: `get_kv_by_name` → `getKvByName`.

## The credential is generated now

The document declares a `bearer` scheme (HTTP bearer) and applies it to every
operation except those carrying `security: []` (22 at v8.5.988 — `/v1/openapi.json`,
`/v1/form/public/*`, `/v1/market/*`, …). So `ApiClient` registers an
`HttpBearerAuth`, and the client sends no credential where the document says
none is wanted. `HanzoTest.sendsNoCredentialWhereTheDocumentSaysNone` pins it.
`/v1/models` used to be public and no longer is; trust the document, not a list.

`ai.hanzo.Hanzo` did that job by hand until the scheme existed, setting
`Authorization` as a default header. It no longer does: it calls
`setBearerToken`, so there is one code path for the credential and it is the
generated one. What remains hand-written is the part no scheme covers — reading
the environment, the default base URL, and `X-Org-Id`, which selects a tenant
rather than authenticating one.

## Dependencies are what the sources import, nothing more

`jakarta.annotation-api` is on the **1.x** line and must stay there: every
generated type carries `@javax.annotation.Generated`, and the javax namespace
only exists in 1.x — 2.0 renames it to `jakarta.annotation` and 2,470 files stop
compiling. `jsr305` supplies `@Nullable`/`@Nonnull` and does not supply
`Generated`. Dropping either is a 2,600-error build.

## Publishing, and what it waits on

Central serves `ai.hanzo:hanzo-java-cloud:8.5.156` and nothing newer
(`repo1.maven.org/maven2/ai/hanzo/hanzo-java-cloud/maven-metadata.xml`). The
`ai.hanzo` namespace is verified — that upload proves it. Tags `v8.5.623` and
`v8.5.640` were cut by the client lane and never reached Central: `release.yml`
stopped at `MAVEN_CENTRAL_USERNAME is not in KMS (org hanzo, env prod)`.

**One credential is owed, and it is not this repo's to create:** a Central
Portal user token as `MAVEN_CENTRAL_USERNAME`/`MAVEN_CENTRAL_PASSWORD` and an
ASCII-armoured PGP key as `MAVEN_GPG_PRIVATE_KEY`/`MAVEN_GPG_PASSPHRASE`, in
KMS org `hanzo`, env `prod`. After that, the next push to main whose
`build.gradle` version Central does not serve publishes with no change here.

### Three traps this lane used to contain

**An unsigned upload used to report success.** `signing` was keyed on whether
`MAVEN_GPG_PRIVATE_KEY` was set, so with no key `sign` was never called, the
publish graph held no signing task at all, and the jars would have gone out
unsigned with `BUILD SUCCESSFUL` — Central refusing them hours later, out of
band. The signature now follows the destination
(`required = { gradle.taskGraph.hasTask(':hanzo-java-cloud:publishMavenPublicationToCentralRepository') }`),
so a keyless Central publish dies at `signMavenPublication` before a byte moves,
and `publishToMavenLocal` still needs no key.

**Uploading is not releasing.** Gradle's `maven-publish` is a series of PUTs
with nothing marking where one deployment ends, so Sonatype's OSSRH-compat
service separates them by source IP and waits to be told. Without a following
`POST /manual/upload/defaultRepository/ai.hanzo?publishing_type=automatic` the
artifacts sit in an open staging repository and never reach the Portal, let
alone Central — Sonatype's own words: *"you must follow this section or your
deployments will not be visible in the Central Publisher Portal."* That call is
the last step of `release.yml`, in the same job so it comes from the same IP.

**A step that measures failure cannot run under `-e`.** The forge invokes every
`run:` as `bash --noprofile --norc -e -o pipefail`, and `set -uo pipefail`
inside the script does not turn `-e` back off. The KMS step exists to find out
whether four secrets are there, so its curls are *expected* to 404 — under `-e`
the first one took the step at curl's exit 22 and none of its messages were ever
reached. Run 69709 reported a missing credential as `exitcode '22'`; run 69723,
after `set +e`, reports `exitcode '1'` and names `MAVEN_CENTRAL_USERNAME`. A
local rehearsal with plain `bash script.sh` cannot catch this — rehearse with
`bash -e -o pipefail script.sh`. `hanzoai/ci`'s `build.yml` carries the same
warning on its own KMS step.

### What is already proven

Everything except the two acts above. `ai.hanzo:hanzo-java-cloud:8.0.1` built,
installed to `~/.m2`, and resolved from a **separate project with an empty
`GRADLE_USER_HOME`** — the full transitive tree (okhttp, gson, okio, kotlin-stdlib)
came down from Central against the generated POM. The README quickstart, compiled
against that resolved jar, printed `GET /v1/keys answered; 0 key(s) owned` with
an IAM access token and `403 {"code":"forbidden"}` without one. The POM carries
every coordinate Central validates: name, description, url, licence, developer,
scm — and `github.com/hanzo-java/sdk` is public, so its urls resolve for a
reader. Signing was exercised with a throwaway key: five `.asc` files, and
`gpg --verify` returns `Good signature`.

## Building

```
./gradlew --no-daemon :hanzo-java-cloud:compileJava   # ~25s cold
./gradlew --no-daemon :examples:compileJava
```

Set `GRADLE_USER_HOME` and `TMPDIR` to somewhere with room. The compile is
forked with `-Xmx6g` (root `build.gradle`) because the Gradle daemon's heap is
not the compiler's, and 2,671 sources will not fit in the default.

## The six examples are not this repo's choice

They are data in `hanzoai/openapi` `flows.yaml` — a closed set every Hanzo SDK
owes — and `test_flows.py` there asserts both that the set is exactly those six
and that every operation id they name exists in the document. Adding a seventh
here breaks that.

`chat` and `money` print a status rather than a body: those routes are published
with no request or response schema, so the generated methods take no argument
and return `void`. Do not hand-roll a request to make them look better — that is
the drift these SDKs exist to prevent. When cloud declares the shapes, a
regeneration fixes them.

## Running one, and which credential answers

Compiling proves the client matches the document; only running proves it can
authenticate. `HANZO_API_KEY=… ./gradlew --console=plain -q :examples:hello`,
and unset it for the control — the pair is the proof.

Two kinds of token reach api.hanzo.ai and they do NOT behave alike:

- **An IAM access token** — `POST /v1/iam/oauth/token`, `grant_type=client_credentials`,
  with a client id and secret (services keep theirs in the k8s Secret
  `<app>-iam-creds`). Measured: `/v1/keys` 200 ten times out of ten.
- **A Cloud API key** (`sk-`). Measured on the same route in the same minute:
  **200 once in ten, 403 the other nine**, while `/v1/tools` and
  `/v1/billing/balance` answered 200 every time. The key is fine; the key→account
  lookup resolves on one replica and not the rest, and `/v1/billing/balance`
  makes that visible by answering 200 with `"account":"anonymous"`. A single
  403 from `hello` is therefore not evidence about the client. Repeat it, or
  use an IAM token, before concluding anything about this repo.
