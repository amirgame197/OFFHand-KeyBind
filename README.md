# Offhand Keybind

A client-side Fabric mod for Minecraft Java **1.21.11** that separates the
off-hand item-use action from the normal Use key.

The bow-with-no-arrows example in the request is a Minecraft **Use** action
(normally right-click), not the left-click combat attack. Vanilla tries the
main hand first and falls back to the off hand when the main hand does not
handle the use. This mod changes that behavior:

- The normal Minecraft Use key only attempts the **main hand**.
- The new **Off-hand Use** key only attempts the **off hand**.
- Off-hand fallback from the normal Use key is disabled completely.
- Normal left-click attack/destroy behavior is unchanged.

The dedicated key is registered in **Options → Controls → Key Binds → Offhand
Keybind** and defaults to **Mouse 3 / middle click**. It can be rebound from
that screen like every other Minecraft keybind. Holding it uses the same
repeat/cooldown behavior as the normal Use key.

The default middle-click binding intentionally takes precedence over
Minecraft's Pick Block action while both use the same button. If you rebind
Off-hand Use to another occupied game control, rebind that other control too.

## Player requirements

- Minecraft Java Edition 1.21.11
- Fabric Loader 0.19.3 or newer
- Fabric API 0.141.5+1.21.11 or newer compatible with 1.21.11

This is a client-only mod. It uses the same normal interaction packets as
Minecraft, so it does not need to be installed on a vanilla server.

## Build it with GitHub Actions

No local Java or Gradle installation is needed. The repository includes a
workflow at [`.github/workflows/build.yml`](.github/workflows/build.yml) that
uses Java 21 and builds the release JAR.

1. Push this project to the connected GitHub repository.
2. Open the repository's **Actions** tab.
3. Select **Build Fabric mod** and choose **Run workflow**. A push to any
   branch also starts the build automatically.
4. When the workflow finishes, open its run and download the
   `offhand-keybind-1.21.11-...` artifact. The downloaded ZIP contains the
   playable mod JAR, not a development JAR.

To use the built mod, place the JAR and the matching Fabric API JAR in the
`mods` folder of a Fabric 1.21.11 Minecraft instance.

## Local commands (optional)

GitHub Actions is the intended build path. If you later install Java 21, these
commands also work locally:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

The built release JAR is written to `build/libs/`.

## Technical approach

The mod leaves Minecraft's item-use implementation intact. A small mixin
selects only `MAIN_HAND` when Minecraft's normal Use key invokes that method,
and selects only `OFF_HAND` while the dedicated key invokes the same method.
This preserves vanilla block/entity/item interaction ordering, animations,
cooldowns, and networking while removing the fallback behavior.
