#!/usr/bin/env python3
"""Source-only release gates for e_e launcher (version read from Gradle).

Does not replace Android Gradle compilation, APK inspection or on-device tests.
"""
from pathlib import Path
import re
import hashlib
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parent.parent
base = root / 'app/src/main/java/org/collegiumdesideriorum/e_e_launcher'
mesh = (base / 'MeshPageEffects.kt').read_text(encoding='utf-8')
main = (base / 'MainActivity.kt').read_text(encoding='utf-8')
dist = (base / 'EnvironmentDistortionEffect.kt').read_text(encoding='utf-8')
build = (root / 'app/build.gradle.kts').read_text(encoding='utf-8')
licenses = (root / 'THIRD_PARTY_LICENSES.md').read_text(encoding='utf-8')
provenance = (root / 'ASSET_PROVENANCE.md').read_text(encoding='utf-8')

checks = {
    'positive versionCode and semantic versionName':
        re.search(r'versionCode\s*=\s*([1-9]\d*)', build) is not None and
        re.search(r'versionName\s*=\s*"\d+\.\d+\.\d+"', build) is not None,
    'previously approved page effect IDs remain present':
        all(effect in mesh for effect in ('SPHERE(', 'CYLINDER(')) or
        ('SPHERE' in mesh and 'CYLINDER' in mesh),
    'backup restore validates before committing and has rollback':
        all(token in (base / 'LauncherSettingsBackup.kt').read_text(encoding='utf-8') for token in (
            'parsePreferences(root.getJSONObject("preferences")',
            'validateWidgetOptions(widgetOptions)',
            'val widgetPlan = planWidgetRestore(',
            'rollbackErrors.forEach(failure::addSuppressed)'
        )),
    'Android 12+ cloud/D2D extraction rules are declared':
        'android:dataExtractionRules="@xml/data_extraction_rules"' in
        (root / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8') and
        (root / 'app/src/main/res/xml/data_extraction_rules.xml').is_file(),
    'bounded user-selected background decoding':
        'decoder.setTargetSize(' in (base / 'LauncherImageDecoding.kt').read_text(encoding='utf-8'),
    'historical third-party attribution removed from shader comments':
        'Microsoft Plus! Science' not in dist,
    'app drawer overview request consumed once':
        'currentOnOverviewRequestConsumed()' in main and
        'onOverviewRequestConsumed = { appDrawerOverviewRequestSerial = 0 }' in main,
    'locked desktop rejects shortcut creation':
        'fun addShortcut(app: AppItem, page: Int, xFraction: Float, yFraction: Float) {\n        if (workspaceEditingLocked)' in main,
    'locked desktop blocks widget gesture handling':
        'if (!editingAllowed)' in main and 'onDragStart = { currentOnEditingLocked() }' in main,
    'localized desktop and dock lock feedback':
        'fun notifyDesktopEditingLocked()' in main and
        'fun notifyDockEditingLocked()' in main and
        all('ui_desktop_editing_is_locked' in (root / f'app/src/main/res/{res}/strings.xml').read_text(encoding='utf-8')
            and 'ui_dock_editing_is_locked' in (root / f'app/src/main/res/{res}/strings.xml').read_text(encoding='utf-8')
            for res in ('values', 'values-ru', 'values-de', 'values-fr', 'values-es', 'values-pt-rBR', 'values-ja', 'values-zh-rCN')),
    'folder reordering remains available while locked':
        'fun reorderFolderApp(folderId: String, fromIndex: Int, toIndex: Int) {\n        if (workspaceEditingLocked)' not in main and
        'fun reorderBottomDockFolderApp(folderId: String, fromIndex: Int, toIndex: Int) {\n        if (bottomDockEditingLocked)' not in main and
        'onExtractionLocked = ::notifyDesktopEditingLocked' in main,
    'app drawer detects locked dock independent of insertion previews':
        'bottomDockZone && bottomDockEditingLocked -> notifyDockEditingLocked()' in main and
        'bottomDockBoundsInRoot = it.boundsInRoot()' in main and
        'measuredDock.contains(position)' in main,
    'desktop shortcuts report locked dock drops':
        'onLockedDockDrop(pointerInRoot) -> Unit' in main and
        'bottomDockBoundsInRoot.contains(pointerInRoot)' in main and
        'sideDockBoundsInRoot.contains(pointerInRoot)' in main,
    'bottom dock drag supports direct removal':
        'bottomDockDeleteDragActive' in main and
        'onDeleteDragStateChange(true, false)' in main and
        'if (isOverDeleteTarget(finalCenter))' in main and
        'else onRemove(moved.id)' in main,
    'independent dock locks in settings and persistence':
        'val sideDockLocked: Boolean = false' in (base / 'LauncherAdvancedFeatures.kt').read_text(encoding='utf-8') and
        'val bottomDockLocked: Boolean = false' in (base / 'LauncherAdvancedFeatures.kt').read_text(encoding='utf-8') and
        'value.copy(sideDockLocked = it)' in (base / 'LauncherAdvancedFeatures.kt').read_text(encoding='utf-8') and
        'value.copy(bottomDockLocked = it)' in (base / 'LauncherAdvancedFeatures.kt').read_text(encoding='utf-8') and
        'LEGACY_DOCK_LOCKED' in (base / 'LauncherAdvancedFeatures.kt').read_text(encoding='utf-8') and
        '.remove(LEGACY_DOCK_LOCKED)' in (base / 'LauncherAdvancedFeatures.kt').read_text(encoding='utf-8'),
    'side and bottom lock checks independent':
        'advancedPreferences.sideDockLocked' in main and
        'advancedPreferences.bottomDockLocked' in main and
        '((bottomDockEditingLocked && overBottomDock) || (sideDockEditingLocked && overSideDock))' in main and
        'if (workspaceEditingLocked || sideDockEditingLocked) return false' in main and
        'if (workspaceEditingLocked || bottomDockEditingLocked) return false' in main,
    'locked dock drag notifies without moving':
        'onLockedDrag = ::notifyDockEditingLocked' in main and
        'onDragStart = { onLockedDrag() }' in main and
        'dragEnabled = !editingLocked' in main and
        'if (editingLocked) {' in main and
        'detectDragGesturesAfterLongPress(' in main,
    'localized independent dock lock toggle labels':
        all('ui_lock_side_dock_changes' in (root / f'app/src/main/res/{res}/strings.xml').read_text(encoding='utf-8')
            and 'ui_lock_bottom_dock_changes' in (root / f'app/src/main/res/{res}/strings.xml').read_text(encoding='utf-8')
            for res in ('values', 'values-ru', 'values-de', 'values-fr', 'values-es', 'values-pt-rBR', 'values-ja', 'values-zh-rCN')),
    'all widget resize arrows draw independently of OEM fonts':
        'Canvas(modifier = Modifier.size(24.dp))' in main and
        all(f'"{arrow}" ->' in main for arrow in ('↔', '↕', '↖', '↘')) and
        'fun arrowhead(tip: Offset, inward: Offset)' in main and
        'Text(label, color = Color(0xFF102016), fontWeight = FontWeight.Bold)' not in main,
    'right/bottom widget resize anchors kept fixed':
        'fun rightResizeWidth(totalDragX: Float): Int' in main and
        'fun bottomResizeHeight(totalDragY: Float): Int' in main and
        main.count('positionToSave = resizeStartPosition') == 3 and
        main.count('temporaryPosition = position') >= 5,
    'left/top widget resize anchors based on gesture start':
        'fun leftResizeWidth(totalDragX: Float): Int' in main and
        'fun topResizeHeight(totalDragY: Float): Int' in main and
        'fun leftEdgeForWidth(newWidthDp: Int): Float' in main and
        'fun topEdgeForHeight(newHeightDp: Int): Float' in main and
        'val newWidth = leftResizeWidth(total.x)' in main and
        'val newHeight = topResizeHeight(total.y)' in main and
        'temporaryPosition = Offset(leftEdgeForWidth(newWidth), topEdgeForHeight(newHeight))' in main and
        'val oldWidthPx = with(density) { widthDp.dp.toPx() }' not in main and
        'val oldHeightPx = with(density) { heightDp.dp.toPx() }' not in main,
    'resize callback freshness':
        'val currentOnDragStart by rememberUpdatedState(onDragStart)' in main and
        'val currentOnDrag by rememberUpdatedState(onDrag)' in main and
        'val currentOnDragEnd by rememberUpdatedState(onDragEnd)' in main,
    'privacy document present':
        (root / 'PRIVACY.md').is_file(),
    'transitive Apache-2.0 attribution':
        all(s in licenses for s in ('org.jetbrains:annotations', 'org.jspecify:jspecify',
                                  'com.google.guava:listenablefuture')),
    'license legal texts present':
        all((root / p).is_file() for p in ('LICENSE', 'licenses/Apache-2.0.txt',
                                          'licenses/CC-BY-SA-4.0.txt')),
    'no INTERNET permission':
        'android.permission.INTERNET' not in (root / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8'),
}

files = sorted((root / 'app/src/main/res/drawable').glob('*.xml'))
checks['all 19 drawable XML resources inventoried'] = (
    len(files) == 19 and all(f.name in provenance for f in files)
)
try:
    for f in (root / 'app/src/main/res').rglob('*.xml'):
        ET.parse(f)
    checks['Android resource XML is well formed'] = True
except ET.ParseError:
    checks['Android resource XML is well formed'] = False


wrapper_jar = root / 'gradle/wrapper/gradle-wrapper.jar'
wrapper_sha256 = hashlib.sha256(wrapper_jar.read_bytes()).hexdigest() if wrapper_jar.is_file() else ''
checks['Gradle 8.13 wrapper JAR matches official SHA-256'] = (
    wrapper_sha256 == '81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f'
)
if not checks['Gradle 8.13 wrapper JAR matches official SHA-256']:
    print('WRAPPER_NOT_VERIFIED: replace gradle/wrapper/gradle-wrapper.jar with the official '
          'Gradle 8.13 wrapper. See RELEASE.md; actual SHA-256: ' + (wrapper_sha256 or '<missing>'))

for title, passed in checks.items():
    print(f'{"OK" if passed else "FAIL"}: {title}')
if not all(checks.values()):
    sys.exit(1)
print('SOURCE_RELEASE_CHECKS_OK (Android SDK build and device testing not performed)')
