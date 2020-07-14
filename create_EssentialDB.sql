--
-- Create the database to utilize
--
CREATE DATABASE IF NOT EXISTS `DATABASE_NAME`;
ALTER DATABASE `DATABASE_NAME` CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;


--
-- Create the user to access the database
--
CREATE USER 'BotAccess'@'localhost';

--
-- Grant all privileges to the made user to access the database we made
-- Replace PASSWORD with the password you want the bot to have for accessing the database
--
GRANT ALL PRIVILEGES ON `DATABASE_NAME`.* TO 'BotAccess'@'localhost' IDENTIFIED BY 'PASSWORD';

--
-- Creating the tables for EssentialBot:
--
DROP TABLE `Stickies`;
CREATE TABLE `Stickies` (
    `Sticky_ID` INTEGER(64) AUTO_INCREMENT PRIMARY KEY,
    `GuildID` BIGINT(32),
    `ChannelID` BIGINT(32),
    `Content` TEXT(1024)
);

DROP TABLE `RolePermissions`;
CREATE TABLE `RolePermissions` (
    `RP_ID` INTEGER(64) AUTO_INCREMENT PRIMARY KEY,
    `GuildID` BIGINT(32),
    `RoleID` BIGINT(32),
    `permissionSticky` BIT(1),
    `permissionKick` BIT(1),
    `permissionShadowBan` BIT(1),
    `permissionBan` BIT(1)
);

DROP TABLE `ShadowBans`;
CREATE TABLE `ShadowBans` (
    `SB_ID` INTEGER(64) AUTO_INCREMENT PRIMARY KEY,
    `GuildID` BIGINT(32),
    `UserID` BIGINT(32),
    `Reason` TEXT(1024)
);