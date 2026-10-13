# Design and security notes

## Scope
Port the entire original Mk III workflow, rather than just a block copier: geometry, cables, exchange, removal, move/copy/stack, transforms, inventory/ME sourcing, uplink, radial UI, holograms, upgrades and original sound *cues*. The Mk III benchmark is 1,024 blocks/s with adjustable server-side throttling.

## Phase 0 prototype included in this branch
Creative + operator proof of concept: use `/give @p matter_manipulator_modern:matter_manipulator_mk3`, sneak-right-click the first and second corners to capture up to 4,096 blocks, then right-click a destination face. The initial snapshot includes raw modern block states, serializable block entity data and modern item components. Pasting is server queued (64 cell operations/tick) in two passes. It makes no attempt to handle third-party address remapping. **Not a safe survival manipulator.**

## Requirements for a correct survival copier
- Build a capture representation of state, entity, inventories, attached data, and adapter-specific machine internals.
- Use **portable vs instance-unique** fields: absolute coordinates, owner IDs, AE2 P2P/quantum links, wireless cards and protection metadata require specific resolution before paste.
- Define mod adapters per version to reconstruct machines through stable public APIs, not by blindly copying internal NBT.
- Separate *blueprint/template* from *move*. Copying a blueprint must not duplicate stored items, fluids, electricity, chemical contents, or credentials in survival. Moving may preserve resources if the old instance is removed transactionally.
- Ensure security checks apply to both sources and destinations, with events/hooks honored; enforce snapshot size and packet bounds.
- Make jobs resumable; preflight block and material requirements; allow rollback/undo; ensure chunk tickets and error recovery are safe.
- Copy rotations, facings and sided inputs/outputs through adapters after placing blocks.
- Build AE2 source hierarchy (ME network -> player -> uplink) and item-return priority independently of the snapshot serializer.
- Validate cross-version serializations and implement a schema-version migration.
- Use sound events matching the original `SoundResource` choices; no bundled proprietary audio is required.

## Attribution
Original code: [RecursivePineapple/MatterManipulator](https://github.com/RecursivePineapple/MatterManipulator), LGPL-3.0. Modern reference implementation: [Raishxn/ModernManipulator](https://github.com/Raishxn/ModernManipulator). Both are acknowledged; no copyright ownership of upstream source is claimed. Retain applicable notices for source actually reused.
