# ShadowPerms

### Useful Links
  - [Download](https://ci.dmulloy2.net/job/SwornPermissions)
  - [Issues](https://github.com/dmulloy2/ShadowPerms/issues/)
  - [Permissions](https://github.com/dmulloy2/ShadowPerms/blob/master/src/main/resources/plugin.yml)

### Installation
1. Remove any other permission or chat systems
2. Drop ShadowPerms.jar into your plugin directory
3. Start the server

### Integration
1. Essentials
  - Essentials doesn't currently support SwornPerms (although drtshock's [EssentialsX](https://ci.drtshock.net/job/EssentialsX/) does)
  - Remove the ````player-commands```` section of the Essentials config.yml
  - Remove EssentialsChat (if installed)
2. SwornNations / Factions
  - For chat formatting, append ````[FACTION]```` to the front of ````chatFormat```` of ShadowPerms config.yml
  - Make sure ````ShadowPerms```` is in the ````softdepend```` section of the Factions plugin.yml

### Command Structure
1. Has changed as of ShadowPerms v1, TODO update this

5. Prefix / Suffix Management
  - /prefix <prefix> - Change prefix
  - /prefixreset [player] - Reset prefix
  - /suffix <suffix> - Change suffix
  - /suffixreset [player] - Reset suffix
6. Options
  - Syntax: String: "[value]", Boolean: "b:[value]", Integer: "i:[value]", Double: "d:[value]"

### File Structure
1. Base Directory
  - Server Groups and Config
2. Worlds
  - Users file and Groups file
