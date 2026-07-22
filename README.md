# Offhand Keybind

A client-side Fabric mod for Minecraft Java **26.2**. It separates off-hand
item use from Minecraft's normal Use key.

The bow-with-no-arrows example is a Minecraft **Use** action (normally right
click), not a left-click combat attack. Vanilla tries the main hand first and
then falls back to the off hand if the main hand does not handle the action.
This mod changes that behavior:

- The normal Minecraft Use key only attempts the **main hand**.
- The new **Off-hand Use** key only attempts the **off hand**.
- Off-hand fallback from the normal Use key is disabled completely.
- Normal left-click attack and block-breaking behavior is unchanged.

The dedicated key is registered in **Options > Controls > Key Binds > Offhand
Keybind**. It defaults to **Mouse 3 / middle click** and can be rebound like
any other Minecraft keybind. Holding it follows Minecraft's normal use-repeat
and cooldown behavior.

Mouse 3 is also Minecraft's default Pick Block key. While both bindings use
the same button, this mod gives **Off-hand Use** priority. If you rebind
Off-hand Use to another occupied control, rebind that other control as well.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.154.2+26.2 or a newer compatible 26.2 release
- Java 25 or newer

This is a client-only mod. It uses Minecraft's normal interaction packets, so
the mod is not required on a vanilla server.

## Build with GitHub Actions

You do not need to install Java, Gradle, or any build tools locally. The
workflow at [`.github/workflows/build.yml`](.github/workflows/build.yml) uses
Java 25 and builds the release JAR on GitHub.

1. Commit and push this project to GitHub.
2. Open the repository's **Actions** tab.
3. Select **Build Fabric mod**, then choose **Run workflow**. Pushing to a
   branch also starts it automatically.
4. Open the successful run and download the `offhand-keybind-26.2-...`
   artifact. Its ZIP contains the playable JAR, not a development or sources
   JAR.

Place that JAR and the matching Fabric API JAR in the `mods` folder of a Fabric
26.2 instance that runs on Java 25.

## Version compatibility

Version 2.0.0 and later target Minecraft 26.2 only. Minecraft 26.1 introduced
unobfuscated Mojang names and removed Fabric's supported Yarn mapping path, so
the 1.21.11 JAR cannot run on 26.2 and this 26.2 JAR cannot run on 1.21.11.

## Technical approach

The mod keeps Minecraft's own item-use routine intact. A small mixin limits
normal Use to `MAIN_HAND`, and invokes that same routine with `OFF_HAND` for
the dedicated key. This preserves vanilla block, entity, and item interaction
ordering, animations, cooldowns, and networking while removing the off-hand
fallback.
