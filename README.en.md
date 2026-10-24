<p align="center">
  <img src="art/shattered-melodies-logo-horizontal.png" width="760" alt="Poris Elegies: Shattered Melodies — a rose and a shattered record">
</p>

# Poris Elegies: Shattered Melodies

[简体中文](README.md) · English

A fractured story. A fractured melody.

After all you've been through, do you still remember her?

**0.1 Beta** · **NeoForge 1.21 / 1.21.1 · Forge 1.20.1** · **Client-side only**

[Download 0.1 Beta](https://github.com/PorisRora/poris-elegies-shattered-melodies/releases/tag/v0.1-beta)

## What you'll hear

- **36 tracks** for the main menu, exploration, caves, underwater areas, the Nether and the End.
- **Music that follows your surroundings** in both Survival and Creative modes.
- **Gentle transitions**, with fades, pauses between tracks and rules that reduce immediate repeats.
- **On-demand playback**: browse the track list, request a song or choose music for your current surroundings.
- **Configurable frequency and coexistence modes** to control how often music plays and how it interacts with other music.

Minecraft's **Master Volume** and **Music** sliders control playback volume.

## Installation

Choose the file for your Minecraft version and mod loader:

| Minecraft | Loader | Java | File |
| --- | --- | --- | --- |
| **1.21** | NeoForge **21.0.143+**, within the 21.0 branch | 21 | `poris-elegies-shattered-melodies-neoforge-1.21-1.21.1-0.1-beta.jar` |
| **1.21.1** | NeoForge **21.1.1+**, within the 21.1 branch | 21 | The same NeoForge file as above |
| **1.20.1** | Forge **47.2.0+**, within the 47 branch | 17 | `poris-elegies-shattered-melodies-forge-1.20.1-0.1-beta.jar` |

1. Install the matching mod loader and Java version.
2. Put **one matching JAR** in your instance's `mods` folder. Do not install the Forge and NeoForge files together.
3. When updating, remove the old Shattered Melodies JAR from `mods`, add the new version, then restart Minecraft completely.

For multiplayer, install it on your client only; the server does not need it.

Support currently covers the three Minecraft versions listed above. Other 1.21.x and 1.20.x versions are outside this release's support range.

## Settings

Open **Mods → Poris Elegies: Shattered Melodies → Config**.

**Main menu music** and **In-game music** can be switched off separately. Disabling either restores vanilla background music in that setting. The small **Now Playing** card is optional too.

Music frequency controls the pause after a track ends naturally:

| Frequency | Pause between tracks |
| --- | --- |
| Frequent, the default | 1–3 minutes |
| Standard | 4–8 minutes |
| Sparse, vanilla pace | 10–20 minutes |

## Playing alongside other music

Choose a **Music coexistence mode** in Config:

| Mode | Behaviour |
| --- | --- |
| **Normal**, the default | Allows music from other mods and music discs to play alongside Shattered Melodies. Tracks may overlap. |
| **Yield** | Fades out while external music is audible, then resumes music selection after it ends. |
| **Exclusive** | Blocks external background music and music discs while Shattered Melodies is enabled in the current setting. |
| **Whitelist** | Allows only listed external music and yields while it plays. |

Whitelist entries are sound event IDs, such as `minecraft:music_disc.cat`. Use `namespace:*` to allow all music from a namespace. The list is empty by default and only applies in Whitelist mode.

In the Forge config screen, separate entries with commas. Invalid IDs appear in red and must be corrected before saving.

These modes affect background music and music discs. Footsteps, combat, weather, note blocks and other effects continue normally. Mods using a separate audio player may not be affected.

## Requesting a song

Enter these commands in chat after joining a world. They support Tab completion, work in multiplayer and **do not require cheats**.

| Command | Action |
| --- | --- |
| `/poris melodies list` | List all tracks and their numbers |
| `/poris melodies play 02c` | Play track `02c`; replace it with another track number |
| `/poris melodies here` | Play music for your current surroundings |
| `/poris melodies next` | Choose another track from the current location's pool |
| `/poris melodies stop` | Stop the current song; automatic selection resumes later |
| `/poris melodies now` | Show the current song, progress or reason for waiting |

**Play music for this place**, **Next track** and **Show now playing** also have key bindings in Minecraft's Controls menu. They are unbound by default.

## Full track list

These 36 tracks are included in 0.1 Beta. Request one in-game with `/poris melodies play <number>`, for example `/poris melodies play 02c`.

<details>
<summary>Show all 36 tracks</summary>

Locations indicate where a track can be selected; playback follows the configured intervals and selection rules. Creative mode also follows biome music; “Generic Creative areas” means places without dedicated biome music.

| No. | English title | Original title | Locations |
| --- | --- | --- | --- |
| `01` | The Witch's Requiem | 魔女の鎮魂歌 | Main menu |
| `01b` | Her Footprints | 彼女の足あと | Main menu |
| `02` | A Walk Along the Star Chart | 星図をたどる散歩道 | General Overworld exploration; Generic Creative areas; Forest; Old growth taiga; Meadow |
| `02b` | Silver Afternoon | 銀色の午後 | General Overworld exploration; Generic Creative areas; Flower forest; Meadow; Cherry grove |
| `02c` | The Familiar Road | いつもの道 | General Overworld exploration; Generic Creative areas |
| `03` | A Castle on a Blank Page | 白紙の頁に城を建てる | Generic Creative areas |
| `04` | Paths Through the Garden Labyrinth | 庭園迷宮の小径 | Forest; Old growth taiga |
| `05` | Rose Garden Waltz | 薔薇園のワルツ | Flower forest; Cherry grove |
| `06` | The Forest Cathedral | 森の大聖堂 | Forest; Old growth taiga; Swamp |
| `07` | Ruins Sleeping Beneath the Ivy | 蔦の下に眠る遺跡 | Jungle; Sparse jungle; Bamboo jungle |
| `08` | Sunlit Terrace | 陽だまりのテラス | Jungle; Sparse jungle; Bamboo jungle |
| `09` | A Teahouse Deep in the Bamboo | 竹林の奥の茶室 | Jungle; Sparse jungle; Bamboo jungle |
| `10` | The Marsh Witch's Tea Party | 沼の魔女のお茶会 | Swamp |
| `11` | The Mountain Wind Brings Word of You | 山風が君に便りを運ぶ | General Overworld exploration; Generic Creative areas; Meadow; Jagged peaks; Stony peaks |
| `12` | Before the Petals Fall | 散りゆく前に | Flower forest; Cherry grove |
| `13` | Temple in the Sands | 砂の神殿 | Desert; Badlands |
| `14` | Rust-Coloured Cliffs | 錆色の断崖 | Desert; Badlands |
| `15` | Snow Dances in the Cloister | 回廊に雪が舞う | Grove; Snowy slopes; Frozen peaks |
| `16` | A Home Beyond the Blizzard | 吹雪の向こうに家がある | Grove; Snowy slopes; Frozen peaks |
| `17` | Chapel of Frozen Stars | 凍てつく星の礼拝堂 | Grove; Snowy slopes; Frozen peaks |
| `18` | An Oath Above the Peaks | 峰々の上で誓う | Jagged peaks |
| `19` | Stone Steps to the Sky | 天へ続く石段 | Jagged peaks; Stony peaks |
| `20` | The Underground Conservatory | 地下の温室 | Lush caves; Dripstone caves |
| `21` | The Dripping Clock Tower | 雫の時計塔 | Lush caves; Dripstone caves |
| `22` | The Silent Ancient City | 静寂の古代都市 | Deep dark |
| `23` | The Sunken Ballroom | 海に沈んだ舞踏室 | Underwater music areas |
| `24` | Hellfire Elegy | 業火の挽歌 | Nether wastes |
| `24b` | The Witch's Withering | 魔女の凋零 | Nether wastes |
| `25` | Crimson Banquet | 緋色の晩餐会 | Crimson forest |
| `26` | Requiem for Departed Souls | 亡魂のレクイエム | Soul sand valley |
| `27` | The Blackstone Tribunal | 黒岩の審問廷 | Basalt deltas |
| `28` | A Chessboard at the Edge of the Star Sea | 星の海の果ての盤面 | End exploration |
| `29` | Born of the End | Born of the End | Ender Dragon battle |
| `30` | A Candle's Worth of Courage | 蝋燭一本分の勇気 | Ordinary Overworld underground |
| `30b` | Until the Torches Burn Out | 松明が尽きるまで | Ordinary Overworld underground |
| `30c` | The Way Home Is Still Far Away | 帰り道はまだ遠い | Ordinary Overworld underground |

“Ordinary Overworld underground” requires at least 12 blocks below the surface and no skylight; lush caves, dripstone caves and the deep dark retain their own music. Tracks 02, 02b, 02c and 11 also serve as the fallback for unmapped music contexts. The warped forest and End poem/credits currently stay quiet.

</details>

## No music?

Check Master Volume, Music volume and the relevant music toggle in Config. Silence between tracks or while yielding is expected; `/poris melodies now` explains the current state.

For a bug report, include your Minecraft, loader and Shattered Melodies versions, the location and coexistence mode when it happened, and `logs/latest.log`. This is a Beta release; feedback from modpacks is welcome.

## Building from source

Install JDK 21 and run from the project root:

```powershell
.\gradlew.bat build :forge:reobfJar
```

On Linux / macOS, use `./gradlew build :forge:reobfJar`. The Gradle Wrapper downloads the pinned Gradle version; toolchain provisioning obtains JDK 17 for Forge when needed. The first build needs internet access.

NeoForge output is in `build/libs/`; Forge output is in `forge/build/libs/`. All 36 game-ready OGG files are included, so no separate audio conversion is needed. See [ASSET_LICENSE.md](ASSET_LICENSE.md) for local build and asset permissions.

## Music permissions and credits

**Source code: MIT. Music, images and logos: All Rights Reserved.** See [LICENSE](LICENSE) and [ASSET_LICENSE.md](ASSET_LICENSE.md) for their respective scopes. The separate music permissions below apply.

You may play the mod, include an unmodified official release in a modpack, and keep its in-game music in gameplay videos and livestreams, including monetized content.

**Extracting, adapting or distributing the music separately, or using it in another project, requires permission.** See the [music licence](MUSIC_LICENSE.md) for the full terms and [music credits](MUSIC_CREDITS.md) for production details. The open-source code licence does not extend to the music or artwork.

---

The interface supports English and Simplified Chinese and follows Minecraft's language setting.
