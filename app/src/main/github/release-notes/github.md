## Features
- Implement Amber support;
- Implement blossom uploads;
- Implement push notifications;
- Implement npub login (#366);
- Implement image gallery in article details (#356);
- Implement new gallery rendering for media attachments in notes (#354);
- Add Copy Url & Copy Image in media gallery screen (#350);
- Add copy to clipboard when tapping on bitcoin or lightning address in wallet (#360);

## Fixes
- Fix back navigation on Nostr Recipient tab in wallet create tx (#370);
- Fix missing legend customization on Zap items (#367);
- Fix edges on feed gallery in notes (#365);
- Fix profile details not updating on screen start;
- Fix profile details always showing "follow" button;
- Fix click indication on wallet receive screen for copy lud16/btc address;
- Fix primal.net not recognized as url (#358);
- Fix hashtag rendering for edge case (#artstr #art) (#359);
- Show error message from api when invoice creation fails (#363);
- Fix new notes pill doesn't show default avatars (#357);
- Fix copy to use lnurl instead of lud16 on wallet receive lightning screen (#361);
- Fix ProfileDataDao writing null values in PrimalPremiumInfo for legends (#362);