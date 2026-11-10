# Privacy Policy for e_e launcher

**Last updated: November 8, 2026**

e_e launcher is designed to work locally on the user's Android device. The application does not contain advertising, analytics, telemetry, tracking SDKs, or its own network communication.

## Data collection and network access

e_e launcher does not collect or transmit personal data to the developer or to third-party servers.

The application does not request the Android `INTERNET` permission and does not include its own networking client for sending launcher data off the device.

The application does not use advertising services, analytics platforms, Firebase, Google Play Services, crash-reporting services, or similar remote SDKs.

Crash recovery and Safe Mode detection are handled locally on the device. Crash information is not uploaded by e_e launcher.

## Installed applications and package information

As a launcher, e_e launcher needs to discover applications that can be launched from the home screen and app drawer. Android package visibility is limited to launcher applications and supported icon-pack intent categories; the application does not request `QUERY_ALL_PACKAGES`.

Information such as application labels, package/component names, icons, installation information, hidden-app state, custom ordering, and launcher-specific customization is processed locally and stored as part of the launcher's configuration when required.

This information is not sent to the developer.

## Usage access

e_e launcher can optionally use Android Usage Access (`PACKAGE_USAGE_STATS`) to provide accurate sorting by last use.

If the user grants this special system access, the launcher queries recent app-usage information locally and uses it to determine the most recent use time of applications. The current implementation queries up to approximately 90 days of usage history for this purpose.

Usage Access is not required for the basic operation of the launcher. e_e launcher does not transmit usage-history information to the developer or to third parties.

## Accessibility service

e_e launcher provides an optional Accessibility Service for launcher actions that Android otherwise does not make directly available to a normal application, currently including:

- opening the notification shade;
- locking the screen.

The Accessibility Service cannot enable itself; the user must explicitly enable it in Android system settings.

The service is configured with `canRetrieveWindowContent="false"`. e_e launcher does not use the Accessibility Service to read the contents of other applications, inspect typed text, monitor user activity, or collect accessibility-event data.

Accessibility information is not transmitted off the device.

## App widgets

e_e launcher can act as an Android App Widget host.

Widget content is produced by the widget provider application and delivered through Android's App Widget framework. e_e launcher stores the information needed to place, size, restore, and interact with hosted widgets, including widget binding identifiers and provider-related options when needed.

The privacy practices of a widget provider are controlled by that provider. e_e launcher does not independently transmit widget contents to the developer.

## Local desktop notes

Optional launcher-native note cards store their text in the same private launcher preferences as the workspace layout. Note text never leaves the device through the launcher’s own networking code (the app does not request `INTERNET`), but **the full text is included in JSON settings backups** and may also be included in Android system backups when that optional feature is enabled. Treat backups containing personal notes accordingly.

## Backups and restore

e_e launcher provides two separate backup mechanisms.

### Manual settings backup

The user can create a JSON backup of launcher settings. A manual backup can contain launcher configuration such as workspace layout, application/package references, customizations, note text, and widget restore metadata. References to user-selected `content://` images do not contain the image bytes themselves and might not work after a transfer. Widgets require their provider applications and may need to be rebound.

The backup file is created only at the user's request and is stored or shared wherever the user chooses. e_e launcher does not upload manual backup files to the developer.

Users should treat exported backup files as private if their launcher layout or installed-app information is sensitive to them.

### Android system backup

Android system backup support can be enabled or disabled in e_e launcher settings. It is enabled by default to preserve the behavior of earlier versions.

When system backup is enabled, Android may back up eligible e_e launcher application data through an Android **cloud** backup transport configured on the device. Depending on the device and account configuration, that transport may be operated by Google, the device manufacturer, or another system backup provider.

This transfer is performed by Android's backup system, not by e_e launcher's own networking code. The developer of e_e launcher does not receive a copy of the backup.

When system backup is disabled inside e_e launcher, the application's backup agent intentionally provides no application files for Android full cloud backup. Android 12+ `dataExtractionRules` explicitly exclude app data from **direct device-to-device transfer**, regardless of this switch. This conservative policy avoids some Android/OEM direct-transfer paths bypassing the in-app switch; use the manual JSON export/import when moving to a different phone. Old cloud backups created before disabling the switch are not automatically deleted by e_e launcher. Android/OEM backup behavior still needs confirmation on devices.

## Local storage

Launcher settings and state are stored locally in the application's private storage unless the user explicitly exports a manual backup or enables Android cloud system backup.

Removing application data or uninstalling e_e launcher may remove locally stored launcher configuration, subject to Android's own backup and restore behavior.

## Third-party services and applications

e_e launcher can launch installed applications, open Android system settings, host widgets, use icon packs, and invoke system interfaces such as the uninstall confirmation screen.

Those applications, providers, and Android system components operate under their own privacy policies and permissions. Their processing is separate from e_e launcher.

## Changes to this policy

This policy may be updated if e_e launcher's data handling, permissions, backup behavior, or optional system integrations change. Material privacy-related changes should be documented in the project repository and release notes.

## Contact

Privacy questions and issues can be raised through the e_e launcher project's public repository.
