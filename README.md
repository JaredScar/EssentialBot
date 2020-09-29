# EssentialBot

[![forthebadge](https://forthebadge.com/images/badges/built-with-love.svg)](https://badger.store)
[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/check-it-out.svg)](https://github.com/JaredScar/EssentialBot/releases)

## What is it?

EssentialBot is well, an essential bot for your discord server. All of 
EssentialBot's commands can be configured to be used by discord roles on your discord 
server. This is a much more advanced permission system than discord's system 
considering you can set up permissions for each command and/or action the bot can 
perform.

**NOTE:** `A web panel for EssentialBot is planned down the line. (The web panel will use the 
live version and will only work for the live version) The web panel will have some 
actions be free, but will include an advanced permission system with UI that will be 
paid monthly to utilize.`

## Features

**Current Possible Permissions:** 
`permissionSticky`, `permissionKick`, `permissionShadowBan`, 
`permissionBan`, `permissionLockdown`

`=perm toggle <permission> <roleID>` - This command will toggle permissions for the 
role specified. A role tag or role ID can go in the `<roleID>` parameter.

`=help` - Shows the help menu for EssentialBot

`=sticky <msg>` - Sticky a message to the bottom of the screen, this will also turn the 
channel into a 15 second slowmode whilst a sticky message is active (by default). 

`=unsticky` - This will unsticky a message from a channel (if there is an sticky 
message active within the channel it is ran in). 

`=kick @User <reason>` - This will kick a user from the discord. 

`=shadowb @User <reason>` - This will shadow ban a user from the discord. Basically they will be 
denied access to every discord channel within the discord. (Even if they join back!) 

`=unshadowb @User` - This will unshadow ban a user from the discord.

`=ban @User <reason>` - This will ban the user from the discord permanently. 

`=unban @User` - This will unban the user from the discord.

`=servers` - List the servers EssentialBot is on.

`=servercount` - Show how many servers in total EssentialBot is on.

`=lockdown <reason>` - This will lock/unlock the discord and [if locked] prevent people from joining 
whilst messaging them the reason they cannot join (`<reason>`).

`=lockstatus` - Take a look of the lockdown status of the server.

*`=serverlist` - Take a look at all the recently bumped servers on the list [this command can 
be disabled by the server owner, but then prevents usage of command `=bump`]

*`=bump` - Bump your server to the top of EssentialBot's server listing (`=serverlist`), can 
be used once every 24 hours...

*`Command` - This means this command and/or feature has not been implemented as of yet. 
It is a planned feature coming soon.

## Screenshots

![Using the Help Menu](https://i.gyazo.com/66c6939703788b487a43fdee852cf630.gif)

## Configuration
```yaml
###################
### Bot Details ###
###################
BotToken: ''

###################
### SQL Details ###
###################
Host: 'badger.store'
Database_Name: ''
Username: ''
Password: 'PASSWORD'
Port: 3306 # This is usually the default port for MySQL servers, you will most likely not need to change this
```


## Download
https://github.com/JaredScar/EssentialBot

## Invite me to your server:
https://discord.com/api/oauth2/authorize?client_id=606882845486481448&permissions=8&scope=bot