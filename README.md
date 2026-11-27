# Lobby Launcher

A minimal Android TV home screen that is just your inputs. Press Home and the connected devices
appear big and centred, in the style of the Apple TV “Who's watching?” screen. Pick one and the
TV switches to it.

## Features

- **Source list as the home screen.** Every HDMI input is shown as a large round avatar with its
  name and port, with nothing else competing for attention, so anyone who lands on the TV by
  accident sees straight away where to go next. Empty inputs are dimmed and devices in standby
  are labelled.
- **Automatic recognition.** Names reported over HDMI-CEC (for example “PlayStation 5” or
  “Apple TV”) are matched against a catalog of 69 devices, which sets the name and icon
  automatically. The catalog covers:
  - PlayStation 2 to 5 Pro; Xbox 360, One and Series S/X; Switch and Switch 2, Wii, Wii U and
    GameCube.
  - Steam Deck, Steam Machine and Steam Link; ROG Ally, Legion Go and MSI Claw.
  - Apple TV, Fire TV, Google TV Streamer, Chromecast, Roku, NVIDIA SHIELD and similar boxes.
  - Operator set-top boxes such as MagentaTV, Vodafone, One TV, DIGI, Sky, Virgin and TiVo.
  - Blu-ray players, soundbars and AV receivers, PCs, phones, tablets, Raspberry Pi and more.
- **Fully editable.** Every source has a custom name, icon, colour and visibility, plus a
  one-click “Reset to automatic”. The icon picker lists every catalogue device by name, grouped
  by category. After you pick one, it offers to use that device's name (“Use the name
  ‘PlayStation 5’?”), so you don't have to type with the remote.
- **Original icon pack.** 45 glyphs and 16 gradient colours, all drawn for this app. Each console
  generation has its own silhouette (PS2 to PS5, Xbox 360 to Series X, GameCube, Wii, Wii U,
  Switch), as do the handheld PCs. There are no brand logos: product names are used only to
  describe the device.
- **Custom order.** The home screen order is independent of the TV's HDMI numbering.
- **Auto-select.** Optionally, when the home screen appears, a countdown ring on the default
  source switches to it after 3–60 seconds, with “Switching to … in N” shown at the top. Any
  button cancels it. It can be limited to devices the TV reports as on. The default source is
  chosen in one place: Settings › Automatic selection.
- **Clock.** A small clock in the corner, in 12-hour (AM/PM), 24-hour or the system format.
- **TV settings, kept small.** Small “TV home”, “TV settings” and “Customise” buttons sit under the
  list. Sources are edited from Customise.
- **Home button integration.** See below.
- English and Hungarian UI.

## Build

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"   # any JDK 17+
./gradlew :app:assembleDebug            # → app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest        # device matching and ordering tests
```

## Install on the TV

1. On the TV, turn on Developer options and USB/network debugging.
2. Install the APK:

   ```sh
   adb connect <tv-ip>:5555
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

The app shows up in the TV's app list as **Lobby Launcher**.

### Making Home open the launcher

Android TV (and especially Google TV) normally won't let a third-party app replace the home
screen. There are two options.

**1. Default home app.** This works on AOSP-based and some vendor TVs:

```sh
adb shell cmd package set-home-activity dev.germar.sourcelauncher/.MainActivity
```

On Google TV the system launcher keeps priority, so this has no visible effect there.

**2. Home button service (recommended, needed on Google TV).** This is an accessibility service
that only listens to the Home key. Turn it on in the TV's settings under
**Settings → System → Accessibility → Lobby Launcher Home button**, or with adb:

```sh
adb shell settings put secure enabled_accessibility_services \
  dev.germar.sourcelauncher/dev.germar.sourcelauncher.service.HomeButtonService
adb shell settings put secure accessibility_enabled 1
```

The adb command replaces the list of enabled accessibility services. If you already use others,
append this one with `:`.

Once the service is on:

- A **short Home press** opens Lobby Launcher.
- A **long Home press** opens the TV's own home screen.

Options under **Customise → Home button**:

- **Open when the TV turns on:** show the source list right after power-on.
- **Replace the TV home screen:** whenever the stock home appears (for example after closing an
  app), it is swapped for this launcher. A long press on Home still reaches the stock home on
  purpose.

## Demo mode

The emulator has no HDMI inputs. To try the UI with sample devices (Apple TV, PlayStation 5, Steam
Deck and an empty port), either:

```sh
adb shell am start -n dev.germar.sourcelauncher/.MainActivity --ez demo true
```

or turn on **Customise → Demo mode**. In demo mode, choosing a source only shows a toast.

## How it works

- Inputs come from `TvInputManager.getTvInputList()`. HDMI-CEC devices appear as child inputs of
  an HDMI port, and their labels (`loadLabel` / `loadCustomLabel`) are matched against
  [`DeviceCatalog`](app/src/main/java/dev/germar/sourcelauncher/catalog/DeviceCatalog.kt) by
  [`DeviceMatcher`](app/src/main/java/dev/germar/sourcelauncher/catalog/DeviceMatcher.kt).
  Connection state comes from `getInputState`.
- Switching sends `ACTION_VIEW` with `TvContract.buildChannelUriForPassthroughInput(inputId)`,
  which the TV's Live TV / input app handles. If that fails, the app opens the TV's input app
  directly.
- Settings are stored in a single JSON DataStore.

## Limitations

- **Home button:** a launcher can't take over Home on Google TV by itself. The accessibility
  service is needed, and some TVs turn accessibility services off after a firmware update.
- **Input switching is vendor-specific.** The passthrough URI is the standard Android TV API, but
  some vendor input apps ignore it or always open their own UI. On those TVs you land in the TV's
  input app instead of directly on the source.
- **Recognition depends on HDMI-CEC.** If CEC is off, or the device reports no name (or a generic
  one), the input is shown as “HDMI n” with a generic icon. You can name it and pick an icon by
  hand.
- **Connection state is approximate.** Many TVs report every HDMI port as connected, or don't
  report standby. “Only when it's on” and dimming depend on what the TV reports.
- **Not tested on real hardware.** Testing so far used an Android TV 14 emulator in demo mode;
  real HDMI switching hasn't been tried. The Home-button service was checked with a virtual HID
  remote. `adb shell input keyevent HOME` does not reach accessibility services, so it can't be
  used to test this.
