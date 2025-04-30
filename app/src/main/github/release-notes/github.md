## Features
- Implemented login with public key (#366);
- Implemented Amber support (#376 #378 #415);
- Implemented blossom media uploads & settings (#408 #417 #418 #419 #421 #422 #436);
- Implemented push notifications for Google builds (not available in Github APKs) (#427);
- Implemented notifications revamp (#416)
- Implemented image gallery in article details (#356);
- Implemented muting words, hashtags & threads (#426 #429)
- Implemented new gallery rendering for media attachments in notes (#354);
- Implemented "Share note as image" option in note context menu (#375)
- Implemented Copy Url & Copy Image in media gallery screen (#350);
- Implemented copy to clipboard when tapping on bitcoin or lightning address in wallet (#360);
- Implemented topological sort for FeedPost on threads (#437);
- Implemented Request Delete action for notes & articles;
- Simplified note context menu (#441);

## Improvements
- Fixed back navigation on Nostr Recipient tab in wallet create tx (#370);
- Fixed missing legend customization on Zap items (#367);
- Fixed edges on feed gallery in notes (#365);
- Fixed profile details not updating on screen start;
- Fixed click indication on wallet receive screen for copy lud16/btc address;
- Fixed primal.net not recognized as url (#358);
- Fixed hashtag rendering for edge case (#artstr #art) (#359);
- Fixed showing error message from api when invoice creation fails (#363);
- Fixed new notes pill doesn't show default avatars (#357);
- Fixed copy to use lnurl instead of lud16 on wallet receive lightning screen (#361);
- Fixed occasional legend avatars flickering in the app (#362);
- Fixed broken article and highlight quoting (#373);
- Fixed re-uploading of media shared through system's share on new note screen (#384);
- Fixed feed flickering when opening/closing feeds list (#423)
- Fixed crash when quoting in NoteEditorViewModel (#377)
- Fixed networking race conditions (#434)
- Fixed muted users appearing in new posts pill (#438)

## Maintenance
- Started with decoupling the app into KMP modules;
- Decoupled local caching layer from the app into a KMP library;
- Decoupled blossom uploader into a KMP library;
- Reimplemented networking layer (from Android OkHttp to KMP Ktor Client);