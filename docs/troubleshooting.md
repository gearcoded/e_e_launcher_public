# Troubleshooting

These checks address common issues with third-party launcher integration.
The names of Android system options vary by device maker and firmware.

## A widget stays blank or stops updating

An Android widget is rendered by its **provider application** and delivered
through `RemoteViews`. If the provider is restricted by the phone's battery
or process manager, the launcher may not receive an initial update.

For the launcher and the affected provider app, look for options such as:
**Allow running in the background**, **Unrestricted battery use**,
**Allow background activity**, **Autostart**, or **Pause app activity if unused**.
Change only what is needed for that app; broad unrestricted background access
is not always necessary. After reboot, allow the provider time to deliver
its first update before concluding that the widget is broken.

Provider-specific and device-specific findings are documented in
[`TESTED_DEVICES.md`](../TESTED_DEVICES.md).

## Opening notifications or locking the screen does not work

These optional launcher actions use the **e_e launcher Accessibility Service**.
The service cannot enable itself; verify that the user has granted access in
Android Accessibility settings.

When upgrading an installed launcher APK, an OEM system may temporarily
disconnect or disable the service. If that occurs, check its status and enable
it manually. Do not assume that ADB or a special installation script can
restore it on every firmware version.

## Android temporarily shows the system home screen after an update

Replacing the installed launcher APK can terminate the active launcher
process. Some devices briefly show their original home screen. Press the
Home button again and confirm that e_e launcher opens and its layout remains.
If the home application has genuinely changed, select e_e launcher again from
Android's default home application settings.

## An APK will not install over the current launcher

A normal update requires the same package ID and a compatible signing
certificate, plus an appropriate version code. If Android reports
`INSTALL_FAILED_UPDATE_INCOMPATIBLE`, **do not uninstall the launcher or clear
its data as a workaround**: verify that you are using the expected release
signing certificate. See [Building from source](building.md).

## Graphics issues

Try temporarily disabling visual effects using [Safe Mode](safe-mode.md).
If the problem goes away, turn effects on again selectively to identify the
feature involved. The source repository's detailed diagnostic collectors are
maintainer/developer tools, not required for normal installation.
