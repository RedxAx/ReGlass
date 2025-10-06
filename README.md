# ReGlass By ReStudio
### Liquid Glass Implemented On Your Favorite Pixelated And Cubic Game

Currently, The Mod Is In Early Development, Expect Bugs And Missing Features.

### Features
- Easy, Customizable, And Fast Glass Rendering API.
- Highly Optimized, Almost Vanilla Performance.
- Some Minecraft UI Redesigns (This Is The Highly WIP Part).

### API Example:
```java
// Widget Based Dimensions
int cornerRadiusPx = 0.5f * Math.min(width, height); // Recommended Rounding
ReGlassApi.create(context).fromWidget(someWidget).cornerRadius(cornerRadiusPx).render();

// Custom Style 
customStyle = WidgetStyle.create()
        .tint(new Tint(Formatting.GOLD.getColorValue(), 0.4f))
        .rimLight(new RimLight(new Vector2f(0.5f, -0.5f), 0xFFEEEE, 0.5f));

// Static Based Rendering E.g. Called From Screen `render()`.
ReGlassApi.create(context).dimensions(10, 10, 100, 100).cornerRadius(cornerRadiusPx).style(customStyle).render();

// You Must Apply Blur
LiquidGlassUniforms.get().tryApplyBlur(context);


// Ready To Use Widget (Screen Usage Example):
boolean moveable = true; // Makes The Widget Draggable
addDrawableChild(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, null).setMoveable(moveable));
```


## Contributing Is More Than Welcome!
Especially In The Minecraft UI Redesign Part, This Part Is Highly WIP And Needs a Lot of Work.