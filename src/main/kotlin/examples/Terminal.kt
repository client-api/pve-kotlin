// Example: open a terminal session against a QEMU VM.
//
// Run with:
//
//   PVE_HOST=https://pve.example.com:8006 \
//   PVE_TOKEN='PVEAPIToken=root@pam!auto=...' \
//   PVE_NODE=orca PVE_VMID=100 \
//   ./gradlew run -PmainClass=examples.TerminalKt
package examples

import dev.clientapi.pve.TerminalTarget
import dev.clientapi.pve.connectTerminal
import dev.clientapi.pve.infrastructure.ApiClient

fun main() {
    val host = System.getenv("PVE_HOST") ?: "https://localhost:8006"
    ApiClient.apiKey["Authorization"] = System.getenv("PVE_TOKEN") ?: ""

    val node = System.getenv("PVE_NODE") ?: "pve1"
    val vmid = (System.getenv("PVE_VMID") ?: "100").toInt()
    val baseUrl = "$host/api2/json"

    println("Opening terminal on $node:qemu/$vmid...")
    val session = connectTerminal(
        baseUrl = baseUrl,
        target = TerminalTarget.Qemu(node = node, vmid = vmid),
        onMessage = { print(it) },
        onClose = { code, reason -> println("\n[closed: $code $reason]") },
        onError = { e -> System.err.println("\n[error: $e]") },
    )

    session.resize(120, 32)
    session.send("uname -a\n")

    Thread.sleep(5_000)
    session.close()
}
