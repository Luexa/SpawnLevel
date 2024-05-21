# SpawnLevel

Plugin for Towny servers that adds more controls to `/t spawn` and `/n spawn`.

## Summary

- Towns and nations may choose whether they want their spawn to be fully public, or restricted to town residents, nation members, or allies.
- Towns and nations can allow specific players to bypass their configured spawn level.
- Players may be banned from using `/n spawn` if they are outlawed from a nation capital.

## Spawn Levels

Towns can configure their spawn level using the command `/t set spawnlevel <level>`, whereas nations can configure their spawn level using the command `/n set spawnlevel <level>`.

| Spawn Level | Description |
| ----------- | ----------- |
| `resident`  | Only town residents can teleport to this spawn |
| `nation`    | Any nation member can teleport to this spawn |
| `ally`      | Any nation member or allied nation member can teleport to this spawn |
| `outsider`  | Anyone can teleport to this spawn unless they are from an enemy nation | 

As spawn levels are an extension to the public/non-public system, setting a spawn level will affect a town or nation's "public" status, and vice versa.

For towns, a spawn level of `resident` results in the town not being public, whereas a spawn level of `nation`, `ally`, or `outsider` results in the town being public.
Using `/t toggle public` to disable public spawns will set the spawn level to `resident`, and using the same command to enable public spawns will (by default) set the spawn level to `outsider`.

For nations, a spawn level of `nation` results in the nation not being public, whereas a spawn level of `ally` or `outsider` results in the nation being public.
Using `/n toggle public` to disable public spawns will set the spawn level to `nation`, and using the same command to enable public spawns will (by default) set the spawn level to `outsider`.

## Player Spawn Overrides

Using the command `/t allowspawn` or `/n allowspawn` allows a town or nation to manage a set of players who are permitted to teleport to the town/nation spawn even if the town or nation's spawn level would normally prevent the teleportation from succeeding. Note that being an outlaw or enemy of the destination will still prevent teleportation.

The available commands are:

- `/t allowspawn add <player>`
- `/t allowspawn remove <player>`
- `/t allowspawn list`
- `/n allowspawn add <player>`
- `/n allowspawn remove <player>`
- `/n allowspawn list`

## Capital Outlaw Ban

Nation spawns will be unavailable to outlaws of the nation's capital unless given the permission node `spawnlevel.bypass_capital_ban`.
