# pve-kotlin

Kotlin SDK for the Proxmox Virtual Environment (PVE) API. Generated
from the upstream `apidoc.js` via [openapi-generator-cli][gen] with
custom Mustache template overrides.

> **Not an official Proxmox project.** Community SDK derived from the
> upstream `apidoc.js`. Always verify against
> <https://pve.proxmox.com/pve-docs/api-viewer/>.

Targets the JVM via OkHttp 4 + Moshi. Requires JDK ≥ 11.

## Install

Maven Central / GitHub Packages publication is set up in this repo's
GitHub Actions workflow; pull the artifact via Gradle:

```kotlin
dependencies {
    implementation("dev.clientapi.pve:pve-kotlin:0.1.0")
}
```

Or build locally:

```bash
./gradlew build
./gradlew publishToMavenLocal
```

## Usage

```kotlin
import dev.clientapi.pve.Pve
import dev.clientapi.pve.infrastructure.ApiClient

// Configure the shared OkHttp + auth headers (set once, reused everywhere).
ApiClient.apiKey["Authorization"] = "PVEAPIToken=user@realm!tokenid=uuid-secret"

val pve = Pve(basePath = "https://pve1.example.com:8006/api2/json")

// Per-tag accessors are lazily instantiated and share the same basePath + Call.Factory.
val status = pve.qemu().qemuVmStatus(node = "pve1", vmid = 100L)
val nodes  = pve.nodes().nodesIndex()
```

The unified `Pve` class wraps each per-tag API class (`QemuApi`,
`LxcApi`, `ClusterApi`, `NodesApi`, …) so consumers don't need to
instantiate them individually.

## Compound configs

PVE encodes many fields as CLI-style shorthand strings
(`net0=virtio,bridge=vmbr0,firewall=1`). Round-trip helpers will be
emitted for every compound config schema in a future iteration. For
now, build the string manually and pass through the relevant API.

## Indexed families

Numbered properties (`net0..net31`, `mp0..mp255`, …) are exposed via
`getNets()` / `withNets(map)` extension functions on every model.
The per-index data class fields are annotated
`@Deprecated(level = DeprecationLevel.HIDDEN)`, so direct access is a
compile error and IDE autocomplete only surfaces the collapsed view:

```kotlin
val req = QemuCreateVmRequest().withNets(mapOf(
    0 to PveQemuNetField(string = "virtio,bridge=vmbr0"),
    3 to PveQemuNetField(string = "e1000,bridge=vmbr1"),
))
// Wire format: { "net0": "virtio,bridge=vmbr0", "net3": "e1000,bridge=vmbr1" }
```

## License

Apache 2.0 — see [LICENSE](./LICENSE).

[gen]: https://openapi-generator.tech
