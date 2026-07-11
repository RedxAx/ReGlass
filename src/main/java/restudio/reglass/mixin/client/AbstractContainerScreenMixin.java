package restudio.reglass.mixin.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
//? } else {
/*import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
*///? }
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if < 26 {
/*import restudio.reglass.client.ContainerColorContext;
*///? }
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
//? if >= 26 {
import restudio.reglass.client.ContainerColorContext;
//? }
import restudio.reglass.mixin.accessor.ShulkerBoxMenuAccessor;
import restudio.reglass.mixin.accessor.SlotAccessor;

//? if >= 26 {
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {
    @Shadow @Final protected T menu;
    @Shadow protected int titleLabelX;
    @Shadow protected int titleLabelY;
    @Shadow protected int inventoryLabelX;
    @Shadow protected int inventoryLabelY;
    @Shadow protected int imageWidth;
    @Shadow @Final protected Component playerInventoryTitle;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
//? } else {
/*@Mixin(HandledScreen.class)
public abstract class AbstractContainerScreenMixin<T extends ScreenHandler> extends Screen {
    @Shadow @Final protected T handler;
    @Shadow protected int titleX;
    @Shadow protected int titleY;
    @Shadow protected int playerInventoryTitleX;
    @Shadow protected int playerInventoryTitleY;
    @Shadow protected int backgroundWidth;
    @Shadow @Final protected Text playerInventoryTitle;
    @Shadow protected int x;
    @Shadow protected int y;
*///? }

    @Unique private final Map<Slot, int[]> reglass$originalSlotPositions = new IdentityHashMap<>();

//? if >= 26 {
    protected AbstractContainerScreenMixin(Component title) {
//? } else {
    /*protected AbstractContainerScreenMixin(Text title) {
*///? }
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void reglass$updateContainerSlotLayout(CallbackInfo ci) {
        reglass$rememberOriginalSlots();
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            reglass$restoreOriginalSlots();
            return;
        }

        reglass$restoreOriginalSlots();
        if (reglass$isPlayerInventory()) {
            reglass$spaceSurvivalInventorySlots();
            return;
        }

//? if >= 26 {
        if (this.menu instanceof BeaconMenu) {
//? } else {
        /*if (this.handler instanceof BeaconScreenHandler) {
*///? }
            reglass$spaceBeaconSlots();
//? if >= 26 {
        } else if (this.menu instanceof BrewingStandMenu) {
//? } else {
        /*} else if (this.handler instanceof BrewingStandScreenHandler) {
*///? }
            reglass$spaceBrewingStandSlots();
//? if >= 26 {
        } else if (this.menu instanceof GrindstoneMenu) {
//? } else {
        /*} else if (this.handler instanceof GrindstoneScreenHandler) {
*///? }
            reglass$spaceGrindstoneSlots();
//? if >= 26 {
        } else if (this.menu instanceof LoomMenu) {
//? } else {
        /*} else if (this.handler instanceof LoomScreenHandler) {
*///? }
            reglass$spaceLoomSlots();
//? if >= 26 {
        } else if (this.menu instanceof EnchantmentMenu) {
//? } else {
        /*} else if (this.handler instanceof EnchantmentScreenHandler) {
*///? }
            reglass$spaceEnchantmentSlots();
        }
        reglass$spaceGenericHotbar();
    }

//? if >= 26 {
    @Inject(
            method = "extractContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractLabels(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"
            )
    )
    private void reglass$extractContainerGlass(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "renderMain", at = @At("HEAD"))
    private void reglass$renderContainerGlass(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
//? if >= 26 {
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
//? } else {
        /*if (cfg.features.enableRedesign && cfg.features.containers) {
            reglass$renderBatchBlobs(context);
*///? }
        }
//? if >= 26 {

        reglass$renderBatchBlobs(context);
//? }
    }

