# Widgets and resizing

e_e launcher hosts standard Android App Widgets. The widget provider, not the
launcher, is responsible for producing its content and updates.

## Adding and arranging widgets

- Choose widgets from the grouped widget picker.
- Add multiple instances of a widget when supported by its provider.
- Move and resize a widget on the workspace.
- Use the arrow handles around a selected widget to change its boundaries;
  the opposite edge/corner stays fixed while resizing.
- Save the layout by leaving edit mode. Layout and size should persist after
  returning to the launcher.

Some widgets enforce their own minimum or maximum dimensions, and actual
content rendering is controlled by the provider app.

## Widget action chains

Launcher widget settings can assign optional **action chains** to individual
widget instances. A chain can intercept the entire widget or a configured
rectangular sub-area and execute multiple supported launcher/application
actions in sequence. Unassigned widgets keep their provider-controlled tap
behavior.

## Backups and widget IDs

Launcher settings backup and restore include widget placement and rebinding
metadata. Android may allocate new `appWidgetId` values following restoration
or reinstallation; the launcher attempts to reconnect widgets to the correct
provider when possible.

A launcher JSON backup does **not** contain the visual contents or data owned
by widget provider apps. For privacy implications see
[Privacy](../PRIVACY.md).

## Blank or frozen widgets

If a provider shows an empty widget or fails to update after reboot, see
[Troubleshooting](troubleshooting.md). Restrictive OEM background settings can
prevent the provider from sending its first `RemoteViews` update.
