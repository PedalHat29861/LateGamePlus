# LateGame+

LateGame+ expands Minecraft’s endgame with new progression items, upgrades, utilities, and infrastructure designed to keep gameplay engaging long after Netherite.

Every feature is balanced and fully configurable, focusing on meaningful late-game systems rather than standalone gimmicks.

---

## New in v1.2.1

This update focuses on **companions and mobility infrastructure**, expanding late-game utility beyond the player character.

* **Netherite Wolf Armor**: late-game protection for tamed wolves.
  ![Wolves](https://cdn.modrinth.com/data/cached_images/fe78dd0cb8895ec2a4a410ae0701a180ce68c35f.png)

 
* **Netherite Happy Ghast Harness (Storage)**: netherite-grade harness with portable storage.

  ![Harness](https://cdn.modrinth.com/data/cached_images/28ed7e2e7c95a3dd44b85ffc95e9f1dc2f88047a.png)

---

## Feature Highlight (v1.2.0)

### Debris Resonator

Late-game locator for **Ancient Debris** powered by a configurable battery.

* Custom scan tiers and configurable cooldowns.
* Rechargeable via Anvil using **Echo Shards** or **Amethyst Shards**.
* Fully configurable through Mod Menu / YACL or JSON.

![debris\_resonator\_showcase\_placeholder](https://i.imgur.com/zW9Eldb.gif)

---

## Feature Showcase

### Exploration & Utility

<details>
<summary>Debris Resonator</summary>

Track **naturally generated Ancient Debris** in the Nether with a rechargeable locator.

Key mechanics:

* **Each** ancient debris has a **50% chance** to be trackable; player-placed debris is never tracked.

* **Tiered scan rings** communicate distance via animations and audio cues.

  | Tier         | Effective range            | Feedback summary                                                                  |
  | ------------ | -------------------------- | --------------------------------------------------------------------------------- |
  | Close        | 0 – 5 blocks               | Fast pulses and a vivid green inner ring indicate the debris is right beside you. |
  | Medium       | 5 – 10 blocks              | Steady rhythm with a yellow ring keeps you on track.                              |
  | Far          | 10 – 16 blocks (≈1 chunk)  | Slower beat and red ring confirm you are still within range.                      |
  | Too far      | 16 – 32 blocks (≈2 chunks) | Low tone with a dim orange ring warns you are at the edge.                        |
  | Out of range | > 32 blocks                | Signal drops; cooldown applies.                                                   |

  ![debris\_resonator\_range](https://i.imgur.com/f8bkRfG.jpeg)

* Displays remaining battery even when powered off.

  | Cooldown type              | Default | When it applies              |
  | -------------------------- | ------- | ---------------------------- |
  | Successfully mining target | 25s     | Tracked debris is mined      |
  | Target missing             | 10s     | Debris removed externally    |
  | Distance limit             | 60s     | Player leaves tracking range |

Crafting Recipe:

![debris\_resonator\_recipe](https://cdn.modrinth.com/data/cached_images/83c0d7b6b73eff92762d4bfd4b44534eaca11dc1.png)

</details>

---

<details>
<summary>Lodestone Warp Compass</summary>

Warp instantly back to your **lodestone-bound location**.

* Works like an Ender Pearl tied to a Lodestone.
* Configurable cooldown.
* Optional **cross-dimension travel**.

Crafting Recipe:

![compass\_warp\_craft](https://cdn.modrinth.com/data/cached_images/8408cfcce789d33916d7465503b71a79ee820d41.png)

Same Dimension:

![compass\_warp\_same\_dimension](https://i.imgur.com/xb4DCc1.gif)

Across Dimensions:

![compass\_warp\_across\_dimensions](https://i.imgur.com/QqFDHVH.gif)

</details>

---

### Consumables & Progression Items

<details>
<summary>Netherite Apple & Enchanted Netherite Apple</summary>

**Netherite Apple**

* Crafted directly or upgraded with a Netherite Smithing Template.

![netherite\_apple\_craft](https://cdn.modrinth.com/data/cached_images/bda1d89a87d40e09132f35e2144257c454d14c67.png)

Upgrade:

![netherite\_apple\_smithing](https://cdn.modrinth.com/data/cached_images/54447fba633b9293488e1d014a7797041c2388bf.png)

**Enchanted Netherite Apple**

![enchanted\_netherite\_apple\_smithing](https://cdn.modrinth.com/data/cached_images/f126df0768b1b85a3526b10417da659d9f8b0b30.png)

</details>

---

<details>
<summary>Totem of Netherdying</summary>

* Two uses by default (configurable).
* Same mechanics as vanilla, tougher.

![totem\_of\_netherdying\_smithing](https://cdn.modrinth.com/data/cached_images/25848de7684edb40fc8a6cd2fb3f066fcd18d106.png)

</details>

---

### Weapons & Combat Gear

<details>
<summary>Netherite Bow</summary>

![netherite\_bow\_smithing](https://cdn.modrinth.com/data/cached_images/0ea423d16a6c073c3a7ac83fff31ad9959b7074b.png)

</details>

---

<details>
<summary>Netherite Crossbow</summary>

* Damage multiplier configurable.
* Increased durability.
* Compatible with vanilla enchantments.

![netherite\_crossbow](https://i.imgur.com/NFlWRVC.gif)

Smithing:

![netherite\_crossbow\_smithing\_recipe](https://cdn.modrinth.com/data/cached_images/5ef861045aaa4a0f3fd30726896ecbf945aee9cd.png)

</details>

---

### Mobility & Equipment

<details>
<summary>Netherite Elytra</summary>

![netherite\_elytra\_smithing](https://cdn.modrinth.com/data/cached_images/f5683d7e8deb1248ae2631d7fdbcfaef0e400b1b.png)

</details>

---

<details>
<summary>Netherite Happy Ghast Harness</summary>

Upgrade any dyed Happy Ghast harness into a **fireproof, netherite-grade rig**.

* Adds armor value and Fire Resistance.
* Supports up to **two chests**; sneak to access storage.
* Storage drops automatically if removed.

![happy\_ghast\_storage\_placeholder](https://i.imgur.com/mA1Hpbh.gif)

</details>

---

### Companion Equipment

<details>
<summary>Netherite Wolf Armor</summary>

* Smithing upgrade from vanilla Wolf Armor.
* Fireproof with increased protection.
* Grants Fire Resistance.
* Dyeable and cleanable in cauldrons.

![netherite_wolf_armor](https://cdn.modrinth.com/data/cached_images/ef2461de1b63c17610d8d1ca120bdd3b348b6e8d.png)![Netherite_wolf_armor_smithing](https://cdn.modrinth.com/data/cached_images/ec9dd560079082e5f1e4eda97aa70259662bf427.png)

</details>

---

### Workstations & Materials

<details>
<summary>Netherite Anvil</summary>

* Unbreakable.
* Removes the Too Expensive limit.
* Configurable XP cap.

![netherite\_anvil\_crafting\_recipe](https://cdn.modrinth.com/data/cached_images/7484ad988d0dabc1deebbebce455427430b4b2cb.png)
![netherite\_anvil\_xp\_cap](https://cdn.modrinth.com/data/cached_images/f4a976eea90107348e3001c99b41ddd9088cbc3e.png)
![vanilla\_anvil\_tooexpensive\_example](https://cdn.modrinth.com/data/cached_images/b312207a60f5bdd80b3b98772651523437f33567.png)

</details>

---

<details>
<summary>Netherite Nuggets</summary>

* Break and recombine ingots.
* Repair Netherite gear.
* Dropped by Piglin Brutes.

![netherite\_nugget\_from\_ingot](https://cdn.modrinth.com/data/cached_images/7d5f4df86e2c80271a0af165cdc14ec2f3298b90.png)
![netherite\_ingot\_from\_nuggets](https://cdn.modrinth.com/data/cached_images/2ca0eb479e261d1394a744dd6c05d3c85133c606.png)
</details>

## ⚙️ Configuration

Fine-tune the mod to fit your playstyle:  

  * Elytra protection level (0–4).  
  * Totem of Netherdying uses.  
  * Lodestone cooldown + cross-dimension toggle.  
  * Piglin Brute nugget drop rates.  
  * Nugget repair percentage.  
  * Netherite Anvil XP cap.  
  * Crossbow damage multiplier.  

---

## 🛠 Compatibility

  * Fully compatible with **Mod Menu**.  
  * Uses **YACL** for in-game config menus.  
  * Works standalone if you prefer configs by file.  

---

## 📥 Installation

  1. Install Fabric.  
  2. Drop the `.jar` into your `mods` folder.  
  3. (Optional) Add Mod Menu + YACL for easy config in-game.  

---

# 🗺️ Roadmap & Future Plans

<details>
<summary> Misc Additions</summary>

  * Lodestone Waypoint Teleport ✅  

</details>

<details>
  <summary> Netherite Additions</summary>

  * Netherite Anvil ✅  
  * Netherite Crossbow ✅
  * Some kind of netherite compass ✅
  * Netherite Happy Ghast Harness (chest storage + armor). ✅
  * Maybe: Dog Armor.  ✅

</details>

<details>
  <summary> End / Void Additions</summary>

  * **Void Smithing Template** (rare End loot).  
  * **Void Infuser** (special workstation).  
  * **Void Crystals** & refined variants.  
  * **Void Infused Netherite** (gear upgrades, blocks, nuggets).  
  * **Void Elytra** (diamond-level protection).  
  * **Void Totem of Undying** (3 uses, protects in the void).  
  * Full **Void Armor / Tools / Weapons** sets.  
  * Void Apples (normal & enchanted).  
  * Maybe: Void Horse/Dog Armor.  

</details>

---

 **LateGame+** gives you fresh reasons to keep playing once you’re fully geared. Tweak configs to match your progression style, and enjoy a true late-game Minecraft experience.

---
