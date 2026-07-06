## Features
- Added a follow mute list to hide muted users from your feeds
- Remote signer updated to respond to the latest protocol changes
- Tapping the Explore tab now opens search

## Improvements
- Faster app startup
- Improved the profile context menu
- Faster feed loading and lower memory use
- Feeds now prefetch on startup for a faster first load
- Reliable connections with automatic connection recovery
- Removed the redundant timestamp from the highlighted note in a thread
- Reserved space for the "Followed by" section to reduce layout shift on profiles

## Fixes
- Single-image notes now render at full content width
- Fixed top-zap avatars flashing when opening a thread
- Fixed live stream pills appearing before the feed finished loading
- Feed media prefetch now stops when you leave a feed
- Fixed incorrect image ordering in the feed media pre-loader
- Capped media pre-cache concurrency to keep loading order stable
- Fixed connections stalling when a relay stopped responding
- Fixed request timeouts not being reflected in the UI
- Fixed invalid entries being pulled in from follow lists
