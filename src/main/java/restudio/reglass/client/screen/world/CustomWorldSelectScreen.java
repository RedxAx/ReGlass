package restudio.reglass.client.screen.world;

import com.mojang.logging.LogUtils;
//? if >= 26 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.components.Button;
//? } else {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
*///? }
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
//? if >= 26 {
import net.minecraft.network.chat.Component;
//? } else {
/*import net.minecraft.text.Text;
*///? }
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;
import restudio.reglass.client.screen.widget.world.WorldListEntryWidget;
import restudio.reglass.client.screen.widget.ScrollableListWidget;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomWorldSelectScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Screen parent;
    private ScrollableListWidget<WorldListEntryWidget> worldList;
    private TextFieldWidget searchBox;
//? if >= 26 {
    private Button playButton, createButton, editButton, deleteButton;
//? } else {
    /*private ButtonWidget playButton, createButton, editButton, deleteButton;
*///? }

    public CustomWorldSelectScreen(Screen parent) {
//? if >= 26 {
        super(Component.translatable("selectWorld.title"));
//? } else {
        /*super(Text.translatable("selectWorld.title"));
*///? }
        this.parent = parent;
    }

    @Override
    protected void init() {
        int listWidth = this.width - 150;
        this.worldList = new ScrollableListWidget<>(this, 20, 50, listWidth - 40, this.height - 100, 36);
        this.worldList.setVerticalPadding(5);

//? if >= 26 {
        this.searchBox = new TextFieldWidget(this.font, 20, 20, listWidth - 40, 20, Component.translatable("selectWorld.search"));
//? } else {
        /*this.searchBox = new TextFieldWidget(this.textRenderer, 20, 20, listWidth - 40, 20, Text.translatable("selectWorld.search"));
*///? }
        this.searchBox.setChangedListener(this::filterWorlds);

        this.loadWorldList();

//? if >= 26 {
        this.addRenderableWidget(this.worldList);
        this.addRenderableWidget(this.searchBox);
//? } else {
        /*this.addDrawableChild(this.worldList);
        this.addDrawableChild(this.searchBox);
*///? }

        int buttonWidth = 120;

//? if >= 26 {
        this.playButton = Button.builder(Component.translatable("selectWorld.select"), button -> play(getSelectedSummaries())).bounds(listWidth, 50, buttonWidth, 20).build();
        this.createButton = Button.builder(Component.translatable("selectWorld.create"), button -> CreateWorldScreen.show(this.minecraft, null)).bounds(listWidth, 80, buttonWidth, 20).build();
        this.editButton = Button.builder(Component.translatable("selectWorld.edit"), button -> {
//? } else {
        /*this.playButton = ButtonWidget.builder(Text.translatable("selectWorld.select"), button -> play(getSelectedSummaries())).dimensions(listWidth, 50, buttonWidth, 20).build();
        this.createButton = ButtonWidget.builder(Text.translatable("selectWorld.create"), button -> CreateWorldScreen.show(this.client, null)).dimensions(listWidth, 80, buttonWidth, 20).build();
        this.editButton = ButtonWidget.builder(Text.translatable("selectWorld.edit"), button -> {
*///? }
            Set<LevelSummary> summaries = getSelectedSummaries();
            if (summaries.size() == 1) {
                LevelSummary summary = summaries.iterator().next();
                try {
//? if >= 26 {
                    LevelStorage.Session session = this.minecraft.getLevelStorage().createSession(summary.getName());
                    this.minecraft.setScreen(EditWorldScreen.create(this.minecraft, session, (saved) -> {
//? } else {
                    /*LevelStorage.Session session = this.client.getLevelStorage().createSession(summary.getName());
                    this.client.setScreen(EditWorldScreen.create(this.client, session, (saved) -> {
*///? }
                        if (saved) {
                            this.loadWorldList();
                        }
//? if >= 26 {
                        this.minecraft.setScreen(this);
//? } else {
                        /*this.client.setScreen(this);
*///? }
                    }));
                } catch (IOException e) {
                    LOGGER.error("Failed to access world {}", summary.getName(), e);
//? if >= 26 {
                    SystemToast.addWorldAccessFailureToast(this.minecraft, summary.getName());
//? } else {
                    /*SystemToast.addWorldAccessFailureToast(this.client, summary.getName());
*///? }
                } catch (SymlinkValidationException e) {
                    LOGGER.warn("Failed to validate symlinks for world {}", summary.getName(), e);
//? if >= 26 {
                    SystemToast.addWorldAccessFailureToast(this.minecraft, summary.getName());
//? } else {
                    /*SystemToast.addWorldAccessFailureToast(this.client, summary.getName());
*///? }
                }
            }
//? if >= 26 {
        }).bounds(listWidth, 100, buttonWidth, 20).build();
        this.deleteButton = Button.builder(Component.translatable("selectWorld.delete"), button -> this.delete(getSelectedSummaries())).bounds(listWidth, 120, buttonWidth, 20).build();
//? } else {
        /*}).dimensions(listWidth, 100, buttonWidth, 20).build();
        this.deleteButton = ButtonWidget.builder(Text.translatable("selectWorld.delete"), button -> this.delete(getSelectedSummaries())).dimensions(listWidth, 120, buttonWidth, 20).build();
*///? }

//? if >= 26 {
        this.addRenderableWidget(this.playButton);
        this.addRenderableWidget(this.createButton);
        this.addRenderableWidget(this.editButton);
        this.addRenderableWidget(this.deleteButton);
//? } else {
        /*this.addDrawableChild(this.playButton);
        this.addDrawableChild(this.createButton);
        this.addDrawableChild(this.editButton);
        this.addDrawableChild(this.deleteButton);
*///? }

//? if >= 26 {
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.minecraft.setScreen(this.parent)).bounds(listWidth, this.height - 40, buttonWidth, 20).build());
//? } else {
        /*this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> this.client.setScreen(this.parent)).dimensions(listWidth, this.height - 40, buttonWidth, 20).build());
*///? }

        updateButtonStates();
    }

    private void loadWorldList() {
        this.worldList.clearEntries();
        try {
//? if >= 26 {
            LevelStorage.LevelList levelList = this.minecraft.getLevelStorage().getLevelList();
            List<LevelSummary> summaries = this.minecraft.getLevelStorage().loadSummaries(levelList).join();
//? } else {
            /*LevelStorage.LevelList levelList = this.client.getLevelStorage().getLevelList();
            List<LevelSummary> summaries = this.client.getLevelStorage().loadSummaries(levelList).join();
*///? }

            for (LevelSummary summary : summaries) {
                this.worldList.addEntry(new WorldListEntryWidget(this, summary, 0, 0, 30));
            }
        } catch (LevelStorageException e) {
            LOGGER.error("Couldn't load worlds", e);
        }
    }

    private void filterWorlds(String filter) {
        this.loadWorldList();
        if (!filter.isEmpty()) {
            String lowerFilter = filter.toLowerCase();
            this.worldList.getEntries().removeIf(entry -> !entry.getSummary().getDisplayName().toLowerCase().contains(lowerFilter) && !entry.getSummary().getName().toLowerCase().contains(lowerFilter));
        }
    }

    @Override
