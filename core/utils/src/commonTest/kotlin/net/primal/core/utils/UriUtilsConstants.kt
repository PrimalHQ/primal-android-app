package net.primal.core.utils

const val MARKDOWN_WITH_LINKS =
"""
An the first episode of Nostr Review on Bitcoin. Review is out!

https://bitcoin.review/podcast/episode-39/

### Software Releases & Project Updates
#### Clients
- Damus [1.5-2](https://github.com/damus-io/damus/blob/master/CHANGELOG.md)
  - Add new full-bleed video player
  - Add ability to show multiple posts per user in Universe
  - Custom iconography added for other areas of the app
  - Custom iconography for the left navigation
  - Custom iconography for the tab buttons
  - Added dots under image carousel
  - Add profile caching
  - Add mention parsing and fine-grained text selection on description in ProfileView
- Nosotros (correction: NOSTROS!)
  - [v0.3.2.5-alpha](https://github.com/KoalaSat/nostros/releases/tag/v0.3.2.5-alpha)
    - Refactor notifications center
  - [v0.3.2.4-alpha](https://github.com/KoalaSat/nostros/releases/tag/v0.3.2.4-alpha)
    - Fixed some performance issues
  - [v0.3.2.3-alpha](https://github.com/KoalaSat/nostros/releases/tag/v0.3.2.3-alpha)
    - NIP-27 supported and used as main implementation for mentions and reposting
    - Mentions and repost improved in Groups and DMs
- Snort [v0.1.9](https://git.v0l.io/Kieran/snort/releases/tag/v0.1.9)
  - Discover tab, shows trending users/posts from nostr.build
  - New DM styles
  - Mentioned Zapstr tracks are previewed on Snort with player
  - Custom emoji rendering in posts (NIP-30)
  - Lanaguage selector on new user flow
  - ZapPool, support nostr ecosystem by donating a percentage of your zaps
  - Alby NWC link added to NWC connect page
  - SemisolDev follow recommendations on Discover tab
  - Pubkey lists (NIP-51) render inline when mentioned in notes
  - Persian language
  - OpenGraph Image/Video media rendered inside link preview box
  - Option to zap everybody on mentioned pubkey list
  - L402 support for inline media (paywall content)
- Amethyst [v0.55.0](https://github.com/vitorpamplona/amethyst/releases/tag/v0.55.0)
  - Displays NIP-89 Notes in the feed
  - Displays recommendations on profile
  - Displays NIP-89-related notifications for your apps.
- Coracle
  - [0.2.29](https://github.com/coracle-social/coracle/releases/tag/0.2.29)
    - Register url handler for web+nostr and use that for sharing
    - Combine search and scan pages
    - Clean up notifications page
    - Add note share page
  - Coracle now has an app store [[Demo](https://snort.social/e/note1umjyh99hc3l7ryq990av4t4ar8y65qv370xzkxsf03wajk6eqjas9mcnql)]
- Nos.social [v0.1 (44)](https://github.com/planetary-social/nos/releases/tag/v0.1-44)
  - Fixed several causes of profile pictures and reposts showing infinite spinners.
  - Filter logged user from Discover screen.
  - Links to notes or profiles are now tappable.
  - Improved performance of posting notes.
- Current [v0.0.9-Tamarindo](https://apps.apple.com/us/app/current-nostr-bitcoin/id1668517032)
  - Wallet Connect - 1-click zaps on other Nostr clients
  - Wallet Connect - 1-click zaps on other Nostr clients. Create a wallet connect link and paste it into the client.
  - Wallet Home Screen Redesign
  - Improved Search View
  - Experimental Direct Messages (Alpha Version) - End-to-end encrypted direct messaging with the ability to share images
  - Push Notifications - Receive push notifications for DMs, likes, mentions, and reposts, even when you're away from your desk.
- Plebstr
  - [0.5.1](https://apps.apple.com/app/plebstr-nostr-client/id1666230916)
    - Zap message
    - Added quotes in comments
  - [0.5.0](https://apps.apple.com/app/plebstr-nostr-client/id1666230916)
    - Grouped posts on timeline and post detail
    - Post stats
    - Automatic refresh info on overview page
    - profile QR opens nostr apps
- NostrChat [v0.1.3](https://www.nostrchat.io/channel/b0ed0085f8ebf1378686525907f8f6c4a0cf81104e913c9220fccb1cd0a44f19)
  - Channel invitations & public channel pages.
  - Public profile pages.
  - DM based on npub.
- Primal
  - [Zaps](https://nostr.band/note1culdqwlh45sjwmlgra2hex5mlu6vnecm855ua880yhj9wffcyzlswlfp4f)
    - 1-click zaps work in the feed
    - Press & hold gives more options
    - Configure zap presets in the settings
    - Zapping more than 1000 sats gives a crazier animation
  - [Saved Searches](https://primal.net/thread/note1mtcpra5snd8zan8ha0kth3wew2wewj4ewx5pdfzw6ru0a3ldznaq8ajdlm)
    - With Saved Searches, you can effortlessly curate your own Nostr universe. Simply save your favorite searches and hashtags to your Primal Homepage by clicking “add this feed to my homepage” whenever you search on Primal.
  - [Curated hashtags](https://primal.net/thread/note1smefmfu302yqcvx9q8z7zdjxqz75cl62ylklwk76ymtpy7wr3q6ql53mdw)
    - On your Primal homepage, you'll find curated hashtags like #photography, #fitness, #nature, #food, and more.
- Agora [Update](https://snort.social/e/note1pq752gksgm3mjj54ju7f3q0jzxhwm6epfp99vc5cdc2n5ydw7lvs7pf7sh?ref=nobsbitcoin.com)
  - New icon (Midjourney AI generated)
  - Smart Search - Detects if you search for a Bluesky (bsky.social), Twitter, or Mastodon (mastodon.social) handle and automatically creates a bridged Nostr profile and redirects you to it. Now you can follow people from 4 social networks all in one.
  - Discover tab - Currently shows an assortment of manually suggested profiles, but will integrate nostr.band suggested follows soon
  - Added the Friends Feed for when you want to just see posts from your follows like other clients.
  - New look for mobile with a bottom tab bar for easier navigation
- Nostur
  - [v1.0.44]
    - Tap on unread count to scroll to first unread, tap twice to scroll to top
    - Improved speed from startup to new posts
    - Improved post context menu
    - Removed "Replying to:" in threads
    - Smooth scrolling improvements
  - Testflight
    - Added Instant Zapping with Nostr Wallet Connect (NWC). Connect any NWC compatible wallet
    - Accidental Instant Zaps can be undone within 9 seconds, same as Likes
    - Failed zaps will be shown in notifications tab
    - Added Lighting Strike effect when using Instant Zaps
    - Increased image cache size
- Nostrmo [1.7.0](https://apps.apple.com/gb/app/nostrmo/id6447441761?platform=iphone) (#note: A nostr client for Mac, iPhone and iPad)
  - NIP-14 Subject tag in text events
  - NIP-30 Custom Emoji
  - Repost event jump to event thread
  - Dependence upgrade
  - Mention user and event method change
- Nostri.chat v0.314159
  - Group chat (NIP-28) support

#### Services
- Nostr Plebs Email [[Announcement](https://iris.to/note1puw3fde729hyj2r389jfv3659wtuskr6sjdac3ncl2g8pw44t8pqec68yv?ref=nobsbitcoin.com)]
  - All Nostr Plebs users can now receive emails sent directly to their Nostr address.
  - Emails will be delivered via Direct Messages sent from the @Nostr Plebs Email bot.
  - This is currently live for all of our domains.
  - Example: if you registered the Nostr address roya@nostrpurple.com, you can now receive emails sent to roya@nostrpurple.com!
  - You cannot REPLY to DMs to respond to any email that you received at this time.

#### Libraries
- rust-nostr [v0.22.0](https://github.com/rust-nostr/nostr/releases/tag/v0.22.0)
  - Remove amount tag from zap receipt event builder
  - Add lnurl tag
  - Add support for handling NIP-40 Expiration Timestamp
  - Make preimage optional in zap event builder
  - nostr: Also accept displayName in metadata
  - Add support for NIP-58 Badges
  - Nip47 by @thesimplekid

#### Relays
- nostream [v1.25.0](https://github.com/Cameri/nostream/releases/tag/v1.25.0)
  - Implement opennode payments processor

#### Projects
- nblog
  - [v0.4.1](https://github.com/jacany/nblog/releases/tag/v0.4.1)
    - Add a variable in src/app.css that allows the user to customize the accent color. (color shown on tags, author names, etc.)
  - [v0.4.0](https://github.com/jacany/nblog/releases/tag/v0.4.0)
    - UI Redesign
- Arcade [v0.0.5-alpha](https://github.com/ArcadeLabsInc/arcade/releases/tag/v0.0.5-alpha) (#note: A Nostr chat & commerce app for Android & iOS)
  - Begin conversion to create-universal-app
  - Start cross-platform design showcase
  - Add initial NIP-57 zaps integration
  - Fix desktop app; send zaps from web+mobile+desktop
  - First chat zaps sent; Message updates automatically
  - Pull v0.0.4 code into new monorepo structure
  - Configure EAS
  - Upgrade to Expo 48 and Tamagui 1.9.12
  - Minor fixes/upgrades
  - Add user metadata; send zaps to correct lud16
- OstrichGram [v0.4](https://github.com/OstrichGram/OstrichGram/releases/tag/0.4)
  - Introduces multi-relay chat functionality, also known as "Fat Groups".
- Nostr Market [NIP-19](https://twitter.com/arcbtc/status/1663283847302201348)
  - You can now publish a list of merchant public keys, market theme, logo and a banner to auto-populate and theme a nostr marketplace
- [Advanced Nostr Search](https://advancednostrsearch.vercel.app/) - Search Nostr notes with filtering by npub, date, and keywords.
- nostr.build [‘Creators’ page](https://snort.social/e/note18kf5hzk9m7uxs545psukc6vt7vrjkpn8xnwtyvqcdjz0emrd0uhq42kpkl)
  - Created an API that would make it easy for app developers to add a ‘Creators’ page button as an option on a note, for curated memes and content.
  - It would be free and easy for anyone to quickly access this content and add it to a note.
- Highlighter 0.3.1
  - Support to delete NIP-51s and list items
  - nsecBunker support: if you have an nsecBunker already, you can now use the very first app that doesn't require access to your nostr extension or to a NIP-07 extension!
  - Include feed of lists' npub
  - Support to delete NIP-51s and list items
  - mobile(ish) support 😅 good enough to log workouts on the go now
  - First nostr webapp that works in any mobile browser WITHOUT an extension or without requesting access to your private key. [[Demo](https://snort.social/e/note1vt8ukdjg7g76e92e8gnaapvuav8qzztl5a4c4w6us47vl6u7kp9q5xnzwu)]
- [satellite.earth](https://satellite.earth/cdn) - Scalable Media Hosting For The Nostr Ecosystem
  - Upload video and other large files, up to 5 GB each
  - Simple flat-rate pricing, buy storage with sats
  - Fast, free and unlimited data transfer
  - Integrated NIP-94 censorship resistance
  - Developer-friendly API (docs coming soon...)
- [njump](https://github.com/fiatjaf/njump) - A nostr static gateway (#note: Made by fiatjaf)
  - It displays notes and other stuff from inside nostr as HTML with previews
- [nak.nostr](https://nak.nostr.com/)
- Zaplife
  - Per-user zap feed

### Boosts
- XX:XX:XX Thanks to everyone who streamed sats, and shoutout to our top boosters:
  - @name (XXX sats) ""

### Project spotlight
- [Nostr Nests](https://nostrnests.com/): An audio space for chatting, brainstorming, debating, jamming, micro-conferences and more.
- [nostr-chat-fyne](https://github.com/Galaxoid-Labs/nostr-chat-fyne): An experimental chat client written with Fyne. Its a work in progress based on kind 9 ideas. (#note: added by fiatjaf)
- [nsecbunker](https://nsecbunker.com/) by Pablo
  - Import your nsec to a secure, trusted environment (e.g. HSM, self-hosted in your basement, etc)
  - Use a Nostr Connect compatible app to remotely sign events.
  - Allow your company to securely have a Nostr presence without risking the security of your key.
  - Enforce signing policies
  - New features: Policy supports, One-use tokens, Finer-grained access control
- [kind3.xyz](https://kind3.xyz/) by Pablo
  - Kind3 allows you completely replace your follow list and follow someone else's
  - It's an experiment to help you peak out of your echo chamber
  - It also allows you to create a backup of your list before doing so and restoring that backup in 1-click
- [nostr.band statistics](https://stats.nostr.band/) (#note: suggested by Miljan)
- [listr.lol](https://listr.lol/) by JeffG - A simple tool that allows you to browse and manage Nostr lists. (#note: suggested by Miljan)
- [nosta.me](https://nosta.me/) - Create and view Nostr profiles (#note: suggested by Miljan)
- [write.nostr.com](https://write.nostr.com) - nip23-enabled article editor for Nostr
- [Ephemerelay](https://gitlab.com/soapbox-pub/ephemerelay) - A Nostr relay that doesn't care about the past
  - Ephemerelay is a Nostr relay implemented in about 100 lines of code.
  - It immediately sends an EOSE for any filter, and then begins returning new events that match. Submitting an event works similarly - it will only be distributed to clients currently connected on that filter.
  - An ephemeral relay can transmit data to clients, who can in turn transmit events to fully-qualified relays that actually store things. Alternatively, you can just use it to chat with your friends who are online.
  - The biggest benefit is not caring about storage space, not caring about spam, and not caring about cost.
- [Nostr Sovrn](https://bitcoinnostr.com/sovrn) - Simplifies the process of setting up a privately controlled, censorship-resistant NIP-05 identifier/Nostr address
- [Zapddit](https://zapddit.com/feed): Reddit-Style Client For Nostr
- Relayable.org
  - A non-profit operating and managing a network of nostr relays
  - Founded to bring a high level of availability and redundancy to nostr relays.
  - Goal is to help grow nostr protocol and usage by providing a high level of service to users without need for forcing people into a pay-to-relay approach.
  - Six geographically distributed relays that use latency based DNS routing to send users to the closest relay with the least latency.
  - Relay locations: US (x2), Brazil, Singapore, Australia, Sweden
  - Relay locations (comins soon): South Africa, Japan, California
- [nostrrr](https://nostrrr.com/) - A Nostr Relay Explorer
- [zapit.live](https://www.zapit.live/)
  - Put any content behind Bitcoin lightning paywall
  - Any content such as Blog, Videos, Audio, Picture & PDF
  - Create paywalled link & share it Everywhere
  - Split Payments (Prisms)
    - Split the zap or forward all payments to your main wallet
- [YakiHonne](https://yakihonne.com/) - A Nostr-based decentralized content media protocol, which supports free curation, creation, publishing, and reporting by various media (#note: suggested by fiatjaf)
- [onyx](https://github.com/TonyGiorgio/onyx): Amethyst fork without censorship by Tony Giorgio (Android)

### News & Noteworthy
- Plebchain Radio now on podcast platforms including [Fountain](https://fountain.fm/show/0N6GGdZuYNNG7ysagCg9)
- [NDK](https://github.com/nostr-dev-kit/ndk) launched
  - NDK is a nostr development kit that makes the experience of building Nostr-related applications, whether they are relays, clients, or anything in between, better, more reliable and overall nicer to work with than existing solutions.
  - NDK Objectives:
    - The core goal of NDK is to improve the decentralization of Nostr via intelligent conventions and data discovery features without depending on any one central point of coordination (such as large relays or centralized search providers).
    - NDK team aims to have new to nostr devs get set up, and reading a NIP-01 event within 10 minutes.
    - NDK's objective is to serve prospective, and current nostr devs as clients. If you have friction with the NDK developer experience, please open issues, and ask for help from the NDK team! Devs are encouraged to search through existing, and/or create new github issues when experiencing friction with NDK.
  - New features: NIP-46 support* Ability to pass in an explicit relay set per subscription, Improved filter aggregation logic, New docs website
- Primal open sourced their [Nostr caching service](https://github.com/PrimalHQ/primal-caching-service) under the MIT license [[Announcement](https://nostr.band/note1llcfvayg63ssej9d9jk3kx6dpvn800qtd7aw8utlx59jsarctr2ql6t3c0)]
- Nostrasia event [[confirmed](https://twitter.com/mcshane_writes/status/1663361178557464576)]
  - November 1-3 in Shibuya Tokyo and Hong Kong simultaneously
  - There will be:
    - Developer keynotes
    - Open source panels
    - A hackathon competition
    - Workshops
    - Bitcoin & Nostr 101 crash courses
    - Networking evenings
    - Sushi
  - The npub for the official Nostrasia account is: npub1nstrcu63lzpjkz94djajuz2evrgu2psd66cwgc0gz0c0qazezx0q9urg5l
- jb55 is working on [Nostrscript](https://github.com/damus-io/damus/pull/1246)
  - NostrScript is a WebAssembly implementation that interacts with Damus. It enables dynamic scripting that can be used to power custom list views, enabling pluggable algorithms.
  - NostrScripts can be written in any language that compiles to WASM.
- Calle creates a basic construction of Nostr silent DM's that "break the DM graph privacy nightmare". [[Announcement](https://snort.social/e/note1c2946v39a2vlgfjn3247ehnrtjs8h0z3j2xyh2zrrff326jn5v6qwa57kt)]
  - "Might become a NIP proposal if there is interest."
- Nostr.build passed 10k uploads for ‘all’ accounts (not just Creators). Roughly 250 accounts /10000 = 40 images (media) per account holder! [[Announcement](https://snort.social/e/note1frsq8u28py8922dw5vg22hdarf3qy9khtlrfwdmm4mf054lhun4s8gy66k)]
- Fiatjaf is looking for people to fund for Nostr projects [[Hodlbod](https://snort.social/e/note10lpcvfk6www208cph2rkuc5tzsythzmjqxzr99jgga579kyw5x0q8vsqu4)]
  - Software projects should be FOSS.
  - Non-software projects eligible too
  - The goal is, as always, to #grownostr
- [Mostr]()https://gitlab.com/soapbox-pub/mostr: A Nostr Bridge announced by the Alex Gleason [[Blog post](https://soapbox.pub/blog/mostr-fediverse-nostr-bridge/)]
  - A new bridge allows interaction between Fediverse and Nostr


### Reads
- nsecBunker: Your Nostr Keys Management Fortress by Tony [[Habla.news](https://habla.news/a/naddr1qq9xuum9vd382mntv4eqzxthwden5te0wfjkccte9ehx7um5wf5kx6pwd3skueqpr9mhxue69uhhqatjv9mxjerp9ehx7um5wghxcctwvsq32amnwvaz7tmwdaehgu3wd9hx7um5vyhxxccpz4mhxue69uhkummnw3ezumtfd3hh2tnvdakqz9nhwden5te0wfjkccte9ehx7um5wghxyctwvsq3qamnwvaz7tmwdaehgu3wwa5kuegpvemhxue69uhkv6tvw3jhytnwdaehgu3wwa5kuef0dec82c33xpshw7ntde4xwdtjx4kxz6nwwg6nxdpn8phxgcmedfukcem3wdexuun5wy6kwunnxsun2a35xfckxdnpwaek5dp409enw0mzwfhkzerrv9ehg0t5wf6k2qgkwaehxw309a3xjarrda5kuetj9eek7cmfv9kqzxnhwden5te0wfjkccte9ehhyctwvajhq6tvdshxgetkqyd8wumn8ghj7un9d3shjtnwdaehgunsd3jkyuewvdhk6qguwaehxw309a6ku6tkv4e8xefwdehhxarjd93kstnvv9hxgqguwaehxw309ahx7um5wghx6at5d9h8jampd3kx2apwvdhk6qg4waehxw309ajkgetw9ehx7um5wghxcctwvsq3samnwvaz7tmjv4kxz7fwdehhxarjv96xjtnrdaksygrlts45uj9qa8lv5caydvfumwpy386qyquc6c9zqu9fdr92sxxht5psgqqqw4rs4a77p5?ref=nobsbitcoin.com)]


### Audience Questions
- "How do vanity addresses like those generated with nostrogen work?"
- "Do we need fully airgapped signing devices for Nostr keys?"


### Notes & Resources
- [simplex](https://simplex.icu/)
- [NIP-44](https://github.com/nostr-protocol/nips/pull/574): Encrypted Direct Message (Versioned)


### Episode submission ideas
- We're looking for ideas for interesting panel conversations. To send Bitcoin related questions, just go to [bitcoin.review](https://bitcoin.review/) and follow the contact links at the bottom of the page.
"""
