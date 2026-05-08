// Example: resilient terminal session with auto-reconnect.
//
// Run with:
//
//   PVE_HOST=https://pve.example.com:8006 \
//   PVE_TOKEN='PVEAPIToken=root@pam!auto=...' \
//   PVE_NODE=orca PVE_VMID=100 \
//   ./gradlew run -PmainClass=examples.ResilientTerminalKt
package examples

import dev.clientapi.pve.RetryOptions
import dev.clientapi.pve.TerminalTarget
import dev.clientapi.pve.connectTerminalResilient
import dev.clientapi.pve.infrastructure.ApiClient

fun main() {
    val host = System.getenv("PVE_HOST") ?: "https://localhost:8006"
    ApiClient.apiKey["Authorization"] = System.getenv("PVE_TOKEN") ?: ""

    val node = System.getenv("PVE_NODE") ?: "pve1"
    val vmid = (System.getenv("PVE_VMID") ?: "100").toInt()
    val baseUrl = "$host/api2/json"

    val session = connectTerminalResilient(
        baseUrl = baseUrl,
        target = TerminalTarget.Qemu(node = node, vmid = vmid),
        retry = RetryOptions(maxRetries = 20, initialDelayMs = 250),
        onMessage = { print(it) },
        onClose = { code, _ -> println("\n[final close: $code]") },
        onReconnect = { attempt -> println("\n[reconnected after $attempt attempts]") },
        onGiveUp = { err -> System.err.println("\n[retries exhausted: $err]") },
    )

    session.send("date\n")
    val deadline = System.currentTimeMillis() + 5 * 60 * 1000L
    while (System.currentTimeMillis() < deadline) {
        Thread.sleep(30_000)
        session.send("date\n")
    }
    session.close()
}
