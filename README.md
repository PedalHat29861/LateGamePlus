# LateGame+
---

> 
> ### ✨ New Feature – Debris Resonator (V1.2.0)
>
> * Late-game locator for **Ancient Debris** powered by a configurable battery (default 30 minutes).
> * Custom scan tiers and configurable cooldowns.
> * Recharge in an Anvil with **Echo Shards** (full) or **Amethyst Shards** (1/60 each).
> * Fully configurable through Mod Menu / YACL or the JSON config.
>
> ![debris_resonator_showcase_placeholder](https://i.imgur.com/zW9Eldb.gif)
> **Gameplay Preview**


## ❤️ Support & Feedback

If you enjoy **LateGame+** and want to help out, there are a couple of ways:

- ☕ [Buy me a coffee](https://ko-fi.com/pedalhat29861) if you feel like supporting the project. Totally optional, but always appreciated.
  
  [![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/pedalhat29861)
  
- 📝 Got ideas, suggestions, or found a bug? Drop them here: [GitHub Issues](https://github.com/PedalHat29861/LateGamePlus/issues)  

Every bit of support (whether it’s feedback or a coffee) helps me keep making the mod better. Thanks a ton!


---


**LateGame+** expands Minecraft’s endgame with new **progression items, upgrades, and utilities** keeping your adventure challenging and rewarding long after you’re geared up.  

From **Netherite Apples** to **Warping Compasses**, **Unbreakable Anvils**, and **Armored Elytras**, every addition is balanced and fully configurable.

---

## 📦 Feature Showcase

<details>
<summary>Debris Resonator</summary>

  Track **naturally generated Ancient Debris** in the Nether with a rechargeable locator.

Key mechanics:
* **Each** ancient debris has a **50% chance** to be "trackable" as a way to balance its power, additionally, ancient debris placed by players will **never** be tracked.
  
* **Tiered scan rings** communicate distance via animations and audio cues.

  | Tier | Effective range | Feedback summary |
  | --- | --- | --- |
  | Close | 0 – 5 blocks | Fast pulses and a vivid green inner ring indicate the debris is right beside you. |
  | Medium | 5 – 10 blocks | Steady rhythm with a yellow ring keeps you on track. |
  | Far | 10 – 16 blocks (≈1 chunk) | Slower beat and red ring confirm you are still within range. |
  | Too far | 16 – 32 blocks (≈2 chunks) | Low tone with a dim orange ring warns you are at the edge. |
  | Out of range | > 32 blocks | Signal drops; the resonator disengages and applies the configured cooldown. |

  ![debris_resonator_range](https://i.imgur.com/f8bkRfG.jpeg)
  *Example*

* Displays remaining battery even when powered off; configurable cooldowns after depletion or range loss.


  | Cooldown type | Default duration | When it applies |
  | --- | --- | --- |
  | Successfully Mining the tracked Debris | 25 seconds | When you successfully mine the block that the debris resonator was tracking |
  | Target missing | 10 seconds | The tracked debris disappears (Broken due to an external reason or a third party). |
  | Distance limit | 60 seconds | You stray beyond the maximum tracking distance and the signal drops. |
  
* Activation is **Nether-only**; other dimensions show a warning message.
* Recharge via **Echo Shard** (full battery, 5 XP) or **Amethyst Shards** (1/60 battery, 1 XP each) in an Anvil.



Crafting Recipe:

![debris_resonator_recipe](https://cdn.modrinth.com/data/cached_images/83c0d7b6b73eff92762d4bfd4b44534eaca11dc1.png)

Configuration knobs (`debrisResonator*`):

* Maximum battery seconds, cooldowns (self / missing target / far distance).
* Vertical scan range.
* Sound volume multiplier.

</details>

---

<details>
<summary>Lodestone Warp Compass</summary>

Warp instantly back to your **lodestone-bound location**.  

* Works like an Ender Pearl, but tied to the lodestone.  
* Configurable cooldown (`lodestoneWarpCooldownTicks`).  
* Optional **cross-dimension travel** (`lodestoneWarpCrossDim`).  

Crafting Recipe:

![compass_warp_craft](https://cdn.modrinth.com/data/cached_images/8408cfcce789d33916d7465503b71a79ee820d41.png)

Same Dimension:

![compass_warp_same_dimension](https://i.imgur.com/xb4DCc1.gif)

Across Dimensions (config enabled):

![compass_warp_across_dimensions](https://i.imgur.com/QqFDHVH.gif)

</details>

---

<details>
<summary> Netherite Apple & Enchanted Netherite Apple</summary>

**Netherite Apple**  
* Crafted directly or upgraded with a Netherite Smithing Template.  
* Designed as a true late-game consumable.  

![netherite_apple_craft](https://cdn.modrinth.com/data/cached_images/bda1d89a87d40e09132f35e2144257c454d14c67.png)  

Upgrade with Smithing Table:  

![netherite_apple_smithing](https://cdn.modrinth.com/data/cached_images/54447fba633b9293488e1d014a7797041c2388bf.png)  

**Enchanted Netherite Apple**  

![enchanted_netherite_apple_smithing](https://cdn.modrinth.com/data/cached_images/f126df0768b1b85a3526b10417da659d9f8b0b30.png)

</details>

---

<details>
<summary> Netherite Gear Upgrades</summary>

* **Netherite Bow** – tougher, stronger, made for late raids.  
  ![netherite_bow_smithing](https://cdn.modrinth.com/data/cached_images/0ea423d16a6c073c3a7ac83fff31ad9959b7074b.png)  

* **Netherite Elytra** – reinforced wings with armor protection (configurable).  
  ![netherite_elytra_smithing](https://cdn.modrinth.com/data/cached_images/f5683d7e8deb1248ae2631d7fdbcfaef0e400b1b.png)  

</details>

---

<details>
<summary> Netherite Crossbow</summary>

Turn the vanilla crossbow into a **late-game powerhouse**.  

  * Damage multiplier: **1.5x** (configurable: `netheriteCrossbowDamageMultiplier`).  
  * Increased durability for extended fights (`netheriteCrossbowDurabilityMultiplier`).  
  * Compatible with all vanilla enchantments.  

    ![netherite_crossbow](https://i.imgur.com/NFlWRVC.gif)  

  Smithing Upgrade:

  ![netherite_crossbow_smithing_recipe](https://cdn.modrinth.com/data/cached_images/5ef861045aaa4a0f3fd30726896ecbf945aee9cd.png)

</details>

---

<details>
<summary> Netherite Anvil</summary>

The ultimate upgrade to your workstation.  

  * **Unbreakable** – no more shattered anvils mid-session.  
  * Removes the `Too Expensive!` limit.  
  * Configurable repair cap (default **35**, tweakable 20–39).  

Crafting Recipe:  

  ![netherite_anvil_crafting_recipe](https://cdn.modrinth.com/data/cached_images/7484ad988d0dabc1deebbebce455427430b4b2cb.png) 

XP Cap:  

  ![netherite_anvil_xp_cap](https://cdn.modrinth.com/data/cached_images/f4a976eea90107348e3001c99b41ddd9088cbc3e.png)

Vanilla Comparison:  

  ![vanilla_anvil_tooexpensive_example](https://cdn.modrinth.com/data/cached_images/b312207a60f5bdd80b3b98772651523437f33567.png)

</details>

---

<details>
<summary> Totem of Netherdying</summary>

A darker twist on the Totem of Undying.  

* **Two uses by default** (configurable).  
* Same mechanics as vanilla just tougher.  

![totem_of_netherdying_smithing](https://cdn.modrinth.com/data/cached_images/25848de7684edb40fc8a6cd2fb3f066fcd18d106.png)

</details>

---

<details>
<summary> Netherite Nuggets</summary>

  * Break ingots down into nuggets.  
  * Recombine nuggets into ingots.  
  * Repair Netherite gear with nuggets (configurable %).  
  * Piglin Brutes can drop them.  

    ![netherite_nugget_from_ingot](https://cdn.modrinth.com/data/cached_images/7d5f4df86e2c80271a0af165cdc14ec2f3298b90.png)  
    ![netherite_ingot_from_nuggets](https://cdn.modrinth.com/data/cached_images/2ca0eb479e261d1394a744dd6c05d3c85133c606.png)

</details>

---

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
  * Netherite Happy Ghast Harness (chest storage + armor). 
  * Maybe: Netherite Horse Armor, Dog Armor.  

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