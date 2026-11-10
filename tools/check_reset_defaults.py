#!/usr/bin/env python3
"""Static consistency checks for e_e launcher 0.42.0 defaults and reset UI.

These checks do not replace an Android build or on-device destructive-reset testing.
"""
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parent.parent
source = root / 'app/src/main/java/org/collegiumdesideriorum/e_e_launcher'
main = (source / 'MainActivity.kt').read_text()
effects = (source / 'EnvironmentEffects.kt').read_text()
dialogs = (source / 'LauncherDialogStyle.kt').read_text()
reset = (source / 'LauncherReset.kt').read_text()

expect = {
    'folder initial speed 90': 'getInt(KEY_FOLDER_FAN_SPEED, 90)' in main,
    'folder retention 95': 'getInt(KEY_FOLDER_FAN_DECAY, 95)' in main,
    'app drawer frame disabled': 'KEY_APP_DRAWER_FRAME_SHAPE, AppItemFrameShape.NONE' in main,
    'app drawer opacity 65': 'getInt(KEY_APP_DRAWER_BACKGROUND_OPACITY, 65)' in main,
    'dialog opacity 50': 'val opacityPercent: Int = 50' in dialogs and 'json.optInt("opacityPercent", 50)' in dialogs,
    'default text shadow on': 'val enabled: Boolean = true' in main and 'getBoolean(KEY_TEXT_SHADOW_ENABLED, true)' in main,
    'cloud count 8': 'EnvironmentEffectType.CLOUDS -> 8' in effects,
    'sun ray speed 162': 'speedPercent = 162' in effects,
    'sun ray angle 45': 'sunRayMaxAngleDegrees = 45' in effects,
    'sun ray thickness 60': 'sunRayThicknessPx = 60' in effects,
    'sun ray height 20': 'DEFAULT_SUN_RAY_HEIGHT_PERCENT = 20' in effects,
    'tumbleweed two directions': 'direction = EnvironmentDirection.LEFT_OR_RIGHT, rotation = EnvironmentRotation.CLOCKWISE' in effects,
    'reset preserves side-dock swipe migration': 'key != "side_dock_gesture_migrated_v11"' in reset,
    'full reset has second confirmation': 'area == LauncherResetArea.ALL && !confirmFullResetAgain' in main,
    'widget host released on workspace reset': 'launcherAppWidgetHost.appWidgetIds.forEach(::deleteHostedWidget)' in main,
    'crimson full reset button': 'ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C))' in main,
    'security settings screen exists': 'LauncherSettingsPage.SECURITY -> {' in main,
    'reset screen nested under security': 'LauncherSettingsPage.SECURITY_RESET -> LauncherSettingsPage.SECURITY' in main and 'LauncherSettingsPage.SECURITY_RESET -> {' in main,
    'home role and locks directly on security page': 'AdvancedFeaturesSettings(' in main and 'value = securityDraft' in main and 'onRequestHomeRole' in main,
    'root navigates to security, not reset': 'page = LauncherSettingsPage.SECURITY\n' in main,
    'only security opens reset page': main.count('page = LauncherSettingsPage.SECURITY_RESET') == 1,
    'all six reset areas available': all(f'LauncherResetArea.{name} to R.string.' in main for name in ('WORKSPACE', 'APP_DRAWER', 'SIDE_DOCK', 'BOTTOM_DOCK', 'INTERFACE', 'ALL')),
}
for folder in (root / 'app/src/main/res').glob('values*'):
    path = folder / 'strings.xml'
    if not path.exists():
        continue
    strings = {s.attrib.get('name') for s in ET.parse(path).getroot().findall('string')}
    expect[f'{folder.name} reset strings'] = all(x in strings for x in ('ui_reset_security_title', 'ui_reset_workspace_warning', 'ui_reset_all_second_warning', 'ui_reset_all_launcher'))

for name, ok in expect.items():
    print(f'{"OK" if ok else "FAIL"}: {name}')
if not all(expect.values()):
    raise SystemExit(1)
print('RESET_DEFAULTS_STATIC_CHECKS_OK')
