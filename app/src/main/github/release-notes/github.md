## Features
- Follow Packs in Explore
- Detecting pasted nostr uris (note, article) and render them in note editor
- Rendering lightning invoices in note editor
- Immersive mode & improved zooming in gallery screen
- "Unable to connect" indicator
- Added scrolling to top on new notifications in notifications tab (#480)
- GIFs support for feed images (#489)
- Zoom support to feed images
- Added prefix when copying note and article ids (#483)
- Added "via [nostr-app]" on article details (#488)
- Improve wallet settings (#496)

## Fixes
- Display the amount for presets instead of leaving empty field (#476)
- Fixed rendering urls which have nostr ids (#471)
- Fixed rendering urls from zap comments as links (#475)
- Fixed preselecting reactions tabs (#478)
- Fixed issues when scrolling to top on note feeds (#481)
- Fixed crash when zapping too fast (#484 #474)
- Improve regex to detect image patterns in articles (#492)
- Fixed max width on attachments preview (#477)
- Fixed attachments appears below the NoteEditorFooter (#494)
- Fixed always adding `nostr:` prefix when processing quoted referenced events
- Fixed crash on failed network call in `EventZapsMediator`
- Fixed missing zaps on articles
- Add cutting off nested notes in feeds on level 3 and below (#499)
- Fixed TLD trimming issues and add more tests (#502)
- Fixed issue where bookmarked comments appear as articles