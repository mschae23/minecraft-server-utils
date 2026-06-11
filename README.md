# Server utils
This is a small mod with server-side things I find useful.

Requires LuckPerms.

## Permissions

### Commands
- `serverutils.command.pos.root`: Permission for /pos command (defaults to `true`).
- `serverutils.command.pos.public`: Overrides the `command.pos.in_public_chat` config property
(if this is enabled, the player's coordinates will be shown to all players instead of just the one who used /pos).

### Other
- `serverutils.death.printcoords.enabled`: When a player dies, their coordinates will be shown in chat if this and the corresponding config option are enabled.
- `serverutils.death.printcoords.public`: Overrides the `death_coords.in_public_chat` config property
(if this is enabled, the player's coordinates will be sent to all players instead of just the one who died).
- `serverutils.key...`: Default prefix for permissions to open containers that are locked.
- `serverutils.rule.keep_inventory`: Overrides the `keep_inventory` game rule.
- `serverutils.rule.pvp`: Overrides the `pvp` game rule. Both players need PVP enabled for them to be allowed to fight each other.

## License
Server utils
Copyright (C) 2024  mschae23

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
