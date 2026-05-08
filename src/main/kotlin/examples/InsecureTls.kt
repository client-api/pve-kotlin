// Example: connect to a Proxmox host with a self-signed certificate.
//
// The PVE web UI ships with a self-signed cert by default. Production
// setups should use a real CA-signed cert (Let's Encrypt via the
// Proxmox UI), but home-lab and dev setups commonly need to opt out
// of cert verification.
//
// **Security note:** disabling verification is vulnerable to MITM.
// Use only on trusted networks.
//
// Run with:
//
//   PVE_HOST=https://pve.example.com:8006 \
//   PVE_TOKEN='PVEAPIToken=root@pam!auto=...' \
//   PVE_NODE=orca PVE_VMID=100 \
//   ./gradlew run -PmainClass=examples.InsecureTlsKt
package examples

import dev.clientapi.pve.Pve
import dev.clientapi.pve.TerminalTarget
import dev.clientapi.pve.infrastructure.ApiClient
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Build an OkHttpClient that accepts any cert. Used for both REST
 * calls and the WebSocket upgrade — OkHttp drives both.
 */
private fun insecureClient(): OkHttpClient {
    val trustAll = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustAll), java.security.SecureRandom())
    }
    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAll)
        .hostnameVerifier { _, _ -> true }
        .build()
}

fun main() {
    val host = System.getenv("PVE_HOST") ?: "https://localhost:8006"
    ApiClient.apiKey["Authorization"] = System.getenv("PVE_TOKEN") ?: ""

    val client = insecureClient()

    // ── 1. REST: pass the insecure client to the Pve facade.
    val pve = Pve(basePath = "$host/api2/json", httpClient = client)
    val nodes = pve.nodes().nodesGetNodes().data ?: emptyList()
    println("Connected (insecure TLS): ${nodes.size} node(s)")

    // ── 2. WebSocket: `pve.connectTerminal(target)` reuses the same
    //    OkHttpClient already plumbed into the Pve facade — for both
    //    the termproxy POST (the REST leg) and the WS upgrade itself.
    val node = System.getenv("PVE_NODE") ?: "pve1"
    val vmid = (System.getenv("PVE_VMID") ?: "100").toInt()

    val session = pve.connectTerminal(
        target = TerminalTarget.Qemu(node = node, vmid = vmid),
        onMessage = { print(it) },
    )
    session.send("uname -a\n")
    Thread.sleep(3_000)
    session.close()
}
