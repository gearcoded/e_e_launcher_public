# Asset provenance

This file documents the intended origin and licensing treatment of graphical
assets shipped with **e_e launcher**.

## Project-created / ChatGPT-assisted artwork

Custom project graphics, pictograms and launcher artwork that are not identified
as third-party assets were **generated with ChatGPT from prompts supplied by the
project maintainer/user**, then selected, directed and, where necessary, adapted
by the maintainer for use in e_e launcher. Android Asset Studio or Android
resource tooling may subsequently have been used to generate density-specific or
adaptive-icon variants from that project artwork.

Examples include the current e_e launcher visual identity and launcher artwork,
including the hedge-house / garden-labyrinth design and project-specific UI
pictograms that are not derived from Google Material Icons.

To the extent that copyright, related rights, database rights or similar rights
exist in these project-created assets and are held by the project maintainer, those
rights are offered under **Creative Commons Attribution-ShareAlike 4.0
International (CC BY-SA 4.0)**.

Suggested attribution for redistribution of these project-created assets:

> e_e launcher project artwork, Collegium Desideriorum project identity; generated
> with ChatGPT from prompts supplied by and under the direction of the
> project maintainer; CC BY-SA 4.0.

When redistributing or adapting these assets, also identify any changes you make,
keep the CC BY-SA 4.0 license notice/link, and license adaptations under the same
license as required by CC BY-SA 4.0.

The statement above is provenance, not a claim that ChatGPT or OpenAI is the
copyright owner. AI-assisted output can have different copyright treatment in
different jurisdictions; the CC license therefore applies only to rights that the
project maintainer can legally license.

A copy of the CC BY-SA 4.0 legal code is included at `licenses/CC-BY-SA-4.0.txt`.
License information: https://creativecommons.org/licenses/by-sa/4.0/

## Third-party vector icons

Google Material Icons and any other assets explicitly identified in
`THIRD_PARTY_LICENSES.md` retain their own upstream licenses. They are not relicensed
under CC BY-SA 4.0 merely because they are stored in the same Android resource tree.

## Drawable inventory (reviewed for 0.48.6)

The source archive contains 18 pictogram vector drawables plus one functional note-widget background drawable in
`app/src/main/res/drawable/`. Their paths are classified as follows:

- **Material or conservatively treated as Material-derived (14):**
  `ic_apps.xml`, `ic_close.xml`, `ic_delete.xml`, `ic_drag.xml`,
  `ic_edit.xml`, `ic_folder.xml`, `ic_info.xml`, `ic_map.xml`,
  `ic_message.xml`, `ic_phone.xml`, `ic_search.xml`, `ic_settings.xml`,
  `ic_sort.xml`, `ic_undo_rounded.xml`. The original geometry may have
  been simplified or adapted; see `THIRD_PARTY_LICENSES.md` for the
  item-by-item treatment. They retain any applicable Apache-2.0 rights.
- **Project-specific vectors (4):** `ic_app_drawer_effect.xml`,
  `ic_environment.xml`, `ic_launcher.xml`, and
  `ic_workspace_effect.xml`. These are treated as project-created
  pictograms/artwork, not as verified copies of Material icon paths.

- **Functional note background (1):** `launcher_note_widget_background.xml`, a simple original rounded rectangle shape created for the built-in text widget (functional XML resource, GPL-3.0-or-later).

The launcher branding additionally includes generated density variants
`app/src/main/res/mipmap-*/e_e_launcher_icon*.webp`, adaptive icon
configuration in `mipmap-anydpi-v26`, and color resources. The generated
bitmap variants are grouped with project artwork; underlying Android resource
configuration follows the functional resource rules below.

For project-created assets, the CC BY-SA 4.0 designation applies only to
rights legally held by the maintainer. For assets using third-party shapes,
any surviving third-party rights take precedence. Some attribution details
are conservatively classified because exact creation records for each path
are not embedded in the Android source archive.

## Functional Android resources

XML and other resources whose primary purpose is functional application behaviour
rather than standalone artwork (for example manifests, configuration XML, strings,
themes and similar application resources) follow the source-code license,
`GPL-3.0-or-later`, unless a file explicitly says otherwise.

## Future assets

When adding a new image, icon, font, sound or other media file, record at least:

- original creator/source or the fact that it was created for the project;
- whether AI assistance was used;
- the upstream license if it is third-party;
- required attribution/copyright notice;
- whether the file was modified.

Do not place an asset of unknown origin into a public release until its redistribution
rights are understood.

## README demonstration recordings (repository media)

Four demonstration MP4 files under `docs/media/` were created from the project maintainer's recordings of the launcher on a demonstration setup. Static JPEG preview images are derived from those recordings and overlaid with a generic play symbol. The videos and stills depict the launcher and may show third-party app icons, widget designs, and other content, whose respective rights remain with their owners. No original recording files or Kdenlive project files are distributed. See `docs/media/README.md` for usage and privacy considerations.


**Source-package note (2026-11-09):** `docs/media/` remains tracked in the
Git repository but is intentionally absent from compact developer ZIPs. This
is a delivery-size decision, not an assertion that repository media or README
video links are missing. Preserve existing media when applying ZIP updates.
