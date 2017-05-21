Loved Up
========
A Valentine's Day themed plugin.

Features:

 * Mobs show heart particles for a configurable period after targeting a
   player. The hearts stop if they lose interest in the player, or on timeout.

 * Projectiles (eggs, arrows, pearls, snowballs, fishing bobs, etc) show a
   trail of heart particles for a configurable timeout.

 * If a projectile hits a player, the victim shows heart particles for a
   configurable period.

 * The `/love <player-partial-name>` command anonymously sends heart
   particles to the player whose name most closely matches the name argument.
   The recipient is not informed of the identity of the sender.

   * If no matching player is online, a humourous error message  is shown.
   * If more than one argument is specified, a humourous error message is shown.

 * If two players `/love` each other, or if they hit each other with
   projectiles, then they "match" and each player sees a message with the name
   of their admirer and a firework effect.

   * Sent `/love` and projectile hits are remembered until the two parties
     match or the server restarts.

 * The `/hate <player-partial-name>` command anonymously sends thunderclouds
   to the player whose name most closely matches the name argument.
   The recipient is not informed of the identity of the sender.

   * If no matching player is online, a humourous error message is shown.
   * if more than one argument is specified, a humourous error message is shown.

 * The `/loveall` command allows a staff member to anonymously send hearts
   to all online players.

 * The `/sparkall [<message>]` command allows a staff member to
   anonymously send hearts, a random firework effect, and an optional message
   with alternate colour codes to all online players.

 * The `/hateall` command allows a staff member to anonymously send thunderclouds
   to all online players.

 * The `/growlall [<message>]` command allows a staff member to
   anonymously send thunderclouds, growl effect, and an optional message
   with alternate colour codes to all online players.

 * The `/unloveable` toggles a player's susceptibility to the effects of
   `/love`, `/loveall`, `/sparkall` and the impact of projectiles
   launched at them by other players until the next server restart.

 * The `/unhateable` toggles a player's susceptibility to the effects of
   `/hate` until the next server restart.

Commands
--------

| Command                   | Permission      | Description                              |
| ------------------------- | --------------- | ---------------------------------------- |
| `/love <player>`          | lovedup.love    | Send your love, anonymously to a player. |
| `/hate <player>`          | lovedup.hate    | Send your hate, anonymously to a player. |
| `/unlovable`              | lovedup.love    | Toggle one's own susceptibility to projectiles, `/love`, `/loveall` and `/sparkall` until the next restart. |
| `/unloveable`             | lovedup.love    | Alias of `/unlovable`. |
| `/unhateable`             | lovedup.hate    | Toggle one's own susceptibility to `/hate`. |
| `/unhatable`              | lovedup.hate    | Alias of `/unhateable`. |
| `/loveall`                | lovedup.loveall | Send hearts, anonymously to all players except the exempt and the unlovable. |
| `/sparkall [<message>]`   | lovedup.loveall | Send hearts, fireworks and an optional message, anonymously to all players except the exempt and the unlovable. |
| `/hateall`                | lovedup.hateall | Send storm clouds, anonymously to all players except the exempt and unhateable. |
| `/growlall [<message>]`   | lovedup.hateall | Send storm clouds, growling and optional message, anonymously to all players except the exempt and unhateable. |
| `/lovedup help`           | lovedup.admin   | Show help for the `/lovedup` command. |
| `/lovedup reload`         | lovedup.admin   | Reload the configuration file. |


Configuration Settings
----------------------

 * `debug: false`
   * If true, log debug messages.

 * `self_match: false`
   * If true, allow players to match with themselves with firework effects.
   * Players can always give themselves hearts, regardless.

 * `only_arrows_love: false`
   * If true, the only projectile that signifies love is an arrow.

 * `enable_arrow_effects: true`
   * If true, shot arrows trail hearts and impart love to players on impact.

 * `enable_other_projectile_effects: true`
   * If true, projectiles other than arrows (e.g. snowballs, eggs, fishing bobs)
     trail hearts and impart love to players on impact.
   * However, note that ender pearls *never( show heart trails or love players.

 * `tracking.player_ms: 20000`
   * Duration of hearts shown on player in milliseconds.

 * `tracking.mob_ms: 120000`
   * Duration of hearts shown on mob in milliseconds.

 * `tracking.projectile_ms: 30000`
   * Duration of hearts shown on projectile in milliseconds.


Permissions
-----------

 * `lovedup.love`
   * Default: `true`
   * Permission to use `/love` and `/unlovable`.

 * `lovedup.hate`
   * Default: `true`
   * Permission to use `/hate` and `/unhateable`.

* `lovedup.loveall`
   * Default: `op`
   * Permission to use `/loveall` and `/sparkall`.

* `lovedup.hateall`
   * Default: `op`
   * Permission to use `/hateall` and `/growlall`.

* `lovedup.admin`
   * Default: `op`
   * Permission to use `/lovedup`.

* `lovedup.exempt`
   * Default: `false`
   * Players with this permission do not show hearts or fireworks and don't
     receive messages.
   * This permission is intended to be assigned to staff performing official
     duties that would be compromised by hearts or fireworks appearing, because
     the staff member is vanished at the time.


bPermissions Configuration Excerpt
----------------------------------
Below is an excerpt of a bPermissions `groups.yml` file showing how to
configure LovedUp permissions when used in conjunction with the
[ModMode](https://github.com/NerdNu/ModMode) plugin.

Staff in the `ModMode` group (Moderators performing official duties) are given
the `lovedup.exempt` permission so that they can remain unobtrusive while
vanished.

Server admins either inherit the `ModMode` group directly (when on
a nerd.nu server that they don't directly administer) or inherit the `super`
group on their "own" server, which in turn inherits the `ModMode` group.  In
order to allow admins to participate in the festivities, the inherited
`lovedup.exempt` permission is negated.  It will then only be effective
when they switch into ModMode.

```
default: default
groups:
  ModMode:
    permissions:
    - lovedup.exempt
    groups:
    - moderators

  super:
    permissions:
    - lovedup.loveall
    - ^lovedup.exempt
    groups:
    - modmode

  PAdmins:
    groups:
    - super

  CAdmins:
    permissions:
    - ^lovedup.exempt
    groups:
    - modmode

  TechAdmins:
    permissions:
    - lovedup.admin
    groups:
    - super
```
