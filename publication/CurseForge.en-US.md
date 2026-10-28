![Poris Elegies: Shattered Melodies](https://raw.githubusercontent.com/PorisRora/poris-elegies-shattered-melodies/main/art/shattered-melodies-logo-horizontal.png)

# Poris Elegies: Shattered Melodies

A fractured story. A fractured melody.

After all you've been through, do you still remember her?

## About

Shattered Melodies is a client-side Minecraft music mod built around an atmosphere of remembrance. Its 38 tracks follow your surroundings, from the title screen and quiet forests to underground passages, the Nether and the Ender Dragon battle.

It works in both Survival and Creative modes, with fades and pauses between tracks. You can also request a favourite song whenever you want to hear it.

## Features

- Environment-based music for the main menu, Overworld biomes, caves, underwater areas, the Nether, the End and the Ender Dragon battle.
- Separate menu and in-game music toggles, adjustable playback frequency and an optional Now Playing card.
- Four music coexistence modes: Normal, Yield, Exclusive and Whitelist.
- Commands for browsing tracks, requesting songs, choosing music for your surroundings and checking playback status.
- English and Simplified Chinese interfaces and track titles, following Minecraft's language setting.

## Installation

**Current release: 0.2.2 Beta. Client-side only; no server installation is needed.**

| Minecraft | Loader | Java |
| --- | --- | --- |
| 1.21 | NeoForge 21.0.143+, within the 21.0 branch | 21 |
| 1.21.1 | NeoForge 21.1.1+, within the 21.1 branch | 21 |
| 1.20.1 | Forge 47.2.0+, within the 47 branch | 17 |

Put the matching JAR in your instance's `mods` folder. Use either the NeoForge or Forge file, according to your loader.

Open **Mods → Poris Elegies: Shattered Melodies → Config** to adjust playback. Minecraft's **Master Volume** and **Music** sliders control the volume.

In a world, use `/poris melodies list` to browse tracks, `/poris melodies play 02c` to request a song, or `/poris melodies now` to check playback. Cheats are not required.

## Track list (38 tracks)

Locations indicate where a track can be selected; playback follows the configured intervals and selection rules. Creative mode also follows biome music; “Generic Creative areas” means places without dedicated biome music.

| No. | English title | Original title | Locations |
| --- | --- | --- | --- |
| `01` | The World She Left Behind | 彼女が遺した世界 | Main menu |
| `01b` | Her Footprints | 彼女の足あと | Main menu |
| `02` | A Walk Along the Star Chart | 星図をたどる散歩道 | General Overworld exploration; Generic Creative areas; Forest; Old growth taiga; Meadow |
| `02b` | Silver Afternoon | 銀色の午後 | General Overworld exploration; Generic Creative areas; Flower forest; Meadow; Cherry grove |
| `02c` | The Familiar Road | いつもの道 | General Overworld exploration; Generic Creative areas |
| `03` | A Castle on a Blank Page | 白紙の頁に城を建てる | Generic Creative areas |
| `04` | Bookmark in the Dappled Light | 木漏れ日の栞 | Forest; Old growth taiga |
| `05` | Waltz of the Unclaimed Rose | 主なき薔薇のワルツ | Flower forest; Cherry grove |
| `06` | Dream of the Star-Fallen Sea of Trees | 星降る樹海の夢 | Forest; Old growth taiga; Swamp |
| `07` | Ancient Pages Beneath the Vines | 蔦に眠る古の頁 | Jungle; Sparse jungle; Bamboo jungle |
| `08` | Sunlit Terrace | 陽だまりのテラス | Jungle; Sparse jungle; Bamboo jungle |
| `09` | Moonshade Wet with Bamboo Dew | 竹露に濡れる月影 | Jungle; Sparse jungle; Bamboo jungle |
| `10` | Song of the Mist and Lethe Water | 霧のほとり、忘れ水の歌 | Swamp |
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
| `24c` | Grimoire Scattered in Ashes | 灰に散る魔導書 | Nether wastes |
| `25` | Nocturne of the Crimson Woods | 紅き森の夜想曲 | Crimson forest |
| `25b` | Fantasia of the Crimson Woods | 紅き森の幻想曲 | Crimson forest |
| `26` | Requiem of the Pale Flame | 蒼き炎の鎮魂歌 | Soul sand valley |
| `27` | Judgment Upon the Obsidian | 黒曜に立つ審判 | Basalt deltas |
| `28` | A Chessboard at the Edge of the Star Sea | 星の海の果ての盤面 | End exploration |
| `29` | Born of the End | Born of the End | Ender Dragon battle |
| `30` | A Candle's Worth of Courage | 蝋燭一本分の勇気 | Ordinary Overworld underground |
| `30b` | Until the Torches Burn Out | 松明が尽きるまで | Ordinary Overworld underground |
| `30c` | The Way Home Is Still Far Away | 帰り道はまだ遠い | Ordinary Overworld underground |

“Ordinary Overworld underground” requires at least 12 blocks below the surface and no skylight; lush caves, dripstone caves and the deep dark retain their own music. Tracks 02, 02b, 02c and 11 also serve as the fallback for unmapped music contexts. The warped forest and End poem/credits currently stay quiet.

## Credits and permissions

**Source code: [MIT](https://github.com/PorisRora/poris-elegies-shattered-melodies/blob/main/LICENSE). Music, images and logos: [All Rights Reserved](https://github.com/PorisRora/poris-elegies-shattered-melodies/blob/main/ASSET_LICENSE.md).** The code licence does not extend to the music or artwork.

Project direction, track selection and publication by Rooboko. Music was generated using Suno; lyrics and prompts were created with Claude assistance. The author selected the material and prepared the audio for in-game playback.

Unmodified official releases may be included in modpacks. In-game music may remain in gameplay videos and livestreams featuring the mod, including monetized content. Extracting, adapting or distributing the music separately, or using it in other projects, requires separate permission. See the [music permissions](https://github.com/PorisRora/poris-elegies-shattered-melodies/blob/main/MUSIC_LICENSE.md) for the full terms.

[Source code](https://github.com/PorisRora/poris-elegies-shattered-melodies) · [Report an issue](https://github.com/PorisRora/poris-elegies-shattered-melodies/issues)
