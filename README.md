# EssentialBot

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
`permissionBan`

`=perm toggle <permission> <roleID>` - This command will toggle permissions for the 
role specified. A role tag or role ID can go in the `<roleID>` parameter.

`=sticky <msg>` - Sticky a message to the bottom of the screen, this will also turn the 
channel into a 15 second slowmode whilst a sticky message is active (by default). 

`=unsticky` - This will unsticky a message from a channel (if there is an sticky 
message active within the channel it is ran in). 

`=kick @User` - This will kick a user from the discord. 

`=shadowb @User` - This will shadow ban a user from the discord. Basically they will be 
denied access to every discord channel within the discord. (Even if they join back!) 

`=ban @User` - This will ban the user from the discord permanently. 

## Screenshots



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

## Invite me to your server:
https://discord.com/api/oauth2/authorize?client_id=606882845486481448&permissions=8&scope=bot