# x0s.link — Android (Kotlin + Jetpack Compose + Material 3)

A native Android port of the x0s.link Social OS web app: Feed, Profile, Disha AI search,
Zynx reels, Messages, and Favourites (music/movies/series/anime), built with Material 3
and the same AMOLED black / iOS-blue accent design language as the web app.

## What's inside

```
app/src/main/java/com/x0s/link/
├── XosApplication.kt          Owns the repositories for the app's lifetime
├── MainActivity.kt            NavHost + bottom navigation, wires all screens together
├── data/
│   ├── model/Models.kt        XosProfile / XosPost / XosCollege / FavItem / API DTOs
│   ├── remote/
│   │   ├── JsObjectParser.kt  Converts profiles.js / colleges.js JS literals -> JSON
│   │   ├── RemoteDataSource.kt Fetches + parses the live JS files, with asset fallback
│   │   ├── Apis.kt            Retrofit interfaces: iTunes, TMDB, Jikan, TVMaze
│   │   └── NetworkModule.kt   OkHttp / Retrofit singletons
│   ├── local/Prefs.kt         DataStore-backed likes/saves/follows/edits/favourites
│   └── repository/
│       ├── ProfileRepository.kt
│       └── FavouritesRepository.kt
├── ui/
│   ├── theme/                 Color.kt, Type.kt, Theme.kt (Material3, XOS AMOLED palette)
│   ├── nav/Destinations.kt    Route constants + bottom-tab definitions
│   ├── components/            Reusable pieces: verified badge, avatars, pills
│   ├── feed/                  Home feed (image/video posts, like/save)
│   ├── postdetail/            Post detail bottom sheet with pager + comments/likes
│   ├── profile/                Profile screen (floating card over banner) + Edit Profile
│   ├── search/                 "Disha" AI-style chat search over profiles + colleges
│   ├── colleges/               College search + college profile
│   ├── messages/                Conversation list + chat screen
│   ├── favourites/              Music/Shows favourites with live search & add
│   └── zynx/                   Vertical reels (Media3 ExoPlayer)
```

## How the data layer works ("script extraction")

`profiles.js` / `colleges.js` on the web app are **not JSON** — they're plain JS files like:

```js
window.XOS_PROFILES = window.XOS_PROFILES || { nikhil: { userid:'nikhil', ... }, ... };
```

`data/remote/JsObjectParser.kt` extracts the literal assigned to `window.XOS_PROFILES` /
`window.XOS_COLLEGES`, strips comments, converts single-quoted strings and unquoted keys
into valid JSON, and removes trailing commas — turning the JS literal into strict JSON that
Gson can parse directly into `XosProfile` / `XosCollege`.

`RemoteDataSource.kt` fetches those files straight from the GitHub Pages deployment
(`https://unknownxsuperman-prog.github.io/anushdecodes/profiles.js` and `colleges.js`) at
runtime. If the network is unavailable, or the site's structure changes in a way the parser
can't handle, it automatically falls back to the bundled `assets/profiles_fallback.json` /
`assets/colleges_fallback.json`, so the app always has data to show.

**To point this at your own deployment:** edit `BASE_URL` in `RemoteDataSource.kt`.

## Favourites: music / movies / series / anime

Mirrors `xos-favourites.html`:
- **Tracks & Albums** → iTunes Search API (no key required)
- **Movies / Series / Anime** → TMDB if you add a key in `data/remote/Apis.kt` →
  `TmdbConfig.API_KEY`; otherwise it automatically falls back to free public sources exactly
  like the web app does: iTunes for movies, **TVMaze** for series, **Jikan** for anime.
- Song previews play through `MediaPlayer` with a mini player bar at the bottom.

## Setting this up in Android Studio

1. Create a new **Empty Activity (Compose)** project in Android Studio (this generates a
   working Gradle wrapper for you).
2. Delete the generated `app/src/main/java/**` and `app/src/main/res/values/*` Android
   Studio created, and copy in everything from this zip's `app/` folder (and the root
   `settings.gradle.kts` / `build.gradle.kts` / `gradle.properties` if you want the exact
   dependency versions used here).
3. Sync Gradle.
4. (Optional) Drop Space Grotesk `.ttf` files into `res/font/` and follow the instructions
   at the top of `ui/theme/Type.kt` to wire up the real typeface — the app builds and runs
   fine without this step, just falls back to the system sans-serif font.
5. (Optional) Add a TMDB API key in `data/remote/Apis.kt` for richer movie/series metadata.
6. Run on a device or emulator with internet access.

## Notes

- All network calls are read-only public APIs (iTunes, TMDB, Jikan, TVMaze) plus fetching
  the two public JS data files — no backend/auth required, matching the original static
  GitHub Pages app.
- Likes, saves, follows, linked college, profile edits and favourites persist locally via
  Jetpack DataStore (`data/local/Prefs.kt`), scoped per user id, mirroring the web app's use
  of `localStorage`.
- Messages currently use local demo data (same shape as `MSGS_DATA` in the web app) since
  there's no backend — swap `MessagesViewModel` for a real API/WebSocket source when you're
  ready to wire up live chat.
