package ai.hanzo.examples;

import ai.hanzo.Hanzo;
import ai.hanzo.ApiException;
import ai.hanzo.api.AgentApi;
import ai.hanzo.model.AgentAgentRunView;
import ai.hanzo.model.AgentAgentView;
import ai.hanzo.model.AgentCreateAgentIn;
import ai.hanzo.model.AgentRunList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * agent — create one, run it, watch the run land.
 *
 * <p>Operations: {@code post_agent} — POST /v1/agent,
 * {@code post_agent_by_ref_run} — POST /v1/agent/{ref}/run,
 * {@code get_agent_by_ref_runs} — GET /v1/agent/{ref}/runs.
 *
 * <p>{@code ref} accepts the public id or the org-unique name, so the run and
 * the read both use the name just created. Names are org-unique, so this makes
 * a fresh one rather than hardcoding it.
 *
 * <p>The run route carries neither a body nor an acknowledgement in the
 * document, so the input the agent works on is its {@code instructions} and the
 * only evidence a run started is the RUN LIST. That is why the example records
 * the run ids that existed beforehand and polls until the one that appeared is
 * terminal — the last step is the list, not the agent read.
 *
 * <p>Agents are org-scoped: without {@code X-Org-Id} these are
 * 403 {@code "X-Org-Id required"}.
 *
 * <pre>
 *   HANZO_API_KEY=hk-... HANZO_ORG_ID=my-org ./gradlew :examples:agent
 * </pre>
 */
public final class Agent {

    /** Anything not still moving is done; the document does not enumerate the vocabulary. */
    private static final Set<String> RUNNING = Set.of("queued", "pending", "running", "in_progress", "starting");

    private static final int POLL_ATTEMPTS = 60;
    private static final long POLL_INTERVAL_MS = 1000L;

    /** The only flow that names a model. {@code HANZO_MODEL} overrides it. */
    private static final String DEFAULT_MODEL = "zen5";

    public static void main(String[] args) throws InterruptedException {
        AgentApi agents = new AgentApi(Hanzo.client());
        String name = "sdk-example-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            AgentAgentView created = agents.postAgent(new AgentCreateAgentIn()
                    .name(name)
                    .model(model())
                    .instructions("Answer in exactly one sentence: what is the capital of Japan?"));
            System.out.printf("created  %s (%s)%n", created.getName(), created.getId());

            Set<String> before = runIds(agents.getAgentByRefRuns(name, 100L));

            agents.postAgentByRefRun(name);
            System.out.printf("ran      %s%n", name);

            for (int attempt = 0; attempt < POLL_ATTEMPTS; attempt++) {
                AgentAgentRunView run = started(agents.getAgentByRefRuns(name, 100L), before);
                if (run != null && !RUNNING.contains(String.valueOf(run.getStatus()))) {
                    System.out.printf("run      %s %s%n", run.getId(), run.getStatus());
                    System.out.printf("output   %s%n", run.getOutput());
                    return;
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }
            System.err.printf("run did not finish within %ds%n", POLL_ATTEMPTS * POLL_INTERVAL_MS / 1000);
            System.exit(1);
        } catch (ApiException e) {
            System.err.printf("agent failed: HTTP %d %s%n", e.getCode(), e.getResponseBody());
            System.exit(1);
        }
    }

    private static String model() {
        String model = System.getenv("HANZO_MODEL");
        return model == null || model.trim().isEmpty() ? DEFAULT_MODEL : model;
    }

    private static Set<String> runIds(AgentRunList list) {
        Set<String> ids = new HashSet<>();
        List<AgentAgentRunView> runs = list == null ? null : list.getRuns();
        if (runs != null) {
            runs.forEach(run -> ids.add(run.getId()));
        }
        return ids;
    }

    /** The run this example started: the one that was not there before. */
    private static AgentAgentRunView started(AgentRunList list, Set<String> before) {
        List<AgentAgentRunView> runs = list == null ? null : list.getRuns();
        if (runs == null) {
            return null;
        }
        return runs.stream().filter(run -> !before.contains(run.getId())).findFirst().orElse(null);
    }
}
