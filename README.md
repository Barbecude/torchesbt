# Realistic Torches BT

Realistic Torches BT makes fire-based light sources behave more dynamically and immersive.

## Features

- **Burn Time Management**: 
  - Torches, lanterns, and campfires burn out after configurable times.
  - Burn times and extinguish behavior are fully configurable in `config/torchesbt.json5`

- **Environmental Effects**:
  - Rain and water can extinguish light sources.
  - Multipliers for rain and water effects are configurable.

- **Reignition System**:
  - Relight extinguished torches, lanterns, and campfires using custom igniters (e.g., flint & steel, blaze rods).
  - Igniters and their effectiveness are defined in JSON: `data/torchesbt/ignite/`

- **Fueling System**:
  - Extend burn times with fuel items (e.g., coal, sticks).
  - Fuel definitions are data-driven via JSON: `data/torchesbt/fuel/`

- **Dynamic Lighting (Optional)**:
  - Toggleable dynamic lighting support.
  - Configurable via `config/torchesbt.json5`
  - Held and dropped torches and lanterns lose burn time while Dynamic Light support is enabled.
 
- **Data-Driven Customization**:
  - All ignition and fuel behaviors are fully editable via JSON files, allowing easy modpack or server customization without code changes.

- **NOTES**:
  - Very early alpha — incompatibilities with other mods that modify torches, lanterns, or campfires are expected.
  - Gonna expand on this mod by slowly adding compat to other mods and adding new mechanics to keep torches, lanterns and campfires lit longer/indefinite

## Dependencies

- **Required**: Fabric API, Cloth Config
- **Optional**: LambDynamicLights, Sodium Dynamic Lights

## Configuration

Edit `config/torchesbt.json5` to tweak:
- Burn times for torches, lanterns, and campfires (in seconds).
- Rain/water extinguish multipliers and toggles.
- Enable or disable dynamic lighting support.
- JSON paths for igniters and fuels.
