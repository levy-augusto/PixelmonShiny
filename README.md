# PixelmonShiny
Welcome to PixelmonShiny! This plugin is my first attempt to make a project serving both PixelmonGenerations and Reforged with as much common code in between as possible.

### What does it do?
PixelmonShiny allows you to give configurable modifier items to players. Depending on the item, they can:

* Toggle shiny/non-shiny
* Change size (with selection GUI)
* Change nature (with selection GUI)
* Reroll IVs
* Swap gender to opposite (no effect on genderless pokemon)

After right-clicking with a modifier item in hand, players select the option (when needed) and then choose a pokemon in party GUI.

### Command/Permission
This plugin includes these commands:

* `/giveshinyitem <player> [amount]` **Permission:** `pixelmonshiny.give`.
Gives the shiny modifier item based on configuration.

* `/givepixelitem <type> <player> [amount]` **Permission:** `pixelmonshiny.give`.
Gives a specific modifier item. Types: `shiny`, `size`, `nature`, `ivreroll`, `gender`.

When you obtain a modifier item, right click while holding it in your main hand to open the corresponding GUI flow.

### Configuration
Upon first starting your server with this plugin, it will automatically generate a default configuration for you already, so you could use it straight out of the box! 
The default configuration will also contain plenty of comments to guide you through it, but just for coverage, I will list them here as well:

Each modifier (`shiny`, `size`, `nature`, `ivReroll`, `genderSwap`) has:

* `partyGuiTitle`
* `itemType`
* `itemName`
* `itemLore`
* `optionGuiTitle` (for size/nature)
* `options` (for size/nature)

Changing `itemType`, `itemName` or `itemLore` invalidates previously-given items of that modifier.

That's about it for this plugin already, it's a small project looking at it, but I tried to learn more about the building process, including signing and maybe even automatic deployment (but this is far far away for now).


### TO-DO(?)
Currently, I can't think of addtional things to add, but if you feel like something is wrong or missing, please open an issue and I'll see what I can do!

Thank you for looking at my plugin and reading until the end of this readme. :)
