# Safe Mode

Safe Mode temporarily disables visual effects that can complicate launch or
troubleshooting: page, app-drawer, icon, launch, and Environment Effects.
Those effect renderers are skipped while Safe Mode is active.

## Enable or disable manually

Go to:

**Launcher settings → Protection and compatibility → HOME role, locks,
icon pack and safe mode → Safe mode → Save**.

A manually enabled Safe Mode stays enabled until you turn it off using the
same setting and press **Save**. On the initial welcome screen, **Start in safe
mode** enables persistent manual Safe Mode before normal setup.

## Automatic crash recovery

The launcher can use an uncaught Java/Kotlin crash or Android-recorded
crash/native-crash exit information to start once with effects disabled.
This automatically activated protection lasts for one healthy launch; a later
process start returns to normal effects unless Safe Mode was enabled manually.
Ordinary process termination does not by itself trigger this recovery.

Safe Mode is a troubleshooting aid; it does not change Android's own system
Safe Mode, remove launcher settings, or disable installed applications.
