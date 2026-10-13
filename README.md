# Matter Manipulator Modern

An independently maintained **Minecraft 1.21.1 / NeoForge** remake inspired by RecursivePineapple's [MatterManipulator](https://github.com/RecursivePineapple/MatterManipulator) for GTNH.

**Version: 0.1.0-alpha.1 — first compile-tested development build.**

> This is not a full GTNH Mk III port. It is a limited, **creative/operator-only** block-and-machine snapshot tool. It has not been gameplay-tested inside FTB Evolution, and no production-world or survival compatibility is claimed.

## What works in the first alpha

- Mark opposite corners; capture rectangular builds up to **8,192 blocks**.
- Capture block IDs, block states (including typical orientation properties), and serializable **block-entity data** (machine settings, inventories, fluid tanks, and similar fields *when the mod serializes them*).
- Paste with a **non-destructive default**: existing blocks are never overwritten unless explicitly enabled.
- Optional paste of air blocks to reproduce empty spaces.
- Rotate and mirror **ordinary blocks** in 90° increments.
- **Refuse rotations/mirroring when block entities are present**, rather than pretending to safely transform machine-specific orientations.
- Perform server-side preflight, time-sliced placement (64 operations/tick) and best-effort rollback on runtime failure.
- Original GTNH sound-cue behavior: Enderman teleport sound at beginning/end of a build, using Minecraft's current built-in sound event.
- Java 21 / NeoForge build pipeline with unit tests for coordinate transforms.

## Install (development build only)

1. Download the JAR **artifact** from a successful GitHub Actions build (Actions > Build (Minecraft 1.21.1) > latest successful run > artifact).
2. Add the JAR to the client and, when playing on a dedicated server, the server's \`mods/\` folder.
3. On a backed-up **test world**, enable creative mode with operator permission level 2+.
4. Use \`/give @p matter_manipulator_modern:matter_manipulator_mk3\`.

**Support target:** FTB Evolution **1.43.1 onward** on Minecraft **1.21.1**. The project is built against NeoForge 21.1.228 as a conservative compile baseline. **No claim that this is already verified in 1.43.1 or newer packs.** See [COMPATIBILITY.md](COMPATIBILITY.md).

## Controls

| Action | Control |
| --- | --- |
| First corner | Sneak + right-click a block with the manipulator |
| Second corner + capture | Sneak + right-click the opposite corner |
| Paste | Normal right-click a block; the destination origin is the adjacent face |
| Check settings | \`/mmm status\` |
| Rotate clockwise/counterclockwise | \`/mmm rotate cw\`, \`/mmm rotate ccw\` |
| Reset rotation | \`/mmm rotate none\` |
| Mirror across local X | \`/mmm mirror on\` or \`/mmm mirror off\` |
| Allow destructive replacement | \`/mmm overwrite on\` or \`/mmm overwrite off\` |
| Include empty air spaces | \`/mmm air on\` or \`/mmm air off\` |
| Forget blueprint and corner selection | \`/mmm reset\` |

**Default:** \`overwrite=off\`, \`paste-air=off\`, \`mirror=off\`, \`rotation=0°\`.

## Limitations and safety

- Do **not** test on your main world without a backup.
- Pure data copying cannot safely guarantee complete modded machine equivalence. Owner UUIDs, AE2 network membership and P2P frequencies, linked inventories, external storage and cross-dimensional targets may require specialized adapters and reconnection. Copied item/fluid contents can be duplicated in creative mode.
- Large blocks or particularly complex machines may exceed the blueprint data-size cap, and captures currently live in the tool's item data (not a reusable blueprint file).
- Pasting is not a true atomic transaction: rollback is **best effort**, and updates/events triggered by modded machines may not be reversible.
- **Not included yet:** GTNH radial menu, hologram previews, geometry tools, cable routing, exchange mode, cut/move, survival economy, AE2 resource sourcing/crafting, uplink, and generalized machine rotation/adapters.
- Original sound *cues* are reproduced using installed Minecraft sounds. No proprietary OGG files are redistributed.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full-port roadmap.

## Build

Java 21 and Gradle 9.2.1:

\`\`\`bash
gradle build
\`\`\`

The CI build produces \`build/libs/matter_manipulator_modern-*.jar\`. The mod does not depend on the GTNH libraries.

## License and attribution

Project licensed **LGPL-3.0**; see [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md). Original MatterManipulator concept/source: **RecursivePineapple and contributors**. The separate [ModernManipulator](https://github.com/Raishxn/ModernManipulator) Forge 1.20.1 port is credited as a reference but is not a drop-in replacement for NeoForge 1.21.1.

Independent project; not affiliated with FTB, Mojang, or the GTNH developers.
