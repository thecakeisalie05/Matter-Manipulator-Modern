# Changelog

## 0.1.0-alpha.1 (development snapshot)

First compile-targeted Minecraft 1.21.1 / NeoForge implementation.

- Added one operator-only creative Mk III item and status/configuration commands.
- Captures block states and serializable block-entity NBT for rectangular structures (up to 8,192 blocks, with a serialized data size cap).
- Server-side paste with a safe default (no overwrite), optional air copying, preflight, bounded batches, and best-effort rollback.
- Added quarter-turn rotation and local-X mirroring for non-machine blocks. Modded machines with block entities cannot yet be rotated.
- Reproduces the original GTNH Enderman teleport building sound cue with an existing modern Minecraft sound.
- Added LGPL-3.0 attribution, build CI, compatibility matrix, and transform unit tests.

**Known gaps:** No verified FTB Evolution pack boot; no survival resource accounting, AE2-specific network adaptation, UI, cut/move, geometry tools, uplink, or full GTNH Mk III parity. This is not a production release.
