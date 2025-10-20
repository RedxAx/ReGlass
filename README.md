# ReGlass By ReStudio
### Liquid Glass Implemented On Your Favorite Pixelated And Cubic Game

https://github.com/user-attachments/assets/59a78fcf-5223-416b-abdb-f2778f62ef0e

ReGlass Is Meant To Be An API For Any Minecraft Mod.

### Features
- Easy, Customizable, And Fast Glass Rendering API.
- Highly Optimized, Almost Vanilla Performance (For Dedicated GPU PCs).
- Some Minecraft UI Redesigns.

### API Example:
```java
// Widget Based Dimensions
int cornerRadiusPx = 0.5f * Math.min(width, height); // Recommended Rounding
ReGlassApi.create(context).fromWidget(someWidget).cornerRadius(cornerRadiusPx).render();

// Custom Style 
customStyle = WidgetStyle.create()
        .tint(Formatting.GOLD.getColorValue(), 0.4f)
        .blurRadius(0).shadow(25f, 0.2f, 0f, 3f)
        .smoothing(.05f).shadowColor(0x000000, 1.0f);

// Static Based Rendering E.g. Called From Screen `render()`.
ReGlassApi.create(context).dimensions(10, 10, 100, 100).cornerRadius(cornerRadiusPx).style(customStyle).render();

// You Must Apply Blur
LiquidGlassUniforms.get().tryApplyBlur(context);


// Ready To Use Widget (Screen Usage Example):
boolean moveable = true; // Makes The Widget Draggable
addDrawableChild(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, null).setMoveable(moveable));
```

### Keybinds:
- `G`: Open The Configuration Screen For ReGlass. (Click In-Game)
- `H`: Playground, Shows a Styled ReGlass Widget, And You Can Summon More By Right Clicking. All Widgets Are Draggable. (Click In-Game)

## Contributing Is More Than Welcome!
Especially In The Minecraft UI Redesign Part, This Part Is Highly WIP And Needs a Lot of Work.
