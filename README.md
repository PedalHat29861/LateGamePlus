# LateGame+

If you enjoy LateGame+ and want to support its development, feedback and ideas are always welcome.

* 💡 Have suggestions, balance ideas, or found a bug?  
  Open an issue on GitHub and let’s improve the mod together.  
  [![GitHub Issues](https://img.shields.io/github/issues/PedalHat29861/LateGamePlus)](https://github.com/PedalHat29861/LateGamePlus/issues)

* ☕ Want to support the project directly?  
  You can donate on Ko-fi to help keep LateGame+ evolving.  
  [![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20the%20mod-red)](https://ko-fi.com/pedalhat29861)

---

LateGame+ expands Minecraft’s endgame with new progression items, upgrades, utilities, and infrastructure designed to keep gameplay engaging long after Netherite.

Every feature is balanced and fully configurable, focusing on meaningful late-game systems rather than standalone gimmicks.

---

## ⭐ New in v1.3: Fusion Forge

### Fusion Forge

An advanced **metal fusion workstation** designed for late-game efficiency and consistency.

The Fusion Forge processes **two material inputs + fuel**, allowing metals to be fused for **higher and more reliable yields** than traditional smelting or Fortune-based mining.

Its true potential is unlocked by installing a **Nether Star catalyst** (permanent, non-consumable), which **doubles all outputs** and pushes resource efficiency beyond Fortune III averages — without relying on RNG.

![fusion_forge_showcase_placeholder](https://cdn.modrinth.com/data/cached_images/9dc9de7fc2ca67fe1b89ab60dd9480d0a9ab7b46.png)

---

### Core Mechanics

* Requires **2 input slots + fuel** to operate.
* Optional **Nether Star catalyst**:
  * Not consumed.
  * Doubles all recipe outputs.
* Designed to outperform **Fortune III averages**, not theoretical maximums.
* Focused on **late-game optimization**, not early progression skips.

---

### Example Fusion Recipes

| Recipe Type        | Inputs                               | Base Output                | With Nether Star |
|-------------------|--------------------------------------|----------------------------|------------------|
| Ore Fusion        | Silk-touched ore + Copper Ingot      | ~Fortune II average        | **x2 output**    |
| Netherite Fusion  | Ancient Debris Scrap + Gold Ingot    | 3 Netherite Nuggets        | **6 Nuggets**    |
| Alloy Fusion      | Raw Iron + Coal                      | 3 Iron Ingots              | **6 Iron Ingots** |

> Outputs are balanced per material based on vanilla Fortune drop tables.

![Fusion_forge_craft](https://cdn.modrinth.com/data/cached_images/616ce33846f4b49146bd1e0dbc519724d58d31b0.png)

---
<details> <summary> Data-Driven Recipes (Advanced)</summary>

Fusion Forge recipes are **fully data-driven** and not hardcoded.

Custom recipes can be defined via JSON using the `lategameplus:fusion_forge` recipe type:

```json
{
  "type": "lategameplus:fusion_forge",
  "input_a": "minecraft:raw_iron",
  "input_b": "#minecraft:coals",
  "result": {
    "id": "minecraft:iron_ingot",
    "count": 3
  },
  "experience": 0.5,
  "cook_time": 200,
  "fuel_cost": 100
}
````

* `input_a` and `input_b` are **order-independent**.
* Fully compatible with **datapacks** for custom balancing or mod integration.
* Designed for extensibility by advanced users and modpack creators.

Have ideas or balance suggestions? Open an issue — feedback is welcome.

---
</details>

## Feature Showcase

### Exploration & Utility

<details>
<summary>Debris Resonator</summary>

Track **naturally generated Ancient Debris** in the Nether with a rechargeable locator.

Key mechanics:

* **Each** ancient debris has a **50% chance** to be trackable; player-placed debris is never tracked.
* Tiered scan rings with visual and audio feedback.

![debris\_resonator\_range](https://i.imgur.com/f8bkRfG.jpeg)
![debris\_resonator\_recipe](https://cdn.modrinth.com/data/cached_images/83c0d7b6b73eff92762d4bfd4b44534eaca11dc1.png)

</details>

---

<details>
<summary>Lodestone Warp Compass</summary>

Instantly warp back to your **lodestone-bound location**.

* Configurable cooldown.
* Optional cross-dimension travel.

![compass\_warp\_same\_dimension](https://i.imgur.com/xb4DCc1.gif)
![compass\_warp\_across\_dimensions](https://i.imgur.com/QqFDHVH.gif)

</details>

---

### Consumables & Progression Items

<details>
<summary>Netherite Apple & Enchanted Netherite Apple</summary>

![netherite\_apple\_crafting](https://cdn.modrinth.com/data/cached_images/bda1d89a87d40e09132f35e2144257c454d14c67.png)
![enchanted\_netherite\_apple](https://cdn.modrinth.com/data/cached_images/f126df0768b1b85a3526b10417da659d9f8b0b30.png)

</details>

---

<details>
<summary>Totem of Netherdying</summary>

* Multiple uses (configurable).
* Tougher vanilla behavior.

![totem\_netherdying](https://cdn.modrinth.com/data/cached_images/25848de7684edb40fc8a6cd2fb3f066fcd18d106.png)

</details>

---

### Weapons & Combat Gear

<details>
<summary>Netherite Bow</summary>

![netherite\_bow](https://cdn.modrinth.com/data/cached_images/0ea423d16a6c073c3a7ac83fff31ad9959b7074b.png)

</details>

---

<details>
<summary>Netherite Crossbow</summary>

* Configurable damage multiplier.
* Increased durability.

![netherite\_crossbow](https://i.imgur.com/NFlWRVC.gif)

</details>

---

### Mobility & Equipment

<details>
<summary>Netherite Elytra</summary>

![netherite\_elytra](https://cdn.modrinth.com/data/cached_images/f5683d7e8deb1248ae2631d7fdbcfaef0e400b1b.png)

</details>

---

<details>
<summary>Netherite Happy Ghast Harness</summary>

Fireproof, netherite-grade harness with optional chest storage.

![happy\_ghast\_storage](https://i.imgur.com/mA1Hpbh.gif)

</details>

---

### Companion Equipment

<details>
<summary>Netherite Wolf Armor</summary>

Fireproof armor upgrade for tamed wolves.

![netherite\_wolf\_armor](https://cdn.modrinth.com/data/cached_images/ef2461de1b63c17610d8d1ca120bdd3b348b6e8d.png)

</details>

---

### Workstations & Materials

<details>
<summary>Netherite Anvil</summary>

* Unbreakable.
* Removes Too Expensive limit.
* Configurable XP cap.

![netherite\_anvil](https://cdn.modrinth.com/data/cached_images/7484ad988d0dabc1deebbebce455427430b4b2cb.png)

</details>

---

<details>
<summary>Netherite Nuggets</summary>

* Break and recombine ingots.
* Repair Netherite gear.

![netherite\_nuggets](https://cdn.modrinth.com/data/cached_images/7d5f4df86e2c80271a0af165cdc14ec2f3298b90.png)

</details>

---

## ⚙️ Configuration

* Elytra protection level
* Totem of Netherdying uses
* Lodestone cooldown & dimension rules
* Piglin Brute nugget drops
* Netherite Anvil XP cap
* Crossbow damage multiplier

---

## 🛠 Compatibility

* Mod Menu support
* YACL in-game configuration
* Standalone JSON configs supported

---

## 📥 Installation

1. Install Fabric
2. Drop the `.jar` into your `mods` folder
3. (Optional) Add Mod Menu + YACL

---

**LateGame+** gives you real reasons to keep playing once you’re fully geared.
