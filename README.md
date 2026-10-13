# Matter Manipulator Modern

An independent **Minecraft 1.21.1 / NeoForge** modernization project, targeting **FTB Evolution** and inspired by the GTNH Matter Manipulator Mk III.

> **Status: pre-alpha. No playable release yet.** The original GTNH tool has not been fully ported. Do not install a development build on a valuable world without a backup.

## Goals

- All GTNH Mk III interaction modes: geometry (corner/edge/face/volume materials), cables, exchange, move, copy/paste, removal, transform, stack, and previews.
- Reproduce Mk III's original performance target of up to **1,024 placements/second**, with time-based server budget and adjustable throttling.
- Preserve every *portable* bit of build data: block identity and block states; orientation; block entity settings; fluid/item contents; machine-specific settings; AE2 parts, patterns, filters, and channels; network and security details wherever technically feasible.
- Integrate with AE2 storage and crafting, wireless power/network infrastructure, player inventory, and optional modded inventories through adapters.
- Preserve the original sound **cues** using present-day equivalent vanilla sounds, including the Enderman teleport sound for a manipulation burst.
- Make the mod suitable for sharing as a public LGPL-3.0 project, with source attribution, a public issue tracker, CI builds, and explicit compatibility reporting.

**Important:** No universal copier can promise perfect copies of *every* third-party machine or saved world link without integration-specific testing. Some IDs (security authorization, owner UUIDs, P2P frequencies, cross-dimensional coordinates, network identity) must be remapped, re-linked, or intentionally excluded. A clone must never unintentionally mint free items, fluids, power, or private network credentials.

## Source projects and credit

- [MatterManipulator by RecursivePineapple](https://github.com/RecursivePineapple/MatterManipulator) — the original GTNH implementation, LGPL-3.0.
- [ModernManipulator by Raishxn](https://github.com/Raishxn/ModernManipulator) — an existing Forge 1.20.1 port for GregTech CEu Modern; a reference only, **not** a drop-in 1.21.1 NeoForge replacement.
- Minecraft and FTB Evolution remain the intellectual property of their respective owners. This mod is independent and not endorsed by them.

See [COMPATIBILITY.md](COMPATIBILITY.md) and [ARCHITECTURE.md](ARCHITECTURE.md) for the validation plan.

## Development

- Minecraft **1.21.1**
- **NeoForge 21.1.x**
- Java **21**
- Gradle build via `gradle build` (project scaffold in development)

## License

The original MatterManipulator code is LGPL-3.0. This project's source is distributed under **LGPL-3.0-or-later** with original authors credited; see [LICENSE](LICENSE). The mod will not bundle proprietary game audio; it invokes modern Minecraft sound events as permitted by the game runtime.