//? if >= 26 {
    @Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
    private void reglass$hidePlayerInventoryLabels(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void reglass$hidePlayerInventoryLabels(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        if (!reglass$isPlayerInventory()) {
//? if >= 26 {
            context.text(this.font, this.title, reglass$labelX(this.titleLabelX), reglass$labelY(this.titleLabelY), 0xFFFFFFFF, true);
//? } else {
            /*context.drawText(this.textRenderer, this.title, reglass$labelX(this.titleX), reglass$labelY(this.titleY), 0xFFFFFFFF, true);
*///? }
        }
        ci.cancel();
    }

    @Unique
//? if >= 26 {
    private void reglass$renderBatchBlobs(GuiGraphicsExtractor context) {
//? } else {
    /*private void reglass$renderBatchBlobs(DrawContext context) {
*///? }
        for (int[] group : reglass$getSlotGroups()) {
            reglass$renderGroup(context, group);
        }
    }

    @Unique
    private List<int[]> reglass$getSlotGroups() {
        List<Slot> slots = new ArrayList<>();
//? if >= 26 {
        for (Slot slot : this.menu.slots) {
            if (slot.isActive()) {
//? } else {
        /*for (Slot slot : this.handler.slots) {
            if (slot.isEnabled()) {
*///? }
                slots.add(slot);
            }
        }
        slots.sort(Comparator.comparingInt((Slot s) -> s.y).thenComparingInt(s -> s.x));

        List<int[]> groups = new ArrayList<>();
        for (Slot slot : slots) {
            int[] rect = new int[] { slot.x, slot.y, slot.x + 18, slot.y + 18 };
            int target = -1;
            for (int i = 0; i < groups.size(); i++) {
                if (reglass$isConnected(groups.get(i), rect)) {
                    target = i;
                    break;
                }
            }

            if (target == -1) {
                groups.add(rect);
                continue;
            }

            int[] group = groups.get(target);
            reglass$include(group, rect);
            for (int i = groups.size() - 1; i >= 0; i--) {
                if (i != target && reglass$isConnected(group, groups.get(i))) {
                    reglass$include(group, groups.remove(i));
                    if (i < target) {
                        target--;
                    }
                }
            }
        }
        groups.sort(Comparator.comparingInt((int[] g) -> g[1]).thenComparingInt(g -> g[0]));
        reglass$mergeVillagerCostSlots(groups);
        return reglass$removeContainedGroups(groups);
    }

    @Unique
    private boolean reglass$isConnected(int[] a, int[] b) {
        boolean overlapsX = a[0] < b[2] && a[2] > b[0];
        boolean overlapsY = a[1] < b[3] && a[3] > b[1];
        boolean touchesX = a[2] == b[0] || b[2] == a[0];
        boolean touchesY = a[3] == b[1] || b[3] == a[1];
        return (touchesX && overlapsY) || (touchesY && overlapsX);
    }

    @Unique
    private void reglass$include(int[] target, int[] rect) {
        target[0] = Math.min(target[0], rect[0]);
        target[1] = Math.min(target[1], rect[1]);
        target[2] = Math.max(target[2], rect[2]);
        target[3] = Math.max(target[3], rect[3]);
    }

    @Unique
    private void reglass$mergeVillagerCostSlots(List<int[]> groups) {
//? if >= 26 {
        if (!(this.menu instanceof MerchantMenu) || this.menu.slots.size() < 2) {
//? } else {
        /*if (!(this.handler instanceof MerchantScreenHandler) || this.handler.slots.size() < 2) {
*///? }
            return;
        }
//? if >= 26 {
        Slot first = this.menu.slots.get(0);
        Slot second = this.menu.slots.get(1);
        if (!first.isActive() || !second.isActive()) {
//? } else {
        /*Slot first = this.handler.slots.get(0);
        Slot second = this.handler.slots.get(1);
        if (!first.isEnabled() || !second.isEnabled()) {
*///? }
            return;
        }

        int[] merged = new int[] {
                Math.min(first.x, second.x),
                Math.min(first.y, second.y),
                Math.max(first.x, second.x) + 18,
                Math.max(first.y, second.y) + 18
        };
        groups.removeIf(group -> reglass$isSingleSlotGroup(group, first) || reglass$isSingleSlotGroup(group, second));
        groups.add(merged);
        groups.sort(Comparator.comparingInt((int[] g) -> g[1]).thenComparingInt(g -> g[0]));
    }

    @Unique
    private boolean reglass$isSingleSlotGroup(int[] group, Slot slot) {
        return group[0] == slot.x && group[1] == slot.y && group[2] == slot.x + 18 && group[3] == slot.y + 18;
    }

    @Unique
//? if >= 26 {
    private void reglass$renderGroup(GuiGraphicsExtractor context, int[] group) {
//? } else {
    /*private void reglass$renderGroup(DrawContext context, int[] group) {
*///? }
        int groupWidth = group[2] - group[0];
        int groupHeight = group[3] - group[1];
        if (reglass$isHotbarGroup(groupWidth, groupHeight)) {
            reglass$renderHotbarGroup(context, group);
            return;
        }

        int padding = groupWidth == 18 && groupHeight == 18 ? reglass$singleSlotPadding() : 4;
//? if >= 26 {
        int x = this.leftPos + group[0] - padding - 1;
        int y = this.topPos + group[1] - padding - 1;
//? } else {
        /*int groupX = this.x + group[0] - padding - 1;
        int groupY = this.y + group[1] - padding - 1;
*///? }
        int width = groupWidth + padding * 2;
        int height = groupHeight + padding * 2;

        ReGlassApi.create(context)
//? if >= 26 {
                .dimensions(x, y, width, height)
//? } else {
                /*.dimensions(groupX, groupY, width, height)
*///? }
                .cornerRadius(Math.min(12, Math.max(6, Math.min(width, height) * 0.22f)))
                .style(reglass$panelStyle(group, groupWidth, groupHeight).layer(1))
//? if >= 26 {
                .screenSpace()
//? }
                .render();
    }

    @Unique
    private boolean reglass$isHotbarGroup(int groupWidth, int groupHeight) {
        return groupWidth == 162 && groupHeight == 18;
    }

    @Unique
//? if >= 26 {
    private void reglass$renderHotbarGroup(GuiGraphicsExtractor context, int[] group) {
//? } else {
    /*private void reglass$renderHotbarGroup(DrawContext context, int[] group) {
*///? }
        ReGlassApi.create(context)
//? if >= 26 {
                .dimensions(this.leftPos + group[0] - 4, this.topPos + group[1] - 2, 170, 22)
//? } else {
                /*.dimensions(this.x + group[0] - 4, this.y + group[1] - 2, 170, 22)
*///? }
                .cornerRadius(11)
                .style(WidgetStyle.create()
                        .tint(0x000000, 0.3f)
                        .layer(1))
//? if >= 26 {
                .screenSpace()
//? }
                .render();
    }

    @Unique
    private WidgetStyle reglass$panelStyle() {
//? if >= 26 {
        return WidgetStyle.create()
                .tint(0x000000, 0.16f);
//? } else {
        /*return WidgetStyle.create().tint(0x000000, 0.16f);
*///? }
    }

    @Unique
    private WidgetStyle reglass$panelStyle(int[] group, int groupWidth, int groupHeight) {
        Integer shulkerTint = reglass$shulkerMainTint(group, groupWidth, groupHeight);
        if (shulkerTint != null) {
            return WidgetStyle.create().tint(shulkerTint, 0.45f);
        }
        return reglass$panelStyle();
    }

    @Unique
    private Integer reglass$shulkerMainTint(int[] group, int groupWidth, int groupHeight) {
//? if >= 26 {
        if (!(this.menu instanceof ShulkerBoxMenu) || groupWidth != 162 || groupHeight != 54 || group[1] >= 80) {
//? } else {
        /*if (!(this.handler instanceof ShulkerBoxScreenHandler) || groupWidth != 162 || groupHeight != 54 || group[1] >= 80) {
*///? }
            return null;
        }

//? if >= 26 {
        Container container = ((ShulkerBoxMenuAccessor) this.menu).reglass$getContainer();
        DyeColor color = container instanceof ShulkerBoxBlockEntity shulkerBox ? shulkerBox.getColor() : null;
//? } else {
        /*Inventory inventory = ((ShulkerBoxMenuAccessor) this.handler).reglass$getInventory();
        DyeColor color = inventory instanceof ShulkerBoxBlockEntity shulkerBox ? shulkerBox.getColor() : null;
*///? }
        if (color == null) {
            color = ContainerColorContext.getLastShulkerColor();
        }
        if (color == null) {
            color = reglass$shulkerColorFromTitle();
        }
        if (color == null) {
            return 0xFF8E579D;
        }
        return reglass$shulkerTintColor(color);
    }

    @Unique
    private DyeColor reglass$shulkerColorFromTitle() {
//? if >= 26 {
        String title = this.title.getString().toLowerCase(Locale.ROOT).replace('_', ' ');
//? } else {
        /*String titleString = this.title.getString().toLowerCase(Locale.ROOT).replace('_', ' ');
*///? }
        DyeColor[] colors = DyeColor.values();
        List<DyeColor> sortedColors = new ArrayList<>(List.of(colors));
//? if >= 26 {
        sortedColors.sort(Comparator.comparingInt((DyeColor color) -> color.getName().length()).reversed());
//? } else {
        /*sortedColors.sort(Comparator.comparingInt((DyeColor color) -> color.getId().length()).reversed());
*///? }
        for (DyeColor color : sortedColors) {
//? if >= 26 {
            if (title.contains(color.getName().replace('_', ' '))) {
//? } else {
            /*if (titleString.contains(color.getId().replace('_', ' '))) {
*///? }
                return color;
            }
        }
        return null;
    }

    @Unique
    private int reglass$shulkerTintColor(DyeColor color) {
        return switch (color) {
            case WHITE -> 0xFFE9ECEC;
            case ORANGE -> 0xFFF07613;
            case MAGENTA -> 0xFFC64FBD;
            case LIGHT_BLUE -> 0xFF3AAFD9;
            case YELLOW -> 0xFFF8C627;
            case LIME -> 0xFF80C71F;
            case PINK -> 0xFFF38BAA;
            case GRAY -> 0xFF474F52;
            case LIGHT_GRAY -> 0xFF9D9D97;
            case CYAN -> 0xFF169C9C;
            case PURPLE -> 0xFF8932B8;
            case BLUE -> 0xFF3C44AA;
            case BROWN -> 0xFF835432;
            case GREEN -> 0xFF5E7C16;
            case RED -> 0xFFB02E26;
            case BLACK -> 0xFF1D1D21;
        };
    }

    @Unique
    private int reglass$singleSlotPadding() {
//? if >= 26 {
        return this.menu instanceof BeaconMenu ? 2 : 3;
//? } else {
        /*return this.handler instanceof BeaconScreenHandler ? 2 : 3;
*///? }
    }

    @Unique
//? if >= 26 {
    private int reglass$labelY(int y) {
        if (this.menu instanceof LoomMenu) {
//? } else {
    /*private int reglass$labelY(int labelY) {
        if (this.handler instanceof LoomScreenHandler) {
*///? }
            return 32;
        }
//? if >= 26 {
        if (this.menu instanceof CraftingMenu
                || this.menu instanceof CrafterMenu
                || this.menu instanceof DispenserMenu) {
            return y - 4;
//? } else {
        /*if (this.handler instanceof CraftingScreenHandler
                || this.handler instanceof CrafterScreenHandler
                || this.handler instanceof Generic3x3ContainerScreenHandler) {
            return labelY - 4;
*///? }
        }
//? if >= 26 {
        if (this.menu instanceof BrewingStandMenu) {
            return y - 10;
//? } else {
        /*if (this.handler instanceof BrewingStandScreenHandler) {
            return labelY - 12;
*///? }
        }
//? if >= 26 {
        return y - 2;
//? } else {
        /*return labelY - 2;
*///? }
    }

    @Unique
//? if >= 26 {
    private int reglass$labelX(int x) {
        if (this.menu instanceof LoomMenu) {
            return -8;
//? } else {
    /*private int reglass$labelX(int labelX) {
        if (this.handler instanceof LoomScreenHandler) {
            return -4;
*///? }
        }
//? if >= 26 {
        return x;
//? } else {
        /*return labelX;
*///? }
    }

    @Unique
    private void reglass$rememberOriginalSlots() {
//? if >= 26 {
        for (Slot slot : this.menu.slots) {
//? } else {
        /*for (Slot slot : this.handler.slots) {
*///? }
            reglass$originalSlotPositions.computeIfAbsent(slot, s -> new int[] { s.x, s.y });
        }
    }

    @Unique
    private void reglass$restoreOriginalSlots() {
        for (Map.Entry<Slot, int[]> entry : reglass$originalSlotPositions.entrySet()) {
            reglass$setSlotPosition(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
        }
    }

    @Unique
    private void reglass$spaceSurvivalInventorySlots() {
        for (int i = 5; i <= 8; i++) {
            reglass$setSlotPosition(i, 8, 2 + (i - 5) * 18);
        }
        reglass$setSlotPosition(45, 69, 56);
        for (int i = 36; i <= 44; i++) {
            reglass$setSlotPosition(i, 8 + (i - 36) * 18, 150);
        }
    }

    @Unique
    private void reglass$spaceGenericHotbar() {
        List<Slot> candidates = new ArrayList<>();
        int bestY = Integer.MIN_VALUE;
//? if >= 26 {
        for (Slot slot : this.menu.slots) {
            int containerSlot = slot.getContainerSlot();
            if (!slot.isActive() || containerSlot < 0 || containerSlot > 8) {
//? } else {
        /*for (Slot slot : this.handler.slots) {
            int containerSlot = slot.getIndex();
            if (!slot.isEnabled() || containerSlot < 0 || containerSlot > 8) {
*///? }
                continue;
            }
            if (slot.y > bestY) {
                bestY = slot.y;
            }
        }
        if (bestY == Integer.MIN_VALUE) {
            return;
        }
//? if >= 26 {
        for (Slot slot : this.menu.slots) {
            int containerSlot = slot.getContainerSlot();
            if (slot.isActive() && containerSlot >= 0 && containerSlot <= 8 && slot.y == bestY) {
//? } else {
        /*for (Slot slot : this.handler.slots) {
            int containerSlot = slot.getIndex();
            if (slot.isEnabled() && containerSlot >= 0 && containerSlot <= 8 && slot.y == bestY) {
*///? }
                candidates.add(slot);
            }
        }
        if (candidates.size() != 9) {
            return;
        }
//? if >= 26 {
        candidates.sort(Comparator.comparingInt(Slot::getContainerSlot));
//? } else {
        /*candidates.sort(Comparator.comparingInt(Slot::getIndex));
*///? }
        for (Slot slot : candidates) {
            int[] original = reglass$originalSlotPositions.get(slot);
            if (original != null) {
                reglass$setSlotPosition(slot, original[0], original[1] + 8);
            }
        }
    }

    @Unique
    private void reglass$spaceBrewingStandSlots() {
        reglass$setSlotPosition(0, 46, 45);
        reglass$setSlotPosition(1, 79, 51);
        reglass$setSlotPosition(2, 112, 45);
        reglass$setSlotPosition(3, 79, 10);
        reglass$setSlotPosition(4, 23, 26);
    }

    @Unique
    private void reglass$spaceBeaconSlots() {
        reglass$setSlotPosition(0, 143, 107);
    }

    @Unique
    private void reglass$spaceGrindstoneSlots() {
        reglass$setSlotPosition(0, 40, 42);
        reglass$setSlotPosition(1, 58, 42);
    }

    @Unique
    private void reglass$spaceLoomSlots() {
        reglass$setSlotPosition(0, -4, 48);
        reglass$setSlotPosition(1, 14, 48);
        reglass$setSlotPosition(2, 32, 48);
        reglass$setSlotPosition(3, 139, 48);
    }

    @Unique
    private void reglass$spaceEnchantmentSlots() {
        reglass$setSlotPosition(0, 15, 52);
        reglass$setSlotPosition(1, 33, 52);
    }

    @Unique
    private List<int[]> reglass$removeContainedGroups(List<int[]> groups) {
        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            int[] group = groups.get(i);
            boolean contained = false;
            for (int j = 0; j < groups.size(); j++) {
                if (i != j && reglass$containsWithPadding(groups.get(j), group)) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                result.add(group);
            }
        }
        return result;
    }

    @Unique
    private boolean reglass$containsWithPadding(int[] outer, int[] inner) {
        int outerWidth = outer[2] - outer[0];
        int outerHeight = outer[3] - outer[1];
        int innerWidth = inner[2] - inner[0];
        int innerHeight = inner[3] - inner[1];
        if (outerWidth <= innerWidth && outerHeight <= innerHeight) {
            return false;
        }
        int outerPadding = outerWidth == 18 && outerHeight == 18 ? reglass$singleSlotPadding() : 4;
        int innerPadding = innerWidth == 18 && innerHeight == 18 ? reglass$singleSlotPadding() : 4;
        return outer[0] - outerPadding <= inner[0] - innerPadding
                && outer[1] - outerPadding <= inner[1] - innerPadding
                && outer[2] + outerPadding >= inner[2] + innerPadding
                && outer[3] + outerPadding >= inner[3] + innerPadding;
    }

    @Unique
//? if >= 26 {
    private void reglass$setSlotPosition(int index, int x, int y) {
        if (index < 0 || index >= this.menu.slots.size()) {
//? } else {
    /*private void reglass$setSlotPosition(int index, int slotX, int slotY) {
        if (index < 0 || index >= this.handler.slots.size()) {
*///? }
            return;
        }
//? if >= 26 {
        reglass$setSlotPosition(this.menu.slots.get(index), x, y);
//? } else {
        /*reglass$setSlotPosition(this.handler.slots.get(index), slotX, slotY);
*///? }
    }

    @Unique
//? if >= 26 {
    private void reglass$setSlotPosition(Slot slot, int x, int y) {
        ((SlotAccessor) slot).reglass$setX(x);
        ((SlotAccessor) slot).reglass$setY(y);
//? } else {
    /*private void reglass$setSlotPosition(Slot slot, int slotX, int slotY) {
        ((SlotAccessor) slot).reglass$setX(slotX);
        ((SlotAccessor) slot).reglass$setY(slotY);
*///? }
    }

    @Unique
    private boolean reglass$isPlayerInventory() {
        return (Object) this instanceof InventoryScreen;
    }
}