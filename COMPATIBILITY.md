# FTB Evolution compatibility policy

**Target minimum pack version: 1.43.1 (Aug 27, 2026)**.
Minecraft 1.21.1 and NeoForge are shared across this and later 1.4x releases.
The Gradle build uses NeoForge **21.1.228**, provisionally selected as the older API baseline.
This loader version must still be checked against the **actual 1.43.1 installed manifest** before declaring compatibility.

| Pack version | Minecraft | Loader | Status |
| --- | --- | --- | --- |
| Evolution 1.43.1 | 1.21.1 | NeoForge | **Targeted, NOT tested** |
| Evolution 1.44.0 | 1.21.1 | NeoForge | **Targeted, NOT tested** |
| Evolution 1.45.0 | 1.21.1 | NeoForge 21.1.250 | **Targeted, NOT tested** |

## Compatibility release gate

- [ ] Build JAR with Java 21 and minimum 1.43.1 pack loader
- [ ] Boot clean singleplayer worlds in 1.43.1 and 1.45.0
- [ ] Boot dedicated servers in both pack versions; verify both-sided mod installation
- [ ] Confirm registration of Mk III item, language, recipe, sound cue
- [ ] Test 3D blockstate orientation (stairs, rails, multipart)
- [ ] Test nested contents, tanks, custom data components and access control
- [ ] Test AE2: terminals, cables/facades, patterns, upgrades, cells, color, P2P networks
- [ ] Test Mekanism, Modern Industrialization, Create, Ender IO, Oritech, Sophisticated Storage, FTB permissions
- [ ] Test across a restarted world, modpack update and copy between dimensions
- [ ] Prevent item/fluid/power duplication in survival
- [ ] Add rollbacks, previews, missing-material analysis and server-side permission checks
- [ ] Confirm mod dependencies installed on both versions (including optional AE2 integrations)

**No finished release is asserted or implied by this matrix.**

## Installation

When released, add the same tested mod JAR to the `mods/` directories of both the client and server. Maintain version-specific testing records. Never test raw machine-copy snapshots on an irreplaceable save.
