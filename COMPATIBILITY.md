# FTB Evolution compatibility policy

Minimum **intended** pack version: **FTB Evolution 1.43.1** (2026-08-27). This target is a release requirement, not a passed certification. Both 1.43.1 and 1.45.0 are Minecraft 1.21.1 / NeoForge. FTB Evolution 1.45.0 explicitly lists NeoForge 21.1.250; the exact NeoForge version used by 1.43.1 has **not been independently verified**.

Build against conservative **NeoForge 21.1.228**; confirm 1.43.1's actual runtime and API behavior before publishing compatibility claims.

| FTB Evolution | Minecraft | NeoForge | Verification |
| --- | --- | --- | --- |
| **1.43.1** | 1.21.1 | exact pack version to confirm | Target, **not tested** |
| 1.44.0 | 1.21.1 | exact pack version to confirm | Target, **not tested** |
| 1.45.0 | 1.21.1 | 21.1.250 | Target, **not tested** |

## Release validation gates

- [x] Java 21 Gradle project builds with a NeoForge 21.1.x baseline (previous prototype passed, latest CI status on PR)
- [ ] Unit tests pass on the alpha branch
- [ ] Load into clean vanilla + NeoForge 1.21.1 client
- [ ] Load into dedicated NeoForge 1.21.1 server
- [ ] **Boot FTB Evolution 1.43.1 client AND dedicated server**
- [ ] Boot 1.45.0 (and relevant intervening pack versions)
- [ ] Verify blockstate/BE capture, restart, paste and undo behavior
- [ ] Round-trip Modern Industrialization, AE2, Mekanism, Create, Oritech, Ender IO and other FTB Evo devices without broken links or item duplication
- [ ] Confirm protections, claim regions, huge blueprint bounds and packet-size behavior
- [ ] Verify machine-specific rotation and network handling (not in this alpha)
- [ ] Implement survival material accounting and network sourcing (not in this alpha)

**Until validation succeeds:** alpha source and built artifacts are offered for development/testing only.
