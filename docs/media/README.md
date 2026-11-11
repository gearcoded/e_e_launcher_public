# README demonstration media

The four videos on the [main README](../../README.md) were recorded by the project maintainer on a demonstration workspace and edited for public presentation.

| Video | Description | Approx. duration |
| --- | --- | ---: |
| [`workspace_transitions.mp4`](workspace_transitions.mp4) | Home screen transitions | 31 s |
| [`environment_effects.mp4`](environment_effects.mp4) | Animated effects over the workspace | 20 s |
| [`app_drawer_transitions.mp4`](app_drawer_transitions.mp4) | App drawer page transitions | 10 s |
| [`docks_and_folders.mp4`](docks_and_folders.mp4) | Side/bottom docks and folder icons | 12 s |

All demonstration videos are silent MP4 files encoded with H.264 at **540 × 1200, approximately 60 FPS**. The corresponding `*_poster.jpg` files are static frames with a play icon for use as clickable previews in the README. Full resolution source recordings and editing projects are deliberately not included.

## GitHub presentation

The README links the four posters to their MP4 files in this directory. This is a portable approach with no dependency on an external service. GitHub does **not** reliably turn an ordinary repository-relative MP4 link into an inline player; users open a separate video page or download the file.

If inline playback in GitHub's README is desired:

1. In a GitHub Markdown editor that accepts attachments (for example an Issue comment), drag in the approved MP4. GitHub will generate an attachment URL like `https://github.com/user-attachments/assets/…`.
2. Paste the **actual generated URL** into README on its own line (not inside a Markdown table or link). GitHub may render this as a clickable inline player.
3. Verify the result on the public repository page before replacing the reliable static-poster gallery.

Do not invent attachment URLs or commit a placeholder that resembles a real URL. The 0.50.0 public tag already contains the four MP4 files; deleting files in a later commit would not remove them from that published tag or existing Git history. Preserve release history and verify the alternative playback experience before changing existing README links.

## Media and privacy

The recordings may show third-party app icons or widget designs, which remain the property of their respective owners. Appearance in a demonstration does not imply endorsement. Check video frames for personal notifications, accounts, or identifying data before public distribution.

Project-created documentation and images follow the rights and licensing policies described in [`ASSET_PROVENANCE.md`](../../ASSET_PROVENANCE.md) and [`THIRD_PARTY_LICENSES.md`](../../THIRD_PARTY_LICENSES.md). Do not assume the licenses for the e_e launcher source automatically apply to third-party elements visible on screen.
