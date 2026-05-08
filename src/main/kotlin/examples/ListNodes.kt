// Example: list cluster nodes.
//
// Run with:
//
//   PVE_HOST=https://pve.example.com:8006 \
//   PVE_TOKEN='PVEAPIToken=root@pam!auto=...' \
//   ./gradlew run -PmainClass=examples.ListNodesKt
//
// Or compile + run with kotlin CLI directly.
package examples

import dev.clientapi.pve.Pve
import dev.clientapi.pve.infrastructure.ApiClient

fun main() {
    val host = System.getenv("PVE_HOST") ?: "https://localhost:8006"
    ApiClient.apiKey["Authorization"] = System.getenv("PVE_TOKEN") ?: ""

    val pve = Pve(basePath = "$host/api2/json")
    val response = pve.nodes().nodesGetNodes()
    val nodes = response.data ?: emptyList()
    println("Found ${nodes.size} node(s):")
    for (n in nodes) {
        println("  - ${n.node} (status=${n.status}, cpu=${n.cpu}, mem=${n.mem}/${n.maxmem})")
    }
}
