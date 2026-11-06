package ai.hanzo.examples;

import ai.hanzo.Hanzo;
import ai.hanzo.ApiException;
import ai.hanzo.api.ToolApi;
import ai.hanzo.model.ToolTool;
import ai.hanzo.model.ToolToolList;

import java.util.List;

/**
 * tools — list the tools this key can reach.
 *
 * <p>Operation: {@code get_tool} — GET /v1/tool.
 *
 * <p>The catalogue is per-key: it composes the typed product operations with
 * whatever external servers the caller's org has enabled, so two keys in two
 * orgs see different lists. {@code activated} says which of them this key may
 * actually dispatch, and {@code source} says where each one comes from —
 * printing both is the difference between a catalogue and a menu.
 *
 * <p>(POST /v1/mcp is the JSON-RPC door onto the same catalogue and answers 200
 * with the tool list, but the document does not declare it, so it is not a
 * generated method. GET /v1/tool is the REST view that is.)
 *
 * <pre>
 *   HANZO_API_KEY=hk-... ./gradlew :examples:tools
 * </pre>
 */
public final class Tools {

    public static void main(String[] args) {
        ToolApi tools = new ToolApi(Hanzo.client());

        try {
            ToolToolList catalog = tools.getTool(null, null);
            List<ToolTool> listed = catalog.getTools();
            if (listed == null || listed.isEmpty()) {
                System.out.println("no tools reachable with this key");
                return;
            }
            for (ToolTool tool : listed) {
                System.out.printf("%-32s %-12s %s%n",
                        tool.getName(),
                        tool.getSource(),
                        Boolean.TRUE.equals(tool.getActivated()) ? "activated" : "available");
            }
            System.out.printf("%n%d tools%n", listed.size());
        } catch (ApiException e) {
            System.err.printf("tools failed: HTTP %d %s%n", e.getCode(), e.getResponseBody());
            System.exit(1);
        }
    }
}