//? if >= 26 {
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
//? } else {
    /*public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

*///? }
        this.worldList.render(context, mouseX, mouseY, delta);
        this.searchBox.render(context, mouseX, mouseY, delta);

//? if >= 26 {
        context.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
//? } else {
        /*context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
*///? }

//? if >= 26 {
        super.extractRenderState(context, mouseX, mouseY, delta);
//? } else {
        /*super.render(context, mouseX, mouseY, delta);
*///? }

        updateButtonStates();
    }

    private void updateButtonStates() {
        int selectionCount = this.worldList.getSelectedEntries().size();
        this.playButton.active = selectionCount == 1;
        this.editButton.active = selectionCount == 1;
        this.deleteButton.active = selectionCount > 0;
    }

    public ScrollableListWidget<WorldListEntryWidget> getList() {
        return this.worldList;
    }

    public void play(Set<LevelSummary> summaries) {
        if (summaries.size() == 1) {
            LevelSummary summary = summaries.iterator().next();
//? if >= 26 {
            client.createIntegratedServerLoader().start(summary.getName(), () -> this.minecraft.setScreen(this));
//? } else {
            /*client.createIntegratedServerLoader().start(summary.getName(), () -> this.client.setScreen(this));
*///? }
        }
    }

    public void delete(Set<LevelSummary> summaries) {
        if (summaries.isEmpty()) return;

//? if >= 26 {
        Component title = Component.translatable("selectWorld.deleteQuestion");
        Component message = Component.translatable("selectWorld.deleteWarning", summaries.stream().map(LevelSummary::getDisplayName).collect(Collectors.joining(", ")));
//? } else {
        /*Text title = Text.translatable("selectWorld.deleteQuestion");
        Text message = Text.translatable("selectWorld.deleteWarning", summaries.stream().map(LevelSummary::getDisplayName).collect(Collectors.joining(", ")));
*///? }

//? if >= 26 {
        this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
//? } else {
        /*this.client.setScreen(new ConfirmScreen(confirmed -> {
*///? }
            if (confirmed) {
                try {
                    for (LevelSummary summary : summaries) {
//? if >= 26 {
                        try (LevelStorage.Session session = this.minecraft.getLevelStorage().createSession(summary.getName())) {
//? } else {
                        /*try (LevelStorage.Session session = this.client.getLevelStorage().createSession(summary.getName())) {
*///? }
                            session.deleteSessionLock();
                        } catch (SymlinkValidationException e) {
                            LOGGER.warn("Failed to validate symlinks for world {}", summary.getName(), e);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to delete worlds", e);
                }
                this.loadWorldList();
            }
//? if >= 26 {
            this.minecraft.setScreen(this);
//? } else {
            /*this.client.setScreen(this);
*///? }
        }, title, message));
    }

    private Set<LevelSummary> getSelectedSummaries() {
        return this.worldList.getSelectedEntries().stream()
                .map(WorldListEntryWidget::getSummary)
                .collect(Collectors.toSet());
    }

    @Override
    public void close() {
//? if >= 26 {
        this.minecraft.setScreen(this.parent);
//? } else {
        /*this.client.setScreen(this.parent);
*///? }
    }
}
