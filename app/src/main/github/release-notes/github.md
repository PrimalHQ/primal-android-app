## Features
- Remote Signer (NIP-46)
- Android Signer (NIP-55)

## Fixes
- Handle local signer requests for deleted apps
- Fixed race condition while loading not cached article  (#838)
- Assure valid pubkey hex is returned for local signer login (#839)
- Allow external signer login with hex pubkey
- Fixed parsing of malformed nostrconnect relay parameters (#836)
- Cache `name` for `LocalApp`s (#835)
- Fixed rows order in session event details (#833)