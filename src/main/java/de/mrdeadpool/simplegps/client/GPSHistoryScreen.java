package de.mrdeadpool.simplegps.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import de.mrdeadpool.simplegps.client.GPSLang;


import java.util.Comparator;
import java.util.List;

public class GPSHistoryScreen extends Screen {


    // ── Layout ────────────────────────────────────────────────────────────
    private static final int GUI_W = 500;
    private static final int FAV_W = 160;
    private static final int GUI_H = 300;
    private static final int ROW_H = 22;
    private static final int HEADER_H = 65;
    private static final int FOOTER_H = 30;

    // Eingabefelder oben
    private EditBox fieldX, fieldY, fieldZ, fieldName;

    // Bestätigungs-Overlay
    private GPSHistoryEntry pendingEntry = null;
    private GPSHistoryEntry pendingDelete = null;

    // Kategorien
    private GPSCategory selectedCategory = null;  // aufgeklappte Kategorie
    private boolean showNewCategoryOverlay = false; // Overlay sichtbar?
    private String newCategoryName = "";           // Eingabe im Overlay
    private boolean newCategoryFieldFocused = false;
    private GPSHistoryEntry contextEntry = null;
    private boolean showCategoryOverlay = false;
    private int selectedCategoryIndex = 0;
    private boolean showCategoryDropdown = false;
    private boolean showOptionsOverlay = false;
    private boolean journeyMapEnabled = false;
    private boolean draggingSound = false;

    private Button closeButton;
    private Button stopNavigationButton;
    private Button startButton;

    // Portal Overlay
    private boolean showNewPortalOverlay = false;

    private List<DimensionOption> portalDimensions = new java.util.ArrayList<>();

    private int selectedPortalFromDim = 0;
    private int selectedPortalToDim = 0;

    private boolean showPortalFromDropdown = false;
    private boolean showPortalToDropdown = false;

    private String portalFromXText = "";
    private String portalFromYText = "";
    private String portalFromZText = "";

    private String portalToXText = "";
    private String portalToYText = "";
    private String portalToZText = "";

    private boolean portalFromXFocused = false;
    private boolean portalFromYFocused = false;
    private boolean portalFromZFocused = false;

    private boolean portalToXFocused = false;
    private boolean portalToYFocused = false;
    private boolean portalToZFocused = false;


    // Sortierung
    private String sortColumn = "nr";
    private boolean sortAsc = true;

    // Modus
    private boolean showRouteView = false;  // false = Verlauf, true = Routen
    private boolean showPortalView = false; // Portale
    private boolean showModeDropdown = false;

    // Routen
    private GPSRoute selectedRoute = null;
    private boolean showNewRouteOverlay = false;
    private String newRouteName = "";
    private boolean newRouteFieldFocused = false;

    private GPSHistoryEntry routeContextEntry = null;
    private boolean showRouteOverlay = false;
    private int selectedRouteIndex = 0;
    private boolean showRouteDropdown = false;

    private int scrollOffset = 0;
    private int routeScrollOffset = 0;
    private int portalScrollOffset = 0;
    private int portalFromScrollOffset = 0;
    private int portalToScrollOffset = 0;
    private static final int MAX_PORTAL_DROPDOWN_VISIBLE = 6;
    private static final int PORTAL_DROPDOWN_ENTRY_H = 14;
    private static final int MAX_VISIBLE = 6;

    public GPSHistoryScreen() {
        super(Component.translatable("gps.history"));
    }
    private static final int GUI_OFFSET_Y = -70; // Verlauf nach oben

    @Override
    protected void init() {
        GPSSettings.load();
        journeyMapEnabled = GPSSettings.isJourneyMapEnabled();

        int left = (width - GUI_W) / 2;
        int top  = (height - GUI_H) / 2 + GUI_OFFSET_Y;

        // ── Koordinaten-Felder ─────────────────────────────────────────
        int fieldY_pos = top + 24;
        int fieldW = 70;

        fieldX = new EditBox(font, left + 30,  fieldY_pos, fieldW, 16,
                Component.literal("X"));
        fieldX.setHint(Component.literal("X"));
        fieldX.setMaxLength(10);
        addRenderableWidget(fieldX);

        fieldY = new EditBox(font, left + 120, fieldY_pos, fieldW, 16,
                Component.literal("Y"));
        fieldY.setHint(Component.literal("Y"));
        fieldY.setMaxLength(10);
        addRenderableWidget(fieldY);

        fieldZ = new EditBox(font, left + 210, fieldY_pos, fieldW, 16,
                Component.literal("Z"));
        fieldZ.setHint(Component.literal("Z"));
        fieldZ.setMaxLength(10);
        addRenderableWidget(fieldZ);

        fieldName = new EditBox(font, left + 295, fieldY_pos, 80, 16,
                Component.literal("Name"));
        fieldName.setHint(Component.literal("Name (optional)"));
        fieldName.setMaxLength(32);
        addRenderableWidget(fieldName);




        // ── Start-Button ───────────────────────────────────────────────
        startButton = Button.builder(
                Component.literal("▶ Start"),
                btn -> onStartClicked()
        ).bounds(left + 385, fieldY_pos - 1, 80, 18).build();
        addRenderableWidget(startButton);

        stopNavigationButton = Button.builder(
                Component.literal("⬛ ").append(Component.translatable("gps.end")),
                btn -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.player.connection.sendCommand("gps end");
                    }
                    onClose();
                }
        ).bounds(left + GUI_W / 2 - 80, top + GUI_H - 46, 160, 18).build();
        addRenderableWidget(stopNavigationButton);


        closeButton = Button.builder(
                Component.literal("✖ ").append(Component.translatable("gps.close")),
                btn -> onClose()
        ).bounds(left + GUI_W / 2 - 50, top + GUI_H - 24, 100, 18).build();
        addRenderableWidget(closeButton);

        portalDimensions = GPSDimensionHelper.getAvailableDimensions();

        if (portalDimensions.isEmpty()) {
            portalDimensions.add(new DimensionOption("minecraft:overworld", "Overworld"));
            portalDimensions.add(new DimensionOption("minecraft:the_nether", "Nether"));
            portalDimensions.add(new DimensionOption("minecraft:the_end", "End"));
        }

    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // Nichts tun – wir zeichnen unseren eigenen Hintergrund
    }

    // ── Render ────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {

        if (showOptionsOverlay) {
            renderOptionsOverlay(g, mouseX, mouseY);
            return;
        }

        int left = (width - GUI_W) / 2;
        int top  = (height - GUI_H) / 2 + GUI_OFFSET_Y;
        Minecraft mc = Minecraft.getInstance();

        // Hintergrund-Panel
        g.fill(left, top, left + GUI_W, top + GUI_H, 0xFF111111);
        g.renderOutline(left, top, GUI_W, GUI_H, 0xFFFF0000);

        boolean hideStartButton = showNewPortalOverlay;
        boolean hideStopButton = showNewPortalOverlay || showPortalView;
        boolean hideCloseButton = showNewPortalOverlay ? true : false;

        if (startButton != null) {
            startButton.visible = !hideStartButton;
            startButton.active = !hideStartButton;
        }

        if (closeButton != null) {
            closeButton.visible = !hideCloseButton;
            closeButton.active = !hideCloseButton;
        }

        // Widgets wie EditBoxen und Buttons rendern
        super.render(g, mouseX, mouseY, delta);

        // Titel / Dropdown
        boolean hoverTitle = mouseX >= left + 4 && mouseX <= left + 160 &&
                mouseY >= top + 2 && mouseY <= top + 14;

        Component titleText;

        if (showPortalView) {
            titleText = Component.literal("§4§l")
                    .append(Component.translatable("gps.portal"));
        } else if (showRouteView) {
            titleText = Component.literal("§4§l")
                    .append(Component.translatable("gps.route.history"));
        } else {
            titleText = Component.literal("§4§l")
                    .append(Component.translatable("gps.history"));
        }

        g.drawString(font,
                titleText.copy().append(showModeDropdown ? " §7▲" : " §7▼"),
                left + 8, top + 4, 0xFFFFFFFF);

        if (hoverTitle) {
            g.fill(left + 4, top + 2, left + 160, top + 14, 0x22FFFFFF);
        }

        // Dropdown offen
        if (showModeDropdown) {
            int dropTop = top - 38;

            g.fill(left + 4, dropTop, left + 160, dropTop + 40, 0xFF111111);
            g.renderOutline(left + 4, dropTop, 156, 40, 0xFFFF0000);

            // =========================
            // HISTORY
            // =========================
            boolean hoverVerlauf =
                    mouseX >= left + 6 && mouseX <= left + 158 &&
                            mouseY >= dropTop + 2 && mouseY <= dropTop + 14;

            g.fill(left + 6, dropTop + 2, left + 158, dropTop + 14,
                    hoverVerlauf ? 0x33FFFFFF : 0x00000000);

            Component historyLine = (!showRouteView && !showPortalView)
                    ? Component.literal("§a▶ §f")
                    .append(Component.translatable("gps.history"))
                    : Component.literal("§7")
                    .append(Component.translatable("gps.history"));

            g.drawString(font, historyLine, left + 10, dropTop + 4, 0xFFFFFFFF);

            // =========================
            // ROUTE
            // =========================
            boolean hoverRouten =
                    mouseX >= left + 6 && mouseX <= left + 158 &&
                            mouseY >= dropTop + 14 && mouseY <= dropTop + 26;

            g.fill(left + 6, dropTop + 14, left + 158, dropTop + 26,
                    hoverRouten ? 0x33FFFFFF : 0x00000000);

            Component routeLine = showRouteView
                    ? Component.literal("§a▶ §f")
                    .append(Component.translatable("gps.route.history"))
                    : Component.literal("§7")
                    .append(Component.translatable("gps.route.history"));

            g.drawString(font, routeLine, left + 10, dropTop + 16, 0xFFFFFFFF);

            // =========================
            // PORTAL
            // =========================
            boolean hoverPortale =
                    mouseX >= left + 6 && mouseX <= left + 158 &&
                            mouseY >= dropTop + 26 && mouseY <= dropTop + 38;

            g.fill(left + 6, dropTop + 26, left + 158, dropTop + 38,
                    hoverPortale ? 0x33FFFFFF : 0x00000000);

            Component portalLine = showPortalView
                    ? Component.literal("§a▶ §f")
                    .append(Component.translatable("gps.portal"))
                    : Component.literal("§7")
                    .append(Component.translatable("gps.portal"));

            g.drawString(font, portalLine, left + 10, dropTop + 28, 0xFFFFFFFF);
        }

        // Trennlinie unter Eingabefeldern
        g.fill(left, top + HEADER_H, left + GUI_W, top + HEADER_H + 1, 0xFFFF0000);

        // Rahmen um Eingabefelder
        int fx = left + 30;
        int fy = top + 24;
        int fw = 70;
        int fh = 16;

        g.renderOutline(fx - 2,       fy - 2, fw + 4, fh + 4, 0xFFFF0000); // X
        g.renderOutline(fx + 90 - 2,  fy - 2, fw + 4, fh + 4, 0xFFFF0000); // Y
        g.renderOutline(fx + 180 - 2, fy - 2, fw + 4, fh + 4, 0xFFFF0000); // Z
        g.renderOutline(fx + 265 - 2, fy - 2, 80 + 4, fh + 4, 0xFFFF0000); // Name

        int tableTop = top + HEADER_H + 14;

        // Spaltenüberschriften
        if (showPortalView) {

            g.drawString(font, "§7Nr", left + 6, tableTop, 0xFFAAAAAA);

            g.drawString(font,
                    Component.literal("§e")
                            .append(Component.translatable("gps.name")),
                    left + 22, tableTop, 0xFFFFFFFF);

            g.drawString(font,
                    Component.translatable("gps.connection"),
                    left + 170, tableTop, 0xFFFFFFFF);

        } else {

            String arrow = sortAsc ? " §f▲" : " §f▼";

            g.drawString(font, "§7Nr", left + 6, tableTop, 0xFFAAAAAA);

            g.drawString(font,
                    Component.literal("§e")
                            .append(Component.translatable("gps.name"))
                            .append(sortColumn.equals("name") ? arrow : ""),
                    left + 22, tableTop, 0xFFFFFFFF);

            g.drawString(font,
                    Component.literal("§eX" + (sortColumn.equals("x") ? arrow : "")),
                    left + 150, tableTop, 0xFFFFFFFF);

            g.drawString(font,
                    Component.literal("§eY" + (sortColumn.equals("y") ? arrow : "")),
                    left + 200, tableTop, 0xFFFFFFFF);

            g.drawString(font,
                    Component.literal("§eZ" + (sortColumn.equals("z") ? arrow : "")),
                    left + 250, tableTop, 0xFFFFFFFF);

            g.drawString(font,
                    Component.literal("§e")
                            .append(Component.translatable("gps.dist"))
                            .append(sortColumn.equals("dist") ? arrow : ""),
                    left + 300, tableTop, 0xFFFFFFFF);

            g.drawString(font,
                    Component.literal("§e")
                            .append(Component.translatable("gps.date"))
                            .append(sortColumn.equals("date") ? arrow : ""),
                    left + 340, tableTop, 0xFFFFFFFF);
        }

        // Hauptansicht
        if (showPortalView) {
            renderPortalView(g, mouseX, mouseY, left, top, tableTop, mc);
        } else if (showRouteView) {
            renderRouteView(g, mouseX, mouseY, left, top, tableTop, mc);
        } else {
            List<GPSHistoryEntry> list = getSorted(GPSHistory.getAll());

            for (int i = 0; i < MAX_VISIBLE && i + scrollOffset < list.size(); i++) {
                GPSHistoryEntry e = list.get(i + scrollOffset);
                int rowBg = tableTop + 22 + i * ROW_H;
                int rowY = rowBg + (ROW_H - 9) / 2;

                if (i % 2 == 0) {
                    g.fill(left + 2, rowBg,
                            left + GUI_W - 2, rowBg + ROW_H - 4, 0x22FFFFFF);
                }

                if (mouseX >= left + 2 && mouseX <= left + GUI_W - 80 &&
                        mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                    g.fill(left + 2, rowBg,
                            left + GUI_W - 80, rowBg + ROW_H - 4, 0x33FFFFFF);

                    g.renderComponentTooltip(font, List.of(
                            Component.literal("§7Dimension: §f" + formatDimension(e.dimension()))
                    ), mouseX, mouseY);
                }

                String dist = "?";
                if (mc.player != null) {
                    double dx = mc.player.getX() - e.x();
                    double dy = mc.player.getY() - e.y();
                    double dz = mc.player.getZ() - e.z();
                    dist = (int) Math.sqrt(dx * dx + dy * dy + dz * dz) + "m";
                }

                g.drawString(font, "§7" + (i + scrollOffset + 1), left + 6, rowY, 0xFFFFFFFF);
                g.drawString(font, shorten(e.name(), 16), left + 22, rowY, 0xFFFFFFFF);
                g.drawString(font, "§f" + (int) e.x(), left + 150, rowY, 0xFFFFFFFF);
                g.drawString(font, "§f" + (int) e.y(), left + 200, rowY, 0xFFFFFFFF);
                g.drawString(font, "§f" + (int) e.z(), left + 250, rowY, 0xFFFFFFFF);
                g.drawString(font, "§a" + dist, left + 300, rowY, 0xFFFFFFFF);
                g.drawString(font, "§7" + e.date(), left + 340, rowY, 0xFFFFFFFF);

                String delText   = "[X]";
                String shareText = "[↗]";
                String favText   = GPSFavorites.isFavorite(e) ? "[★]" : "[☆]";
                String routeText = "[+]";
                String jmText    = "[📍]";

                int gap = 3;
                int rightPadding = 6;

                int jmW    = journeyMapEnabled ? font.width(jmText) : 0;
                int routeW = font.width(routeText);
                int favW   = font.width(favText);
                int shareW = font.width(shareText);
                int delW   = font.width(delText);

                int rightX = left + GUI_W - rightPadding;

                int btnJmX    = journeyMapEnabled ? rightX - jmW : rightX;
                int btnRouteX = (journeyMapEnabled ? btnJmX - gap : rightX) - routeW;
                int btnFavX   = btnRouteX - gap - favW;
                int btnShareX = btnFavX   - gap - shareW;
                int btnDelX   = btnShareX - gap - delW;

                boolean hoverDel = mouseX >= btnDelX && mouseX <= btnDelX + delW &&
                        mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;
                g.drawString(font, hoverDel ? "§c" + delText : "§7" + delText,
                        btnDelX, rowY, 0xFFFFFFFF);

                boolean hoverShare = mouseX >= btnShareX && mouseX <= btnShareX + shareW &&
                        mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;
                g.drawString(font, hoverShare ? "§b" + shareText : "§7" + shareText,
                        btnShareX, rowY, 0xFFFFFFFF);

                boolean hoverFav = mouseX >= btnFavX && mouseX <= btnFavX + favW &&
                        mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;
                g.drawString(font,
                        GPSFavorites.isFavorite(e)
                                ? "§6" + favText
                                : (hoverFav ? "§e" + favText : "§7" + favText),
                        btnFavX, rowY, 0xFFFFFFFF);

                if (journeyMapEnabled) {
                    boolean hoverJm = mouseX >= btnJmX && mouseX <= btnJmX + jmW &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;
                    g.drawString(font, hoverJm ? "§d" + jmText : "§7" + jmText,
                            btnJmX, rowY, 0xFFFFFFFF);
                }

                boolean hoverRoute = mouseX >= btnRouteX && mouseX <= btnRouteX + routeW &&
                        mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;
                g.drawString(font, hoverRoute ? "§a" + routeText : "§7" + routeText,
                        btnRouteX, rowY, 0xFFFFFFFF);
            }
        }

        // Favoriten-Panel
        int favLeft = left - FAV_W - 5;
        int favTop  = top;

        g.fill(favLeft, favTop, favLeft + FAV_W, favTop + GUI_H, 0xFF111111);
        g.renderOutline(favLeft, favTop, FAV_W, GUI_H, 0xFFFF0000);

        g.drawString(font,
                Component.literal("§4§l⭐ ")
                        .append(Component.translatable("gps.fav")),
                favLeft + 6, favTop + 4, 0xFFFFFFFF);
        g.fill(favLeft, favTop + 16, favLeft + FAV_W, favTop + 17, 0xFFFF0000);

        List<GPSHistoryEntry> favList = GPSFavorites.getAll();

        if (favList.isEmpty()) {
            g.drawString(font,
                    Component.literal("§7")
                            .append(Component.translatable("gps.no.fav")),
                    favLeft + 6, favTop + 24, 0xFFAAAAAA);

        } else {

            for (int i = 0; i < favList.size(); i++) {
                GPSHistoryEntry fav = favList.get(i);
                int favRowBg = favTop + 22 + i * ROW_H;
                int favRowY  = favRowBg + (ROW_H - 9) / 2;

                if (i % 2 == 0) {
                    g.fill(favLeft + 2, favRowBg,
                            favLeft + FAV_W - 2, favRowBg + ROW_H - 4, 0x22FFFFFF);
                }

                boolean hovering =
                        mouseX >= favLeft + 2 && mouseX <= favLeft + FAV_W - 30 &&
                                mouseY >= favRowBg && mouseY <= favRowBg + ROW_H - 4;

                if (hovering) {
                    g.fill(favLeft + 2, favRowBg,
                            favLeft + FAV_W - 2, favRowBg + ROW_H - 4, 0x33FFFFFF);

                    g.renderComponentTooltip(font, List.of(
                            Component.literal("§b" + fav.name()),
                            Component.literal("§eX: §f" + (int) fav.x()),
                            Component.literal("§aY: §f" + (int) fav.y()),
                            Component.literal("§dZ: §f" + (int) fav.z())
                    ), mouseX, mouseY);
                }

                String favDist = "?";
                String favDistColor = "§f";

                if (mc.player != null) {
                    double dx = mc.player.getX() - fav.x();
                    double dy = mc.player.getY() - fav.y();
                    double dz = mc.player.getZ() - fav.z();

                    int d = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    favDist = d + "m";
                    favDistColor = d <= 50 ? "§a" : d <= 200 ? "§e" : "§c";
                }

                g.drawString(font,
                        Component.literal(shorten(fav.name(), 9)),
                        favLeft + 4, favRowY, 0xFFFFFFFF);

                g.drawString(font,
                        Component.literal(favDistColor + favDist),
                        favLeft + 90, favRowY, 0xFFFFFFFF);

                boolean hoverFavDel =
                        mouseX >= favLeft + FAV_W - 32 &&
                                mouseX <= favLeft + FAV_W - 22 &&
                                mouseY >= favRowBg && mouseY <= favRowBg + ROW_H - 4;

                g.drawString(font,
                        Component.literal(hoverFavDel ? "§c[X]" : "§7[X]"),
                        favLeft + FAV_W - 32, favRowY, 0xFFFFFFFF);

                boolean hoverFavShare =
                        mouseX >= favLeft + FAV_W - 16 &&
                                mouseX <= favLeft + FAV_W - 4 &&
                                mouseY >= favRowBg && mouseY <= favRowBg + ROW_H - 4;

                g.drawString(font,
                        Component.literal(hoverFavShare ? "§b[↗]" : "§7[↗]"),
                        favLeft + FAV_W - 16, favRowY, 0xFFFFFFFF);
            }
        }

        if (GPSFavorites.isFull()) {
            g.drawString(font,
                    Component.literal("§c§l")
                            .append(Component.translatable("gps.full"))
                            .append(Component.literal(" (10/10)")),
                    favLeft + 6, favTop + GUI_H - 14, 0xFFFFFFFF);
        }

        // Kategorien-Panel
        int catLeft = left + GUI_W + 5;
        int catTop  = top;

        g.fill(catLeft, catTop, catLeft + FAV_W, catTop + GUI_H, 0xFF111111);
        g.renderOutline(catLeft, catTop, FAV_W, GUI_H, 0xFFFF0000);

        g.drawString(font,
                Component.literal("§4§l📁 ")
                        .append(Component.translatable("gps.categories")),
                catLeft + 6, catTop + 4, 0xFFFFFFFF);
        g.fill(catLeft, catTop + 16, catLeft + FAV_W, catTop + 17, 0xFFFF0000);

        boolean hoverNewCat = mouseX >= catLeft + 4 && mouseX <= catLeft + FAV_W - 4 &&
                mouseY >= catTop + 20 && mouseY <= catTop + 32;
        g.fill(catLeft + 4, catTop + 20, catLeft + FAV_W - 4, catTop + 32,
                hoverNewCat ? 0x44FFFFFF : 0x22FFFFFF);
        g.drawCenteredString(font,
                Component.literal("§a+ ")
                        .append(Component.translatable("gps.new.folder")),
                catLeft + FAV_W / 2, catTop + 22, 0xFFFFFFFF);

        g.fill(catLeft, catTop + 34, catLeft + FAV_W, catTop + 35, 0x44FFFFFF);

        List<GPSCategory> catList = GPSCategories.getAll();

        if (selectedCategory == null) {
            for (int i = 0; i < catList.size(); i++) {
                GPSCategory cat = catList.get(i);
                int catRowBg = catTop + 38 + i * ROW_H;
                int catRowY  = catRowBg + (ROW_H - 9) / 2;

                if (i % 2 == 0) {
                    g.fill(catLeft + 2, catRowBg,
                            catLeft + FAV_W - 2, catRowBg + ROW_H - 4, 0x22FFFFFF);
                }

                boolean hoverCat = mouseX >= catLeft + 2 &&
                        mouseX <= catLeft + FAV_W - 22 &&
                        mouseY >= catRowBg &&
                        mouseY <= catRowBg + ROW_H - 4;
                if (hoverCat) {
                    g.fill(catLeft + 2, catRowBg,
                            catLeft + FAV_W - 22, catRowBg + ROW_H - 4, 0x33FFFFFF);
                }

                g.drawString(font,
                        "§e📁 " + shorten(cat.getName(), 8)
                                + " §7(" + cat.getEntries().size() + ")",
                        catLeft + 4, catRowY, 0xFFFFFFFF);

                boolean hoverCatDel = mouseX >= catLeft + FAV_W - 18 &&
                        mouseX <= catLeft + FAV_W - 4 &&
                        mouseY >= catRowBg &&
                        mouseY <= catRowBg + ROW_H - 4;
                g.drawString(font, hoverCatDel ? "§c[X]" : "§7[X]",
                        catLeft + FAV_W - 18, catRowY, 0xFFFFFFFF);
            }

            if (catList.isEmpty()) {
                g.drawString(font,
                        Component.literal("§7")
                                .append(Component.translatable("gps.no.categories")),
                        catLeft + 6, catTop + 44, 0xFFAAAAAA);
            }

        } else {
            boolean hoverBack = mouseX >= catLeft + 2 && mouseX <= catLeft + FAV_W - 2 &&
                    mouseY >= catTop + 38 && mouseY <= catTop + 50;
            g.fill(catLeft + 2, catTop + 38, catLeft + FAV_W - 2, catTop + 50,
                    hoverBack ? 0x44FFFFFF : 0x22FFFFFF);
            g.drawString(font, "§f◀ " + shorten(selectedCategory.getName(), 9),
                    catLeft + 4, catTop + 40, 0xFFFFFFFF);

            g.fill(catLeft, catTop + 52, catLeft + FAV_W, catTop + 53, 0x44FFFFFF);

            List<GPSHistoryEntry> catEntries = selectedCategory.getEntries();
            if (catEntries.isEmpty()) {
                g.drawString(font,
                        Component.literal("§7")
                                .append(Component.translatable("gps.empty")),
                        catLeft + 6, catTop + 58, 0xFFAAAAAA);
            } else {
                for (int i = 0; i < catEntries.size(); i++) {
                    GPSHistoryEntry entry = catEntries.get(i);
                    int catRowBg = catTop + 56 + i * ROW_H;
                    int catRowY  = catRowBg + (ROW_H - 9) / 2;

                    if (i % 2 == 0) {
                        g.fill(catLeft + 2, catRowBg,
                                catLeft + FAV_W - 2, catRowBg + ROW_H - 4, 0x22FFFFFF);
                    }

                    boolean hoverEntry = mouseX >= catLeft + 2 &&
                            mouseX <= catLeft + FAV_W - 22 &&
                            mouseY >= catRowBg &&
                            mouseY <= catRowBg + ROW_H - 4;

                    if (hoverEntry) {
                        g.fill(catLeft + 2, catRowBg,
                                catLeft + FAV_W - 22, catRowBg + ROW_H - 4, 0x33FFFFFF);
                        g.renderComponentTooltip(font, List.of(
                                Component.literal("§f" + entry.name()),
                                Component.literal("§7X: §f" + (int) entry.x()),
                                Component.literal("§7Y: §f" + (int) entry.y()),
                                Component.literal("§7Z: §f" + (int) entry.z())
                        ), mouseX, mouseY);
                    }

                    String catDist = "?";
                    String catDistColor = "§f";
                    if (mc.player != null) {
                        double dx = mc.player.getX() - entry.x();
                        double dy = mc.player.getY() - entry.y();
                        double dz = mc.player.getZ() - entry.z();
                        int d = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
                        catDist = d + "m";
                        catDistColor = d <= 50 ? "§a" : d <= 200 ? "§e" : "§c";
                    }

                    g.drawString(font, shorten(entry.name(), 9),
                            catLeft + 4, catRowY, 0xFFFFFFFF);

                    g.drawString(font, catDistColor + catDist,
                            catLeft + 90, catRowY, 0xFFFFFFFF);

                    boolean hoverEntryDel = mouseX >= catLeft + FAV_W - 18 &&
                            mouseX <= catLeft + FAV_W - 4 &&
                            mouseY >= catRowBg &&
                            mouseY <= catRowBg + ROW_H - 4;

                    g.drawString(font,
                            hoverEntryDel ? "§c[X]" : "§7[X]",
                            catLeft + FAV_W - 18, catRowY, 0xFFFFFFFF);
                }
            }
        }

        List<GPSHistoryEntry> list2 = getSorted(GPSHistory.getAll());

        g.drawString(font,
                Component.literal("§7")
                        .append(Component.translatable("gps.foot.history"))
                        .append(" §f" + list2.size() + "§7/§f50"),
                left + 8, top + GUI_H - 14,
                0xFFFFFFFF);

        int gearX = left + GUI_W - 14;
        int gearY = top + GUI_H - 14;

        boolean hoverGear = mouseX >= gearX - 2 && mouseX <= gearX + 10 &&
                mouseY >= gearY - 2 && mouseY <= gearY + 10;

        g.drawString(font,
                hoverGear ? "§e⚙" : "§7⚙",
                gearX, gearY, 0xFFFFFFFF);

        // Overlays ganz zum Schluss
        if (showCategoryOverlay) {
            renderCategoryOverlay(g, mouseX, mouseY);
        }

        if (showNewPortalOverlay) {
            renderNewPortalOverlay(g, mouseX, mouseY);
        }

        if (showRouteOverlay) {
            renderRouteOverlay(g, mouseX, mouseY);
        }

        if (pendingEntry != null) {
            renderConfirmOverlay(g, mouseX, mouseY);
        }

        if (pendingDelete != null) {
            renderDeleteOverlay(g, mouseX, mouseY);
        }

        if (showNewCategoryOverlay) {
            renderNewCategoryOverlay(g, mouseX, mouseY);
        }

        if (showNewRouteOverlay) {
            renderNewRouteOverlay(g, mouseX, mouseY);
        }
        boolean inSpecialView = showRouteView || showPortalView;
        boolean showStopButton = !inSpecialView;

        if (stopNavigationButton != null) {
            stopNavigationButton.visible = showStopButton;
            stopNavigationButton.active = showStopButton;
        }
    }

    private void renderNewRouteOverlay(GuiGraphics g, int mouseX, int mouseY) {
        int ox = width  / 2 - 120;
        int oy = height / 2 + 80;

        g.fill(0, 0, width, height, 0x88000000);
        g.fill(ox, oy, ox + 240, oy + 80, 0xFF111111);
        g.renderOutline(ox, oy, 240, 80, 0xFFFF0000);

        g.drawCenteredString(font,
                Component.literal("§4§l")
                        .append(Component.translatable("gps.new.route")),
                width / 2, oy + 8, 0xFFFFFFFF);

        g.fill(ox + 10, oy + 24, ox + 230, oy + 38, 0xFF222222);
        g.renderOutline(ox + 10, oy + 24, 220, 14, 0xFFFF0000);
        Component routeDisplay;

        if (!newRouteFieldFocused && newRouteName.isEmpty()) {
            routeDisplay = Component.literal("§7")
                    .append(Component.translatable("gps.new.route"))
                    .append("...");
        } else {
            String text = newRouteName;
            if (newRouteFieldFocused && isCursorVisible()) {
                text += "|";
            }
            routeDisplay = Component.literal("§f" + text);
        }

        g.drawString(font, routeDisplay, ox + 14, oy + 27, 0xFFFFFFFF);

        boolean hoverCreate =
                mouseX >= ox + 10 && mouseX <= ox + 110 &&
                        mouseY >= oy + 52 && mouseY <= oy + 72;

        int createColor = hoverCreate ? 0xFF1B5E20 : 0xFF224422;
        int createBorder = hoverCreate ? 0xFF66BB6A : 0xFF44AA44;

        g.fill(ox + 10, oy + 52, ox + 110, oy + 72, createColor);
        g.renderOutline(ox + 10, oy + 52, 100, 20, createBorder);
        g.drawCenteredString(font,
                Component.literal("§a✔ ")
                        .append(Component.translatable("gps.create")),
                ox + 60, oy + 58, 0xFFFFFFFF);

        boolean hoverCancel =
                mouseX >= ox + 130 && mouseX <= ox + 230 &&
                        mouseY >= oy + 52 && mouseY <= oy + 72;

        int cancelColor = hoverCancel ? 0xFF5A1A1A : 0xFF442222;
        int cancelBorder = hoverCancel ? 0xFFEF5350 : 0xFFAA4444;

        g.fill(ox + 130, oy + 52, ox + 230, oy + 72, cancelColor);
        g.renderOutline(ox + 130, oy + 52, 100, 20, cancelBorder);
        g.drawCenteredString(font,
                Component.literal("§c✖ ")
                        .append(Component.translatable("gps.cancel")),
                ox + 180, oy + 58, 0xFFFFFFFF);
    }

    private void renderPortalView(GuiGraphics g, int mouseX, int mouseY,
                                  int left, int top, int tableTop, Minecraft mc) {
        List<GPSPortalLink> portals = GPSPortals.getAll();

        int btnY = top + GUI_H - 28;
        boolean hoverNew = mouseX >= left + 4 && mouseX <= left + 150 &&
                mouseY >= btnY && mouseY <= btnY + 14;

        g.fill(left + 4, btnY, left + 150, btnY + 14,
                hoverNew ? 0x44FFFFFF : 0x22FFFFFF);
        g.drawString(font,
                Component.literal("§a+ ")
                        .append(Component.translatable("gps.new.portal")),
                left + 8, btnY + 3, 0xFFFFFFFF);

        if (portals.isEmpty()) {
            g.drawString(font,
                    Component.literal("§7")
                            .append(Component.translatable("gps.no.portal")),
                    left + 8, btnY + 20, 0xFFAAAAAA);
            return;
        }

        for (int i = 0; i < MAX_VISIBLE && i + portalScrollOffset < portals.size(); i++) {
            GPSPortalLink portal = portals.get(i + portalScrollOffset);
            int rowBg = tableTop + 16 + i * ROW_H;
            int rowY = rowBg + (ROW_H - 9) / 2;

            if (i % 2 == 0) {
                g.fill(left + 2, rowBg,
                        left + GUI_W - 2, rowBg + ROW_H - 4, 0x22FFFFFF);
            }

            boolean hover = mouseX >= left + 2 && mouseX <= left + GUI_W - 30 &&
                    mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;
            if (hover) {
                g.fill(left + 2, rowBg,
                        left + GUI_W - 30, rowBg + ROW_H - 4, 0x33FFFFFF);

                g.renderComponentTooltip(font, List.of(
                        Component.literal("§e" + portal.name()),
                        Component.literal("§7")
                                .append(Component.translatable("gps.from"))
                                .append("§f" + GPSDimensionHelper.toDisplayName(portal.fromDimension())),
                        Component.literal("§7")
                                .append(Component.translatable("gps.to"))
                                .append("§f" + GPSDimensionHelper.toDisplayName(portal.toDimension())),
                        Component.literal("§7")
                                .append(Component.translatable("gps.beginn"))
                                .append("§f" + (int) portal.fromX() + ", " + (int) portal.fromY() + ", " + (int) portal.fromZ()),
                        Component.literal("§7")
                                .append(Component.translatable("gps.desti"))
                                .append("§f" + (int) portal.toX() + ", " + (int) portal.toY() + ", " + (int) portal.toZ())
                ), mouseX, mouseY);
            }

            g.drawString(font, "§7" + (i + portalScrollOffset + 1),
                    left + 6, rowY, 0xFFFFFFFF);

            g.drawString(font, "§e" + shorten(portal.name(), 22),
                    left + 22, rowY, 0xFFFFFFFF);

            g.drawString(font,
                    "§7" + GPSDimensionHelper.toDisplayName(portal.fromDimension())
                            + " §f→ §7"
                            + GPSDimensionHelper.toDisplayName(portal.toDimension()),
                    left + 170, rowY, 0xFFFFFFFF);

            boolean hoverDel = mouseX >= left + GUI_W - 28 &&
                    mouseX <= left + GUI_W - 15 &&
                    mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;
            g.drawString(font, hoverDel ? "§c[X]" : "§7[X]",
                    left + GUI_W - 28, rowY, 0xFFFFFFFF);
        }
    }

    // Portal-Overlay
    private void renderNewPortalOverlay(GuiGraphics g, int mouseX, int mouseY) {
        int ox = width / 2 - 170;
        int oy = height / 2 + 40;

        g.fill(0, 0, width, height, 0x88000000);
        g.fill(ox, oy, ox + 340, oy + 190, 0xFF111111);
        g.renderOutline(ox, oy, 340, 190, 0xFFFF0000);

        if (!showPortalToDropdown && !showPortalFromDropdown) {
            g.drawCenteredString(font,
                    Component.literal("§4§l")
                            .append(Component.translatable("gps.new.portal")),
                    width / 2, oy + 8, 0xFFFFFFFF);
        }

        // Start Dimension
        if (!showPortalFromDropdown) {
            g.drawString(font,
                    Component.literal("§f")
                            .append(Component.translatable("gps.start.destin")),
                    ox + 14, oy + 28, 0xFFFFFFFF);
        }

        g.fill(ox + 14, oy + 42, ox + 144, oy + 58, 0xFF222222);
        g.renderOutline(ox + 14, oy + 42, 130, 16, 0xFFFF0000);
        g.drawString(font,
                "§f" + portalDimensions.get(selectedPortalFromDim).displayName(),
                ox + 20, oy + 46, 0xFFFFFFFF);
        g.drawString(font, showPortalFromDropdown ? "§7▲" : "§7▼", ox + 130, oy + 46, 0xFFFFFFFF);

        // Ziel Dimension
        if (!showPortalToDropdown) {
            g.drawString(font,
                    Component.literal("§f")
                            .append(Component.translatable("gps.destin")),
                    ox + 196, oy + 28, 0xFFFFFFFF);
        }

        g.fill(ox + 196, oy + 42, ox + 326, oy + 58, 0xFF222222);
        g.renderOutline(ox + 196, oy + 42, 130, 16, 0xFFFF0000);
        g.drawString(font,
                "§f" + portalDimensions.get(selectedPortalToDim).displayName(),
                ox + 202, oy + 46, 0xFFFFFFFF);
        g.drawString(font, showPortalToDropdown ? "§7▲" : "§7▼", ox + 312, oy + 46, 0xFFFFFFFF);

        // Dropdown links (nach oben, mit Scroll)
        if (showPortalFromDropdown) {
            int visibleCount = Math.min(MAX_PORTAL_DROPDOWN_VISIBLE, portalDimensions.size());
            int startIndex = portalFromScrollOffset;

            int dropdownHeight = visibleCount * PORTAL_DROPDOWN_ENTRY_H;
            int dy = oy + 42 - dropdownHeight;

            for (int visibleRow = 0; visibleRow < visibleCount; visibleRow++) {
                int index = startIndex + visibleRow;
                int rowY = dy + visibleRow * PORTAL_DROPDOWN_ENTRY_H;

                boolean hover = mouseX >= ox + 14 && mouseX <= ox + 144 &&
                        mouseY >= rowY && mouseY <= rowY + PORTAL_DROPDOWN_ENTRY_H;

                g.fill(ox + 14, rowY, ox + 144, rowY + PORTAL_DROPDOWN_ENTRY_H,
                        hover ? 0xFF333333 : 0xFF222222);
                g.renderOutline(ox + 14, rowY, 130, PORTAL_DROPDOWN_ENTRY_H, 0xFF444444);

                g.drawString(font,
                        "§f" + portalDimensions.get(index).displayName(),
                        ox + 18, rowY + 3, 0xFFFFFFFF);
            }

            // Scrollbar nur wenn mehr als 6 Einträge
            if (portalDimensions.size() > MAX_PORTAL_DROPDOWN_VISIBLE) {
                int barX = ox + 146;
                int barY = dy;
                int barH = dropdownHeight;

                g.fill(barX, barY, barX + 4, barY + barH, 0xFF1A1A1A);

                int maxOffset = portalDimensions.size() - MAX_PORTAL_DROPDOWN_VISIBLE;
                int thumbH = Math.max(10, (barH * MAX_PORTAL_DROPDOWN_VISIBLE) / portalDimensions.size());
                int thumbY = barY + (maxOffset == 0 ? 0 :
                        (portalFromScrollOffset * (barH - thumbH)) / maxOffset);

                g.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFFF0000);
            }
        }

        // Dropdown rechts (nach oben, mit Scroll)
        if (showPortalToDropdown) {
            int visibleCount = Math.min(MAX_PORTAL_DROPDOWN_VISIBLE, portalDimensions.size());
            int startIndex = portalToScrollOffset;

            int dropdownHeight = visibleCount * PORTAL_DROPDOWN_ENTRY_H;
            int dy = oy + 42 - dropdownHeight;

            for (int visibleRow = 0; visibleRow < visibleCount; visibleRow++) {
                int index = startIndex + visibleRow;
                int rowY = dy + visibleRow * PORTAL_DROPDOWN_ENTRY_H;

                boolean hover = mouseX >= ox + 196 && mouseX <= ox + 326 &&
                        mouseY >= rowY && mouseY <= rowY + PORTAL_DROPDOWN_ENTRY_H;

                g.fill(ox + 196, rowY, ox + 326, rowY + PORTAL_DROPDOWN_ENTRY_H,
                        hover ? 0xFF333333 : 0xFF222222);
                g.renderOutline(ox + 196, rowY, 130, PORTAL_DROPDOWN_ENTRY_H, 0xFF444444);

                g.drawString(font,
                        "§f" + portalDimensions.get(index).displayName(),
                        ox + 200, rowY + 3, 0xFFFFFFFF);
            }

            // Scrollbar nur wenn mehr als 6 Einträge
            if (portalDimensions.size() > MAX_PORTAL_DROPDOWN_VISIBLE) {
                int barX = ox + 328;
                int barY = dy;
                int barH = dropdownHeight;

                g.fill(barX, barY, barX + 4, barY + barH, 0xFF1A1A1A);

                int maxOffset = portalDimensions.size() - MAX_PORTAL_DROPDOWN_VISIBLE;
                int thumbH = Math.max(10, (barH * MAX_PORTAL_DROPDOWN_VISIBLE) / portalDimensions.size());
                int thumbY = barY + (maxOffset == 0 ? 0 :
                        (portalToScrollOffset * (barH - thumbH)) / maxOffset);

                g.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFFF0000);
            }
        }

        int fieldsY = oy + 92;

        // Start Portal Überschrift
        g.drawString(font,
                Component.literal("§f")
                        .append(Component.translatable("gps.start.portal")),
                ox + 20, oy + 74, 0xFFFFFFFF);

        // Start X
        g.fill(ox + 20, oy + 92, ox + 70, oy + 108, 0xFF222222);
        g.renderOutline(ox + 20, oy + 92, 50, 16, portalFromXFocused ? 0xFFFFFF00 : 0xFFFF0000);
        g.drawCenteredString(font,
                portalFromXText.isEmpty() ? "§7X" : "§f" + portalFromXText,
                ox + 45, oy + 96, 0xFFFFFFFF);

        // Start Y
        g.fill(ox + 80, oy + 92, ox + 130, oy + 108, 0xFF222222);
        g.renderOutline(ox + 80, oy + 92, 50, 16, portalFromYFocused ? 0xFFFFFF00 : 0xFFFF0000);
        g.drawCenteredString(font,
                portalFromYText.isEmpty() ? "§7Y" : "§f" + portalFromYText,
                ox + 105, oy + 96, 0xFFFFFFFF);

        // Start Z
        g.fill(ox + 140, oy + 92, ox + 190, oy + 108, 0xFF222222);
        g.renderOutline(ox + 140, oy + 92, 50, 16, portalFromZFocused ? 0xFFFFFF00 : 0xFFFF0000);
        g.drawCenteredString(font,
                portalFromZText.isEmpty() ? "§7Z" : "§f" + portalFromZText,
                ox + 165, oy + 96, 0xFFFFFFFF);

        // Ziel Portal Überschrift
        g.drawString(font,
                Component.literal("§f")
                        .append(Component.translatable("gps.tar.portal")),
                ox + 20, oy + 112, 0xFFFFFFFF);

        // Ziel X
        g.fill(ox + 20, oy + 130, ox + 70, oy + 146, 0xFF222222);
        g.renderOutline(ox + 20, oy + 130, 50, 16, portalToXFocused ? 0xFFFFFF00 : 0xFFFF0000);
        g.drawCenteredString(font,
                portalToXText.isEmpty() ? "§7X" : "§f" + portalToXText,
                ox + 45, oy + 134, 0xFFFFFFFF);

        // Ziel Y
        g.fill(ox + 80, oy + 130, ox + 130, oy + 146, 0xFF222222);
        g.renderOutline(ox + 80, oy + 130, 50, 16, portalToYFocused ? 0xFFFFFF00 : 0xFFFF0000);
        g.drawCenteredString(font,
                portalToYText.isEmpty() ? "§7Y" : "§f" + portalToYText,
                ox + 105, oy + 134, 0xFFFFFFFF);

        // Ziel Z
        g.fill(ox + 140, oy + 130, ox + 190, oy + 146, 0xFF222222);
        g.renderOutline(ox + 140, oy + 130, 50, 16, portalToZFocused ? 0xFFFFFF00 : 0xFFFF0000);
        g.drawCenteredString(font,
                portalToZText.isEmpty() ? "§7Z" : "§f" + portalToZText,
                ox + 165, oy + 134, 0xFFFFFFFF);

        // Buttons
        boolean hoverConfirm =
                mouseX >= ox + 20 && mouseX <= ox + 150 &&
                        mouseY >= oy + 160 && mouseY <= oy + 180;

        int confirmColor = hoverConfirm ? 0xFF1B5E20 : 0xFF224422;
        int confirmBorder = hoverConfirm ? 0xFF66BB6A : 0xFF44AA44;

        g.fill(ox + 20, oy + 160, ox + 150, oy + 180, confirmColor);
        g.renderOutline(ox + 20, oy + 160, 130, 20, confirmBorder);
        g.drawCenteredString(font,
                Component.literal("§a✔ ")
                        .append(Component.translatable("gps.confirm")),
                ox + 85, oy + 166, 0xFFFFFFFF);

        boolean hoverCancel =
                mouseX >= ox + 190 && mouseX <= ox + 320 &&
                        mouseY >= oy + 160 && mouseY <= oy + 180;

        int cancelColor = hoverCancel ? 0xFF5A1A1A : 0xFF442222;
        int cancelBorder = hoverCancel ? 0xFFEF5350 : 0xFFAA4444;

        g.fill(ox + 190, oy + 160, ox + 320, oy + 180, cancelColor);
        g.renderOutline(ox + 190, oy + 160, 130, 20, cancelBorder);
        g.drawCenteredString(font,
                Component.literal("§c✖ ")
                        .append(Component.translatable("gps.cancel")),
                ox + 240, oy + 166, 0xFFFFFFFF);
    }

    // ── Bestätigungs-Overlay ──────────────────────────────────────────────
    private void renderConfirmOverlay(GuiGraphics g, int mouseX, int mouseY) {
        if (pendingEntry == null) return;

        int ox = width  / 2 - 120;  // breiter: war -100
        int oy = height / 2 + 80;   // höher: war -35

        // Abdunklung des gesamten Hintergrunds
        g.fill(0, 0, width, height, 0x88000000);

        // Overlay-Panel
        g.fill(ox, oy, ox + 240, oy + 90, 0xFF111111);  // dunkler Hintergrund
        g.renderOutline(ox, oy, 240, 90, 0xFFFF0000);    // roter Rahmen

        g.drawCenteredString(font,
                Component.literal("§f§l")
                        .append(Component.translatable("gps.start.ask.navi")),
                width / 2, oy + 10, 0xFFFFFFFF);
        g.drawCenteredString(font,
                "§e" + pendingEntry.name(), width / 2, oy + 26, 0xFFFFFFFF);
        g.drawCenteredString(font,
                "§7" + (int)pendingEntry.x() + ", "
                        + (int)pendingEntry.y() + ", "
                        + (int)pendingEntry.z(),
                width / 2, oy + 40, 0xFFAAAAAA);
        boolean hoverYes =
                mouseX >= ox + 10 && mouseX <= ox + 110 &&
                        mouseY >= oy + 62 && mouseY <= oy + 82;

        int yesColor = hoverYes ? 0xFF1B5E20 : 0xFF224422;
        int yesBorder = hoverYes ? 0xFF66BB6A : 0xFF44AA44;

        g.fill(ox + 10, oy + 62, ox + 110, oy + 82, yesColor);
        g.renderOutline(ox + 10, oy + 62, 100, 20, yesBorder);
        g.drawCenteredString(font,
                Component.literal("§a✔ ")
                        .append(Component.translatable("gps.yes")),
                ox + 60, oy + 68, 0xFFFFFFFF);

        boolean hoverNo =
                mouseX >= ox + 130 && mouseX <= ox + 230 &&
                        mouseY >= oy + 62 && mouseY <= oy + 82;

        int noColor = hoverNo ? 0xFF5A1A1A : 0xFF442222;
        int noBorder = hoverNo ? 0xFFEF5350 : 0xFFAA4444;

        g.fill(ox + 130, oy + 62, ox + 230, oy + 82, noColor);
        g.renderOutline(ox + 130, oy + 62, 100, 20, noBorder);
        g.drawCenteredString(font,
                Component.literal("§c✖ ")
                        .append(Component.translatable("gps.no")),
                ox + 180, oy + 68, 0xFFFFFFFF);
    }

    private void renderDeleteOverlay(GuiGraphics g, int mouseX, int mouseY) {
        if (pendingDelete == null) return;

        // Gesamten Bildschirm abdunkeln
        g.fill(0, 0, width, height, 0xAA000000);

        int ox = width  / 2 - 120;
        int oy = height / 2 + 80;

        // Solider Hintergrund (kein CC sondern FF = komplett undurchsichtig)
        g.fill(ox, oy, ox + 240, oy + 90, 0xFF111111);
        g.renderOutline(ox, oy, 240, 90, 0xFFFF5555);

        g.drawCenteredString(font,
                "§c" + Component.translatable("gps.sure").getString(),
                width / 2, oy + 8, 0xFFFFFFFF);

        g.drawCenteredString(font,
                "§7\"" + pendingDelete.name() + "\" " +
                        Component.translatable("gps.delete").getString(),
                width / 2, oy + 26, 0xFFAAAAAA);

        boolean hoverYes =
                mouseX >= ox + 10 && mouseX <= ox + 110 &&
                        mouseY >= oy + 62 && mouseY <= oy + 82;

        int yesColor = hoverYes ? 0xFF1B5E20 : 0xFF224422;
        int yesBorder = hoverYes ? 0xFF66BB6A : 0xFF44AA44;

        g.fill(ox + 10, oy + 62, ox + 110, oy + 82, yesColor);
        g.renderOutline(ox + 10, oy + 62, 100, 20, yesBorder);
        g.drawCenteredString(font,
                Component.literal("§a✔ ")
                        .append(Component.translatable("gps.yes")),
                ox + 60, oy + 68, 0xFFFFFFFF);

        boolean hoverNo =
                mouseX >= ox + 130 && mouseX <= ox + 230 &&
                        mouseY >= oy + 62 && mouseY <= oy + 82;

        int noColor = hoverNo ? 0xFF5A1A1A : 0xFF442222;
        int noBorder = hoverNo ? 0xFFEF5350 : 0xFFAA4444;

        g.fill(ox + 130, oy + 62, ox + 230, oy + 82, noColor);
        g.renderOutline(ox + 130, oy + 62, 100, 20, noBorder);
        g.drawCenteredString(font,
                Component.literal("§c✖ ")
                        .append(Component.translatable("gps.no")),
                ox + 180, oy + 68, 0xFFFFFFFF);
    }

    private void renderOptionsOverlay(GuiGraphics g, int mouseX, int mouseY) {
        int ox = width / 2 - 120;
        int oy = height / 2 - 50;

        // Hintergrund abdunkeln
        g.fill(0, 0, width, height, 0xCC000000);

        // Fenster
        g.fill(ox, oy, ox + 240, oy + 110, 0xFF111111);
        g.renderOutline(ox, oy, 240, 110, 0xFFFF0000);

        // Titel
        g.drawCenteredString(font,
                Component.literal("§4§l")
                        .append(Component.translatable("gps.option")),
                width / 2, oy + 8, 0xFFFFFFFF);

        // Optionsliste - JourneyMap
        boolean hoverJourney = mouseX >= ox + 10 && mouseX <= ox + 230 &&
                mouseY >= oy + 28 && mouseY <= oy + 46;
        boolean hoverSound = mouseX >= ox + 10 && mouseX <= ox + 230 &&
                mouseY >= oy + 50 && mouseY <= oy + 68;
// JourneyMap
        g.fill(ox + 10, oy + 28, ox + 230, oy + 46,
                hoverJourney ? 0x33FFFFFF : 0x22FFFFFF);
        g.renderOutline(ox + 10, oy + 28, 220, 18, 0xFFFF0000);
// Sound
        g.fill(ox + 10, oy + 50, ox + 230, oy + 68,
                hoverSound ? 0x33FFFFFF : 0x22FFFFFF);

        g.renderOutline(ox + 10, oy + 50, 220, 18, 0xFFFF0000);

        g.drawString(font,
                Component.literal("§fJourneyMap Integration: ")
                        .append(GPSSettings.isJourneyMapEnabled()
                                ? Component.literal("§a").append(Component.translatable("gps.on"))
                                : Component.literal("§c").append(Component.translatable("gps.off"))),
                ox + 14, oy + 33, 0xFFFFFFFF);

        float volume = GPSSettings.getSoundVolume();

        String text = "Sound Volume: " + (int)(volume * 100) + "%";
        int textX = ox + 14;
        int textY = oy + 55;

        int textWidth = font.width(text);

// Slider beginnt NACH Text
        int sliderX = ox + 120;
        int sliderY = oy + 58;
        int sliderW = 90;

// Hintergrund-Leiste
        g.fill(sliderX, sliderY - 2, sliderX + sliderW, sliderY + 2, 0xFF555555);

// gefüllt
        int filled = (int)(sliderW * volume);
        g.fill(sliderX, sliderY - 2, sliderX + filled, sliderY + 2, 0xFF00FF00);

// Knopf
        int knobX = sliderX + filled;
        g.fill(knobX - 2, sliderY - 5, knobX + 2, sliderY + 5, 0xFFFFFFFF);

// Text
        g.drawString(font,
                Component.literal(text),
                textX, textY, 0xFFFFFFFF);

        // Schließen Button
        boolean hoverClose = mouseX >= ox + 70 && mouseX <= ox + 170 &&
                mouseY >= oy + 78 && mouseY <= oy + 98;

        int closeColor = hoverClose ? 0xFF5A1A1A : 0xFF442222;
        int closeBorder = hoverClose ? 0xFFEF5350 : 0xFFAA4444;

        g.fill(ox + 70, oy + 78, ox + 170, oy + 98, closeColor);
        g.renderOutline(ox + 70, oy + 78, 100, 20, closeBorder);
        g.drawCenteredString(font,
                Component.literal("§c✖ ")
                        .append(Component.translatable("gps.close")),
                ox + 120, oy + 84, 0xFFFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showNewRouteOverlay) {
            if (keyCode == 259 && newRouteFieldFocused) {
                if (!newRouteName.isEmpty()) {
                    newRouteName = newRouteName.substring(0, newRouteName.length() - 1);
                }
                return true;
            }
            if ((keyCode == 257 || keyCode == 335) && newRouteFieldFocused) {
                if (!newRouteName.isEmpty()) {
                    GPSRoute newRoute = GPSRoutes.addRoute(newRouteName);
                    if (routeContextEntry != null) {
                        GPSRoutes.addWaypointToRoute(newRoute, routeContextEntry);
                        routeContextEntry = null;
                    }
                    newRouteName = "";
                    newRouteFieldFocused = false;
                    showNewRouteOverlay = false;
                }
                return true;
            }
            if (keyCode == 256) {
                showNewRouteOverlay = false;
                newRouteName = "";
                newRouteFieldFocused = false;
                return true;
            }
            return true;
        }

        if (showNewPortalOverlay) {
            if (keyCode == 259) { // Backspace
                if (portalFromXFocused && !portalFromXText.isEmpty()) {
                    portalFromXText = portalFromXText.substring(0, portalFromXText.length() - 1);
                } else if (portalFromYFocused && !portalFromYText.isEmpty()) {
                    portalFromYText = portalFromYText.substring(0, portalFromYText.length() - 1);
                } else if (portalFromZFocused && !portalFromZText.isEmpty()) {
                    portalFromZText = portalFromZText.substring(0, portalFromZText.length() - 1);
                } else if (portalToXFocused && !portalToXText.isEmpty()) {
                    portalToXText = portalToXText.substring(0, portalToXText.length() - 1);
                } else if (portalToYFocused && !portalToYText.isEmpty()) {
                    portalToYText = portalToYText.substring(0, portalToYText.length() - 1);
                } else if (portalToZFocused && !portalToZText.isEmpty()) {
                    portalToZText = portalToZText.substring(0, portalToZText.length() - 1);
                }
                return true;
            }

            if (keyCode == 256) { // ESC
                closePortalOverlay();
                return true;
            }

            if (keyCode == 257 || keyCode == 335) { // Enter
                createPortalFromOverlay();
                return true;
            }

            return true;
        }

        if (showNewCategoryOverlay) {
            if (keyCode == 259 && newCategoryFieldFocused) {
                if (!newCategoryName.isEmpty()) {
                    newCategoryName = newCategoryName
                            .substring(0, newCategoryName.length() - 1);
                }
                return true;
            }
            if ((keyCode == 257 || keyCode == 335) && newCategoryFieldFocused) {
                if (!newCategoryName.isEmpty()) {
                    GPSCategories.addCategory(newCategoryName);
                    newCategoryName = "";
                    newCategoryFieldFocused = false;
                    showNewCategoryOverlay = false;
                }
                return true;
            }
            if (keyCode == 256) {
                showNewCategoryOverlay = false;
                newCategoryName = "";
                newCategoryFieldFocused = false;
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (showNewRouteOverlay) {
            if (newRouteFieldFocused && newRouteName.length() < 20 && c >= 32) {
                newRouteName += c;
            }
            return true;
        }
        if (showNewCategoryOverlay) {
            if (newCategoryFieldFocused && newCategoryName.length() < 20 && c >= 32) {
                newCategoryName += c;
            }
            return true;
        }
        if (showNewPortalOverlay) {
            if (c >= 32) {
                if (portalFromXFocused && portalFromXText.length() < 10 && isValidCoordChar(c, portalFromXText)) {
                    portalFromXText += c;
                } else if (portalFromYFocused && portalFromYText.length() < 10 && isValidCoordChar(c, portalFromYText)) {
                    portalFromYText += c;
                } else if (portalFromZFocused && portalFromZText.length() < 10 && isValidCoordChar(c, portalFromZText)) {
                    portalFromZText += c;
                } else if (portalToXFocused && portalToXText.length() < 10 && isValidCoordChar(c, portalToXText)) {
                    portalToXText += c;
                } else if (portalToYFocused && portalToYText.length() < 10 && isValidCoordChar(c, portalToYText)) {
                    portalToYText += c;
                } else if (portalToZFocused && portalToZText.length() < 10 && isValidCoordChar(c, portalToZText)) {
                    portalToZText += c;
                }
            }
            return true;
        }
        return super.charTyped(c, modifiers);
    }

    // ── Maus-Klick ────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - GUI_W) / 2;
        int top  = (height - GUI_H) / 2 + GUI_OFFSET_Y;
        int tableTop = top + HEADER_H + 14;

        //Optionen Block
        if (showOptionsOverlay) {
            int ox = width / 2 - 120;
            int oy = height / 2 - 50;

            // JourneyMap Toggle
            if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                    mouseY >= oy + 28 && mouseY <= oy + 46) {
                journeyMapEnabled = !journeyMapEnabled;
                GPSSettings.setJourneyMapEnabled(journeyMapEnabled);
                return true;
            }

            // Sound Slider Klick
            int textX = ox + 14;
            int textWidth = font.width(
                    "Sound Volume: " + (int)(GPSSettings.getSoundVolume() * 100) + "%"
            );

            int sliderX = ox + 120;
            int sliderW = 90;

            if (mouseX >= sliderX && mouseX <= sliderX + sliderW &&
                    mouseY >= oy + 52 && mouseY <= oy + 70) {

                float value = (float)((mouseX - sliderX) / sliderW);

                GPSSettings.setSoundVolume(value);
                draggingSound = true;

                return true;
            }

            // Schließen
            if (mouseX >= ox + 70 && mouseX <= ox + 170 &&
                    mouseY >= oy + 78 && mouseY <= oy + 98) {
                showOptionsOverlay = false;
                return true;
            }

            return true;
        }

        // Portal-Overlay-Klick
        if (showNewPortalOverlay) {
            int ox = width / 2 - 170;
            int oy = height / 2 + 40;

            // Start Dropdown
            if (mouseX >= ox + 14 && mouseX <= ox + 144 &&
                    mouseY >= oy + 42 && mouseY <= oy + 58) {
                showPortalFromDropdown = !showPortalFromDropdown;
                showPortalToDropdown = false;

                if (showPortalFromDropdown) {
                    portalFromScrollOffset = 0;
                }

                return true;
            }

            // Ziel Dropdown
            if (mouseX >= ox + 196 && mouseX <= ox + 326 &&
                    mouseY >= oy + 42 && mouseY <= oy + 58) {
                showPortalToDropdown = !showPortalToDropdown;
                showPortalFromDropdown = false;

                if (showPortalToDropdown) {
                    portalToScrollOffset = 0;
                }

                return true;
            }

            // Start Dropdown Auswahl
            if (showPortalFromDropdown) {
                int visibleCount = Math.min(MAX_PORTAL_DROPDOWN_VISIBLE, portalDimensions.size());
                int dropdownHeight = visibleCount * PORTAL_DROPDOWN_ENTRY_H;
                int dy = oy + 42 - dropdownHeight;

                for (int visibleRow = 0; visibleRow < visibleCount; visibleRow++) {
                    int index = portalFromScrollOffset + visibleRow;
                    int rowY = dy + visibleRow * PORTAL_DROPDOWN_ENTRY_H;

                    if (mouseX >= ox + 14 && mouseX <= ox + 144 &&
                            mouseY >= rowY && mouseY <= rowY + PORTAL_DROPDOWN_ENTRY_H) {
                        selectedPortalFromDim = index;
                        showPortalFromDropdown = false;
                        return true;
                    }
                }
            }

            // Ziel Dropdown Auswahl
            if (showPortalToDropdown) {
                int visibleCount = Math.min(MAX_PORTAL_DROPDOWN_VISIBLE, portalDimensions.size());
                int dropdownHeight = visibleCount * PORTAL_DROPDOWN_ENTRY_H;
                int dy = oy + 42 - dropdownHeight;

                for (int visibleRow = 0; visibleRow < visibleCount; visibleRow++) {
                    int index = portalToScrollOffset + visibleRow;
                    int rowY = dy + visibleRow * PORTAL_DROPDOWN_ENTRY_H;

                    if (mouseX >= ox + 196 && mouseX <= ox + 326 &&
                            mouseY >= rowY && mouseY <= rowY + PORTAL_DROPDOWN_ENTRY_H) {
                        selectedPortalToDim = index;
                        showPortalToDropdown = false;
                        return true;
                    }
                }
            }

            // Erst alles zurücksetzen
            portalFromXFocused = false;
            portalFromYFocused = false;
            portalFromZFocused = false;
            portalToXFocused = false;
            portalToYFocused = false;
            portalToZFocused = false;

            // Start Portal Felder
            if (mouseX >= ox + 20 && mouseX <= ox + 70 &&
                    mouseY >= oy + 92 && mouseY <= oy + 108) {
                portalFromXFocused = true;
            } else if (mouseX >= ox + 80 && mouseX <= ox + 130 &&
                    mouseY >= oy + 92 && mouseY <= oy + 108) {
                portalFromYFocused = true;
            } else if (mouseX >= ox + 140 && mouseX <= ox + 190 &&
                    mouseY >= oy + 92 && mouseY <= oy + 108) {
                portalFromZFocused = true;
            }

            // Ziel Portal Felder
            else if (mouseX >= ox + 20 && mouseX <= ox + 70 &&
                    mouseY >= oy + 130 && mouseY <= oy + 146) {
                portalToXFocused = true;
            } else if (mouseX >= ox + 80 && mouseX <= ox + 130 &&
                    mouseY >= oy + 130 && mouseY <= oy + 146) {
                portalToYFocused = true;
            } else if (mouseX >= ox + 140 && mouseX <= ox + 190 &&
                    mouseY >= oy + 130 && mouseY <= oy + 146) {
                portalToZFocused = true;
            }

            // Bestätigen
            if (mouseX >= ox + 20 && mouseX <= ox + 150 &&
                    mouseY >= oy + 160 && mouseY <= oy + 180) {
                createPortalFromOverlay();
                return true;
            }

            if (mouseX >= ox + 190 && mouseX <= ox + 320 &&
                    mouseY >= oy + 160 && mouseY <= oy + 180) {
                closePortalOverlay();
                return true;
            }

            return true;
        }

        // ── Dropdown Titel ────────────────────────────────────────────
        if (mouseX >= left + 4 && mouseX <= left + 160 &&
                mouseY >= top + 2 && mouseY <= top + 14) {
            showModeDropdown = !showModeDropdown;
            return true;
        }

        // Dropdown Auswahl
        if (showModeDropdown) {
            int dropTop = top - 38;

            // GPS Verlauf
            if (mouseX >= left + 6 && mouseX <= left + 158 &&
                    mouseY >= dropTop + 2 && mouseY <= dropTop + 14) {
                showRouteView = false;
                showPortalView = false;
                selectedRoute = null;
                showModeDropdown = false;
                return true;
            }

            // Routen Verlauf
            if (mouseX >= left + 6 && mouseX <= left + 158 &&
                    mouseY >= dropTop + 14 && mouseY <= dropTop + 26) {
                showRouteView = true;
                showPortalView = false;
                selectedRoute = null;
                routeScrollOffset = 0;
                showModeDropdown = false;
                return true;
            }

            // Portale
            if (mouseX >= left + 6 && mouseX <= left + 158 &&
                    mouseY >= dropTop + 26 && mouseY <= dropTop + 38) {
                showPortalView = true;
                showRouteView = false;
                selectedRoute = null;
                portalScrollOffset = 0;
                showModeDropdown = false;
                return true;
            }

            showModeDropdown = false;
            return true;
        }

        if (showPortalView) {
            int btnY = top + GUI_H - 28;

            // Neues Portal
            if (mouseX >= left + 4 && mouseX <= left + 150 &&
                    mouseY >= btnY && mouseY <= btnY + 14) {
                showNewPortalOverlay = true;
                showPortalFromDropdown = false;
                showPortalToDropdown = false;

                portalFromXText = "";
                portalFromYText = "";
                portalFromZText = "";

                portalToXText = "";
                portalToYText = "";
                portalToZText = "";

                portalFromXFocused = false;
                portalFromYFocused = false;
                portalFromZFocused = false;

                portalToXFocused = false;
                portalToYFocused = false;
                portalToZFocused = false;

                portalDimensions = GPSDimensionHelper.getAvailableDimensions();
                if (portalDimensions.isEmpty()) {
                    portalDimensions.add(new DimensionOption("minecraft:overworld", "Overworld"));
                    portalDimensions.add(new DimensionOption("minecraft:the_nether", "Nether"));
                    portalDimensions.add(new DimensionOption("minecraft:the_end", "End"));
                }
                return true;
            }

            // Portal löschen
            List<GPSPortalLink> portals = GPSPortals.getAll();
            for (int i = 0; i < MAX_VISIBLE && i + portalScrollOffset < portals.size(); i++) {
                GPSPortalLink portal = portals.get(i + portalScrollOffset);
                int rowBg = tableTop + 16 + i * ROW_H;

                if (mouseX >= left + GUI_W - 28 && mouseX <= left + GUI_W - 15 &&
                        mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                    GPSPortals.remove(portal);
                    return true;
                }
            }

            // Nur Klicks im Portalbereich abfangen, nicht das ganze GUI blockieren
            if (mouseX >= left && mouseX <= left + GUI_W &&
                    mouseY >= tableTop && mouseY <= top + GUI_H - FOOTER_H) {
                return true;
            }
        }

        int gearX = left + GUI_W - 14;
        int gearY = top + GUI_H - 14;

        if (mouseX >= gearX - 2 && mouseX <= gearX + 10 &&
                mouseY >= gearY - 2 && mouseY <= gearY + 10) {
            showOptionsOverlay = true;
            return true;
        }

        // Neue Route Overlay
        if (showNewRouteOverlay) {
            int ox = width / 2 - 120;
            int oy = height / 2 + 80;

            // Klick ins Eingabefeld
            if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                    mouseY >= oy + 24 && mouseY <= oy + 38) {
                newRouteFieldFocused = true;
                return true;
            } else {
                newRouteFieldFocused = false;
            }

            // Erstellen
            if (mouseX >= ox + 10 && mouseX <= ox + 110 &&
                    mouseY >= oy + 52 && mouseY <= oy + 72) {
                if (!newRouteName.isEmpty()) {
                    GPSRoute newRoute = GPSRoutes.addRoute(newRouteName);
                    if (routeContextEntry != null) {
                        GPSRoutes.addWaypointToRoute(newRoute, routeContextEntry);
                        routeContextEntry = null;
                    }
                    newRouteName = "";
                }
                newRouteFieldFocused = false;
                showNewRouteOverlay = false;
                return true;
            }

            // Abbrechen
            if (mouseX >= ox + 130 && mouseX <= ox + 230 &&
                    mouseY >= oy + 52 && mouseY <= oy + 72) {
                showNewRouteOverlay = false;
                newRouteName = "";
                newRouteFieldFocused = false;
                return true;
            }

            return true;
        }

        // ── Navigation-Overlay aktiv ──────────────────────────────────
        if (pendingEntry != null) {
            int ox = width  / 2 - 120;
            int oy = height / 2 + 80;
            // Ja
            if (mouseX >= ox + 10 && mouseX <= ox + 110 &&
                    mouseY >= oy + 62 && mouseY <= oy + 82) {
                startNavigationSmart(pendingEntry);
                pendingEntry = null;
                return true;
            }
            // Nein
            if (mouseX >= ox + 130 && mouseX <= ox + 230 &&
                    mouseY >= oy + 62 && mouseY <= oy + 82) {
                pendingEntry = null;
                return true;
            }
            return true; // Klick außerhalb → ignorieren
        }

        if (showRouteOverlay) {
            List<GPSRoute> routes = GPSRoutes.getAll();
            int ox = width / 2 - 120;
            int oy = height / 2 + 80;

            // Dropdown öffnen/schließen
            if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                    mouseY >= oy + 34 && mouseY <= oy + 48) {
                showRouteDropdown = !showRouteDropdown;
                return true;
            }

            if (showRouteDropdown) {
                int dropY = oy + 48;

                if (routes.isEmpty()) {
                    dropY += 14;
                }

                for (int i = 0; i < routes.size(); i++) {
                    if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                            mouseY >= dropY + i * 14 &&
                            mouseY <= dropY + i * 14 + 14) {
                        selectedRouteIndex = i;
                        showRouteDropdown = false;
                        return true;
                    }
                }

                int newY = dropY + routes.size() * 14;
                if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                        mouseY >= newY && mouseY <= newY + 14) {
                    showRouteDropdown = false;
                    showRouteOverlay = false;
                    showNewRouteOverlay = true;
                    newRouteName = "";
                    newRouteFieldFocused = false;
                    return true;
                }

                showRouteDropdown = false;
                return true;
            }

            // Hinzufügen
            if (mouseX >= ox + 10 && mouseX <= ox + 110 &&
                    mouseY >= oy + 72 && mouseY <= oy + 92) {
                if (!routes.isEmpty() && routeContextEntry != null) {
                    GPSRoutes.addWaypointToRoute(
                            routes.get(selectedRouteIndex), routeContextEntry);
                    showRouteOverlay = false;
                    routeContextEntry = null;
                }
                return true;
            }

            // Abbrechen
            if (mouseX >= ox + 130 && mouseX <= ox + 230 &&
                    mouseY >= oy + 72 && mouseY <= oy + 92) {
                showRouteOverlay = false;
                routeContextEntry = null;
                showRouteDropdown = false;
                return true;
            }

            return true;
        }

        // ── Lösch-Overlay aktiv ───────────────────────────────────────
        if (pendingDelete != null) {
            int ox = width  / 2 - 120;
            int oy = height / 2 + 80;
            // Ja
            if (mouseX >= ox + 10 && mouseX <= ox + 110 &&
                    mouseY >= oy + 62 && mouseY <= oy + 82) {
                GPSHistory.remove(pendingDelete);
                pendingDelete = null;
                return true;
            }
            // Nein
            if (mouseX >= ox + 130 && mouseX <= ox + 230 &&
                    mouseY >= oy + 62 && mouseY <= oy + 82) {
                pendingDelete = null;
                return true;
            }
            return true; // Klick außerhalb → ignorieren
        }

        // ── Kontextmenü Klicks ─────────────────────────────
        if (showCategoryOverlay) {
            List<GPSCategory> cats = GPSCategories.getAll();
            int ox = width  / 2 - 120;
            int oy = height / 2 + 80;

            // Dropdown öffnen/schließen
            if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                    mouseY >= oy + 34 && mouseY <= oy + 48) {
                if (!cats.isEmpty()) {
                    showCategoryDropdown = !showCategoryDropdown;
                }
                return true;
            }

            if (showCategoryDropdown) {
                int dropY = oy + 48;
                for (int i = 0; i < cats.size(); i++) {
                    if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                            mouseY >= dropY + i * 14 &&
                            mouseY <= dropY + i * 14 + 14) {
                        selectedCategoryIndex = i;
                        showCategoryDropdown = false;
                        return true;
                    }
                }
                // + Neuer Ordner geklickt
                int newY = dropY + cats.size() * 14 + (cats.isEmpty() ? 14 : 0);
                if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                        mouseY >= newY && mouseY <= newY + 14) {
                    showCategoryDropdown = false;
                    showCategoryOverlay = false;
                    showNewCategoryOverlay = true;
                    return true;
                }
                showCategoryDropdown = false;
                return true;
            }

            // Hinzufügen
            if (mouseX >= ox + 10 && mouseX <= ox + 110 &&
                    mouseY >= oy + 72 && mouseY <= oy + 92) {
                if (!cats.isEmpty() && contextEntry != null) {
                    GPSCategories.addEntryToCategory(
                            cats.get(selectedCategoryIndex), contextEntry);
                }
                showCategoryOverlay = false;
                contextEntry = null;
                return true;
            }

            // Abbrechen
            if (mouseX >= ox + 130 && mouseX <= ox + 230 &&
                    mouseY >= oy + 72 && mouseY <= oy + 92) {
                showCategoryOverlay = false;
                contextEntry = null;
                showCategoryDropdown = false;
                return true;
            }

            return true; // Klick außerhalb ignorieren
        }



// ── Routen-Ansicht Klicks ─────────────────────────────────────
        if (showRouteView) {

            if (selectedRoute == null) {
                // + Neue Route
                int btnY = top + GUI_H - 28;

                if (mouseX >= left + 4 && mouseX <= left + 150 &&
                        mouseY >= btnY && mouseY <= btnY + 14) {
                    showNewRouteOverlay = true;
                    newRouteName = "";
                    newRouteFieldFocused = false;
                    return true;
                }

                // Routen-Liste Klicks
                List<GPSRoute> routes = GPSRoutes.getAll();
                for (int i = 0; i < MAX_VISIBLE && i + routeScrollOffset < routes.size(); i++) {
                    GPSRoute route = routes.get(i + routeScrollOffset);
                    int rowBg = tableTop + 16 + i * ROW_H;

                    // Start [▶]
                    if (mouseX >= left + GUI_W - 55 && mouseX <= left + GUI_W - 35 &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                        GPSRouteManager.startRoute(route);

                        GPSHistoryEntry first = route.getWaypoints().isEmpty()
                                ? null
                                : route.getWaypoints().get(0);

                        if (first != null) {
                            pendingEntry = null;
                            startNavigationSmart(first);
                        }

                        onClose();
                        return true;
                    }

                    // Löschen [X]
                    if (mouseX >= left + GUI_W - 28 && mouseX <= left + GUI_W - 15 &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                        GPSRoutes.removeRoute(route);
                        return true;
                    }

                    // Route öffnen
                    if (mouseX >= left + 2 && mouseX <= left + GUI_W - 60 &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                        selectedRoute = route;
                        routeScrollOffset = 0;
                        return true;
                    }
                }
            } else {
                int routeBarY = tableTop - 30;

                // ◀ Zurück
                if (mouseX >= left + 4 && mouseX <= left + 100 &&
                        mouseY >= routeBarY && mouseY <= routeBarY + 12) {
                    selectedRoute = null;
                    routeScrollOffset = 0;
                    return true;
                }

                // + Aus Verlauf hinzufügen → öffnet Auswahl
                if (mouseX >= left + GUI_W - 154 && mouseX <= left + GUI_W - 4 &&
                        mouseY >= routeBarY && mouseY <= routeBarY + 12) {
                    showRouteView = false;
                    return true;
                }

                // Wegpunkte Klicks
                List<GPSHistoryEntry> waypoints = selectedRoute.getWaypoints();
                for (int i = 0; i < MAX_VISIBLE && i + routeScrollOffset < waypoints.size(); i++) {
                    GPSHistoryEntry wp = waypoints.get(i + routeScrollOffset);
                    int rowBg = tableTop + 18 + i * ROW_H;

                    // Löschen [X]
                    if (mouseX >= left + GUI_W - 28 && mouseX <= left + GUI_W - 15 &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                        GPSRoutes.removeWaypointFromRoute(selectedRoute, wp);
                        return true;
                    }
                }
            }
        }

        // Klick auf Spaltenüberschriften
        if (mouseY >= tableTop - 2 && mouseY <= tableTop + 10) {
            if (mouseX >= left + 22  && mouseX <= left + 140) {
                if (sortColumn.equals("name")) sortAsc = !sortAsc;
                else { sortColumn = "name"; sortAsc = true; }
                return true;
            }
            if (mouseX >= left + 141 && mouseX <= left + 190) {
                if (sortColumn.equals("x")) sortAsc = !sortAsc;
                else { sortColumn = "x"; sortAsc = true; }
                return true;
            }
            if (mouseX >= left + 191 && mouseX <= left + 240) {
                if (sortColumn.equals("y")) sortAsc = !sortAsc;
                else { sortColumn = "y"; sortAsc = true; }
                return true;
            }
            if (mouseX >= left + 241 && mouseX <= left + 290) {
                if (sortColumn.equals("z")) sortAsc = !sortAsc;
                else { sortColumn = "z"; sortAsc = true; }
                return true;
            }
            if (mouseX >= left + 291 && mouseX <= left + 340) {
                if (sortColumn.equals("dist")) sortAsc = !sortAsc;
                else { sortColumn = "dist"; sortAsc = true; }
                return true;
            }
            if (mouseX >= left + 341 && mouseX <= left + 430) {
                if (sortColumn.equals("date")) sortAsc = !sortAsc;
                else { sortColumn = "date"; sortAsc = true; }
                return true;
            }
        }

        // ── Kategorien-Panel Klicks ───────────────────────────────────
        int catLeft = left + GUI_W + 5;
        int catTop = top;

// Neuer Ordner Button
        if (mouseX >= catLeft + 4 && mouseX <= catLeft + FAV_W - 4 &&
                mouseY >= catTop + 20 && mouseY <= catTop + 32) {
            showNewCategoryOverlay = true;
            showCategoryOverlay = false;
            contextEntry = null;
            newCategoryName = "";
            newCategoryFieldFocused = false;
            return true;
        }

// Neuer Ordner Overlay Klicks
        if (showNewCategoryOverlay) {
            int ox = width  / 2 - 120;
            int oy = height / 2 + 80;

            // Klick ins Eingabefeld
            if (mouseX >= ox + 10 && mouseX <= ox + 230 &&
                    mouseY >= oy + 24 && mouseY <= oy + 38) {
                newCategoryFieldFocused = true;
                return true;
            } else {
                newCategoryFieldFocused = false;
            }

            // Erstellen
            if (mouseX >= ox + 10 && mouseX <= ox + 110 &&
                    mouseY >= oy + 52 && mouseY <= oy + 72) {
                if (!newCategoryName.isEmpty()) {
                    GPSCategories.addCategory(newCategoryName);
                    newCategoryName = "";
                }
                newCategoryFieldFocused = false;
                showNewCategoryOverlay = false;
                return true;
            }

            // Abbrechen
            if (mouseX >= ox + 130 && mouseX <= ox + 230 &&
                    mouseY >= oy + 52 && mouseY <= oy + 72) {
                showNewCategoryOverlay = false;
                newCategoryName = "";
                newCategoryFieldFocused = false;
                return true;
            }

            return true;
        }


        if (selectedCategory == null) {
            // Kategorien-Liste Klicks
            List<GPSCategory> catList = GPSCategories.getAll();
            for (int i = 0; i < catList.size(); i++) {
                GPSCategory cat = catList.get(i);
                int catRowBg = catTop + 38 + i * ROW_H;

                // Löschen [X]
                if (mouseX >= catLeft + FAV_W - 18 && mouseX <= catLeft + FAV_W - 4 &&
                        mouseY >= catRowBg && mouseY <= catRowBg + ROW_H - 4) {
                    GPSCategories.removeCategory(cat);
                    return true;
                }

                // Kategorie öffnen
                if (mouseX >= catLeft + 2 && mouseX <= catLeft + FAV_W - 22 &&
                        mouseY >= catRowBg && mouseY <= catRowBg + ROW_H - 4) {
                    selectedCategory = cat;
                    return true;
                }
            }
        } else {
            // Zurück Button
            if (mouseX >= catLeft + 2 && mouseX <= catLeft + FAV_W - 2 &&
                    mouseY >= catTop + 38 && mouseY <= catTop + 50) {
                selectedCategory = null;
                return true;
            }

            // Einträge Klicks
            List<GPSHistoryEntry> catEntries = selectedCategory.getEntries();
            for (int i = 0; i < catEntries.size(); i++) {
                GPSHistoryEntry entry = catEntries.get(i);
                int catRowBg = catTop + 56 + i * ROW_H;

                // Löschen [X]
                if (mouseX >= catLeft + FAV_W - 18 && mouseX <= catLeft + FAV_W - 4 &&
                        mouseY >= catRowBg && mouseY <= catRowBg + ROW_H - 4) {
                    GPSCategories.removeEntryFromCategory(selectedCategory, entry);
                    return true;
                }

                // Navigation
                if (mouseX >= catLeft + 2 && mouseX <= catLeft + FAV_W - 22 &&
                        mouseY >= catRowBg && mouseY <= catRowBg + ROW_H - 4) {
                    pendingEntry = entry;
                    return true;
                }
            }
        }

        // ── Favoriten-Panel Klicks ────────────────────────────────────
        int favLeft = left - FAV_W - 5;
        List<GPSHistoryEntry> favList = GPSFavorites.getAll();
        for (int i = 0; i < favList.size(); i++) {
            GPSHistoryEntry fav = favList.get(i);
            int favRowBg = top + 22 + i * ROW_H;

            // Löschen [X]
            if (mouseX >= favLeft + FAV_W - 28 && mouseX <= favLeft + FAV_W - 22 &&
                    mouseY >= favRowBg && mouseY <= favRowBg + ROW_H - 4) {
                GPSFavorites.remove(fav);
                return true;
            }

            // Teilen [↗]
            if (mouseX >= favLeft + FAV_W - 16 && mouseX <= favLeft + FAV_W - 4 &&
                    mouseY >= favRowBg && mouseY <= favRowBg + ROW_H - 4) {
                shareEntry(fav);
                return true;
            }

            // Zeile → Navigation-Bestätigung
            if (mouseX >= favLeft + 2 && mouseX <= favLeft + FAV_W - 30 &&
                    mouseY >= favRowBg && mouseY <= favRowBg + ROW_H - 4) {
                pendingEntry = fav;
                return true;
            }
        }
        // Routenansicht soll keine normalen History-Klicks auslösen
        if (showRouteView) {
            if (mouseX >= left &&
                    mouseX <= left + GUI_W &&
                    mouseY >= tableTop &&
                    mouseY <= top + GUI_H - FOOTER_H) {
                return true;
            }
        }

        // ── Zeilen-Klicks ─────────────────────────────────────────────
        List<GPSHistoryEntry> list = getSorted(GPSHistory.getAll());
        for (int i = 0; i < MAX_VISIBLE && i + scrollOffset < list.size(); i++) {
            GPSHistoryEntry e = list.get(i + scrollOffset);
            int rowBg = tableTop + 22 + i * ROW_H;

            // Löschen [X]
            String delText   = "[X]";
            String shareText = "[↗]";
            String favText   = GPSFavorites.isFavorite(e) ? "[★]" : "[☆]";
            String routeText = "[+]";
            String jmText    = "[📍]";

            int gap = 3;
            int rightPadding = 6;

            int jmW    = journeyMapEnabled ? font.width(jmText) : 0;
            int routeW = font.width(routeText);
            int favW   = font.width(favText);
            int shareW = font.width(shareText);
            int delW   = font.width(delText);

            int rightX = left + GUI_W - rightPadding;

            int btnJmX    = journeyMapEnabled ? rightX - jmW :rightX;
            int btnRouteX = (journeyMapEnabled ? btnJmX - gap : rightX) - routeW;
            int btnFavX   = btnRouteX - gap - favW;
            int btnShareX = btnFavX   - gap - shareW;
            int btnDelX   = btnShareX - gap - delW;

            // Löschen [X]
            if (mouseX >= btnDelX && mouseX <= btnDelX + delW &&
                    mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                pendingDelete = e;
                return true;
            }

            // Teilen [↗]
            if (mouseX >= btnShareX && mouseX <= btnShareX + shareW &&
                    mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                shareEntry(e);
                return true;
            }

            // Favorit [⭐]
            if (mouseX >= btnFavX && mouseX <= btnFavX + favW &&
                    mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                if (GPSFavorites.isFavorite(e)) {
                    GPSFavorites.remove(e);
                } else if (!GPSFavorites.isFull()) {
                    GPSFavorites.add(e);
                }
                return true;
            }

            // JourneyMap [📍]
            if (journeyMapEnabled &&
                    mouseX >= btnJmX && mouseX <= btnJmX + jmW &&
                    mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                openJourneyMapFor(e);
                return true;
            }

            // Route [+]
            if (mouseX >= btnRouteX && mouseX <= btnRouteX + routeW &&
                    mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                routeContextEntry = e;
                showRouteOverlay = true;
                showRouteDropdown = false;
                selectedRouteIndex = 0;
                return true;
            }

            // Rechtsklick → Kontextmenü
            if (button == 1) {
                if (mouseX >= left + 2 && mouseX <= left + 435 &&
                        mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                    contextEntry = e;
                    showCategoryOverlay = true;
                    showCategoryDropdown = false;
                    selectedCategoryIndex = 0;
                    return true;
                }
            }

            // Zeile → Navigation-Bestätigung
            if (mouseX >= left + 2 && mouseX <= left + 435 &&
                    mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4) {
                pendingEntry = e;
                return true;
            }
        }
        // Erst normale Minecraft Buttons verarbeiten
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSound = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {

        if (showOptionsOverlay && draggingSound) {

            int ox = width / 2 - 120;

            int textX = ox + 14;
            int textWidth = font.width(
                    "Sound Volume: " + (int)(GPSSettings.getSoundVolume() * 100) + "%"
            );

            int sliderX = ox + 120;
            int sliderW = 90;

            float value = (float)((mouseX - sliderX) / sliderW);

            GPSSettings.setSoundVolume(value);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showNewPortalOverlay) {
            int ox = width / 2 - 170;
            int oy = height / 2 + 40;

            // Start-Dropdown scrollen
            if (showPortalFromDropdown) {
                int visibleCount = Math.min(MAX_PORTAL_DROPDOWN_VISIBLE, portalDimensions.size());
                int dropdownHeight = visibleCount * PORTAL_DROPDOWN_ENTRY_H;
                int dy = oy + 42 - dropdownHeight;

                if (mouseX >= ox + 14 && mouseX <= ox + 148 &&
                        mouseY >= dy && mouseY <= dy + dropdownHeight) {

                    if (scrollY > 0) {
                        portalFromScrollOffset--;
                    } else if (scrollY < 0) {
                        portalFromScrollOffset++;
                    }

                    int maxOffset = Math.max(0, portalDimensions.size() - MAX_PORTAL_DROPDOWN_VISIBLE);
                    if (portalFromScrollOffset < 0) portalFromScrollOffset = 0;
                    if (portalFromScrollOffset > maxOffset) portalFromScrollOffset = maxOffset;

                    return true;
                }
            }

            // Ziel-Dropdown scrollen
            if (showPortalToDropdown) {
                int visibleCount = Math.min(MAX_PORTAL_DROPDOWN_VISIBLE, portalDimensions.size());
                int dropdownHeight = visibleCount * PORTAL_DROPDOWN_ENTRY_H;
                int dy = oy + 42 - dropdownHeight;

                if (mouseX >= ox + 196 && mouseX <= ox + 330 &&
                        mouseY >= dy && mouseY <= dy + dropdownHeight) {

                    if (scrollY > 0) {
                        portalToScrollOffset--;
                    } else if (scrollY < 0) {
                        portalToScrollOffset++;
                    }

                    int maxOffset = Math.max(0, portalDimensions.size() - MAX_PORTAL_DROPDOWN_VISIBLE);
                    if (portalToScrollOffset < 0) portalToScrollOffset = 0;
                    if (portalToScrollOffset > maxOffset) portalToScrollOffset = maxOffset;

                    return true;
                }
            }
        }

        if (showPortalView) {
            List<GPSPortalLink> portals = GPSPortals.getAll();

            if (scrollY > 0) {
                portalScrollOffset--;
            } else if (scrollY < 0) {
                portalScrollOffset++;
            }

            if (portalScrollOffset < 0) portalScrollOffset = 0;
            if (portalScrollOffset > portals.size() - MAX_VISIBLE) {
                portalScrollOffset = Math.max(0, portals.size() - MAX_VISIBLE);
            }

            return true;
        }

        if (showRouteView) {
            if (selectedRoute == null) {
                List<GPSRoute> routes = GPSRoutes.getAll();
                if (scrollY > 0) {
                    routeScrollOffset--;
                } else if (scrollY < 0) {
                    routeScrollOffset++;
                }

                if (routeScrollOffset < 0) routeScrollOffset = 0;
                if (routeScrollOffset > routes.size() - MAX_VISIBLE) {
                    routeScrollOffset = Math.max(0, routes.size() - MAX_VISIBLE);
                }
                return true;
            } else {
                List<GPSHistoryEntry> waypoints = selectedRoute.getWaypoints();

                if (scrollY > 0) {
                    routeScrollOffset--;
                } else if (scrollY < 0) {
                    routeScrollOffset++;
                }

                if (routeScrollOffset < 0) routeScrollOffset = 0;
                if (routeScrollOffset > waypoints.size() - MAX_VISIBLE) {
                    routeScrollOffset = Math.max(0, waypoints.size() - MAX_VISIBLE);
                }
            }
            return true;
        }

        List<GPSHistoryEntry> list = getSorted(GPSHistory.getAll());

        if (scrollY > 0) {
            scrollOffset--;
        } else if (scrollY < 0) {
            scrollOffset++;
        }

        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > list.size() - MAX_VISIBLE) {
            scrollOffset = Math.max(0, list.size() - MAX_VISIBLE);
        }

        return true;
    }

    // ── Aktionen ──────────────────────────────────────────────────────────
    private void onStartClicked() {
        try {
            double x = Double.parseDouble(fieldX.getValue().trim());
            double y = Double.parseDouble(fieldY.getValue().trim());
            double z = Double.parseDouble(fieldZ.getValue().trim());

            // Name optional – fallback auf "Koordinaten"
            String name = fieldName.getValue().trim();
            if (name.isEmpty()) {
                name = "Koordinaten";
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Befehl ausführen
            mc.player.connection.sendCommand(
                    "gps set \"" + name + "\" " + x + " " + y + " " + z
            );
            onClose();
        } catch (NumberFormatException e) {
            // Felder rot markieren bei ungültiger Eingabe
            fieldX.setTextColor(0xFFFF5555);
            fieldY.setTextColor(0xFFFF5555);
            fieldZ.setTextColor(0xFFFF5555);
        }
    }

    private void startNavigation(GPSHistoryEntry e) {
        startNavigationSmart(e);
    }

    private void startNavigationSmart(GPSHistoryEntry e) {
        pendingEntry = null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        String playerDim = mc.level.dimension().location().toString();
        String targetDim = e.dimension();

        // 1) Gleiche Dimension -> direkt zum Ziel
        if (playerDim.equals(targetDim)) {
            mc.player.connection.sendCommand(
                    "gps set \"" + e.name() + "\" " + e.x() + " " + e.y() + " " + e.z()
            );
            GPSClientData.set(e.name(), e.x(), e.y(), e.z(), "white", e.dimension());
            onClose();
            return;
        }

        // 2) Andere Dimension -> gespeichertes Portal suchen
        for (GPSPortalLink portal : GPSPortals.getAll()) {

            // Fall 1: Wir stehen auf fromDimension und wollen zu toDimension
            if (portal.fromDimension().equals(playerDim) &&
                    portal.toDimension().equals(targetDim)) {

                String portalName = "Portal: " + portal.name();

                //mc.player.connection.sendCommand(
                        //"gps set \"" + portalName + "\" "
                                //+ portal.fromX() + " "
                                //+ portal.fromY() + " "
                                //+ portal.fromZ()
                //);

                GPSClientData.setPortalTarget(
                        portalName,
                        portal.fromX(), portal.fromY(), portal.fromZ(),
                        "gelb",
                        e.name(),
                        e.x(), e.y(), e.z(),
                        e.dimension()
                );

                mc.player.displayClientMessage(
                        Component.literal("§e[GPS] §f")
                                .append(Component.translatable("gps.save.portal")),
                        true
                );

                onClose();
                return;
            }

            // Fall 2: Wir stehen auf toDimension und wollen zu fromDimension
            if (portal.toDimension().equals(playerDim) &&
                    portal.fromDimension().equals(targetDim)) {

                String portalName = Component.literal("")
                        .append(Component.translatable("gps.to.portal"))
                        .append(portal.name())
                        .getString();

                GPSClientData.setPortalTarget(
                        portalName,
                        portal.toX(), portal.toY(), portal.toZ(),
                        "gelb",
                        e.name(),
                        e.x(), e.y(), e.z(),
                        e.dimension()
                );

                mc.player.displayClientMessage(
                        Component.literal("§e[GPS] §f")
                                .append(Component.translatable("gps.save.portal")),
                        true
                );

                onClose();
                return;
            }
        }

        // 3) Kein Portal gefunden
        mc.player.displayClientMessage(
                Component.literal("§c[GPS] §f")
                        .append(Component.translatable("gps.no.save.portal")),
                true
        );
    }

    private void openJourneyMapFor(GPSHistoryEntry e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        String wpName = ("[GPS] " + e.name()).replace("\"", "");

        // Dimension aus dem GPS-Verlauf nehmen, nicht aus der aktuellen Spieler-Dimension
        String dimensionId = e.dimension();
        if (dimensionId == null || dimensionId.isBlank()) {
            dimensionId = "minecraft:overworld";
        }

        mc.player.connection.sendCommand(
                "jm waypoint temp create \"" + wpName + "\" "
                        + dimensionId + " "
                        + (int) e.x() + " "
                        + (int) e.y() + " "
                        + (int) e.z() + " "
                        + "red "
                        + mc.player.getName().getString() + " "
                        + "true"
        );

        mc.setScreen(null);

        mc.player.displayClientMessage(
                Component.literal("§a[SimpleGPS] §f")
                        .append(Component.translatable("gps.temp.way")),
                true
        );
    }

    private void shareEntry(GPSHistoryEntry e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.connection.sendCommand(
                "gps share \"" + e.name() + "\" " + (int)e.x()
                        + " " + (int)e.y() + " " + (int)e.z()
        );
    }


    private List<GPSHistoryEntry> getSorted(List<GPSHistoryEntry> list) {
        Minecraft mc = Minecraft.getInstance();
        List<GPSHistoryEntry> sorted = new java.util.ArrayList<>(list);
        sorted.sort((a, b) -> {
            int result = switch (sortColumn) {
                case "name" -> a.name().compareToIgnoreCase(b.name());
                case "x"    -> Double.compare(a.x(), b.x());
                case "y"    -> Double.compare(a.y(), b.y());
                case "z"    -> Double.compare(a.z(), b.z());
                case "dist" -> {
                    if (mc.player == null) yield 0;
                    double dxa = mc.player.getX() - a.x();
                    double dya = mc.player.getY() - a.y();
                    double dza = mc.player.getZ() - a.z();
                    double dxb = mc.player.getX() - b.x();
                    double dyb = mc.player.getY() - b.y();
                    double dzb = mc.player.getZ() - b.z();
                    yield Double.compare(
                            dxa*dxa + dya*dya + dza*dza,
                            dxb*dxb + dyb*dyb + dzb*dzb
                    );
                }
                case "date" -> a.date().compareTo(b.date());
                default     -> 0;
            };
            return sortAsc ? result : -result;
        });
        return sorted;
    }

    private void renderNewCategoryOverlay(GuiGraphics g, int mouseX, int mouseY) {
        int ox = width  / 2 - 120;
        int oy = height / 2 + 80;

        g.fill(0, 0, width, height, 0x88000000);
        g.fill(ox, oy, ox + 240, oy + 80, 0xFF111111);
        g.renderOutline(ox, oy, 240, 80, 0xFFFF0000);

        g.drawCenteredString(font,
                Component.literal("§4§l")
                        .append(Component.translatable("gps.new.folder")),
                width / 2, oy + 8, 0xFFFFFFFF);

        g.fill(ox + 10, oy + 24, ox + 230, oy + 38, 0xFF222222);
        g.renderOutline(ox + 10, oy + 24, 220, 14, 0xFFFF0000);

        Component categoryDisplay;

        if (!newCategoryFieldFocused && newCategoryName.isEmpty()) {
            categoryDisplay = Component.literal("§7")
                    .append(Component.translatable("gps.new.folder"))
                    .append("...");
        } else {
            String text = newCategoryName;
            if (newCategoryFieldFocused && isCursorVisible()) {
                text += "|";
            }
            categoryDisplay = Component.literal("§f" + text);
        }

        g.drawString(font, categoryDisplay, ox + 14, oy + 27, 0xFFFFFFFF);

        // Bestätigen
        boolean hoverCreate =
                mouseX >= ox + 10 && mouseX <= ox + 110 &&
                        mouseY >= oy + 52 && mouseY <= oy + 72;

        int createColor = hoverCreate ? 0xFF1B5E20 : 0xFF224422;
        int createBorder = hoverCreate ? 0xFF66BB6A : 0xFF44AA44;

        g.fill(ox + 10, oy + 52, ox + 110, oy + 72, createColor);
        g.renderOutline(ox + 10, oy + 52, 100, 20, createBorder);
        g.drawCenteredString(font,
                Component.literal("§a✔ ")
                        .append(Component.translatable("gps.create")),
                ox + 60, oy + 58, 0xFFFFFFFF);

        boolean hoverCancel =
                mouseX >= ox + 130 && mouseX <= ox + 230 &&
                        mouseY >= oy + 52 && mouseY <= oy + 72;

        int cancelColor = hoverCancel ? 0xFF5A1A1A : 0xFF442222;
        int cancelBorder = hoverCancel ? 0xFFEF5350 : 0xFFAA4444;

        g.fill(ox + 130, oy + 52, ox + 230, oy + 72, cancelColor);
        g.renderOutline(ox + 130, oy + 52, 100, 20, cancelBorder);
        g.drawCenteredString(font,
                Component.literal("§c✖ ")
                        .append(Component.translatable("gps.cancel")),
                ox + 180, oy + 58, 0xFFFFFFFF);
    }

    private void renderCategoryOverlay(GuiGraphics g, int mouseX, int mouseY) {
        if (contextEntry == null) return;

        List<GPSCategory> cats = GPSCategories.getAll();

        int ox = width  / 2 - 120;
        int oy = height / 2 + 80;

        // Abdunklung
        g.fill(0, 0, width, height, 0x88000000);

        // Panel
        g.fill(ox, oy, ox + 240, oy + 100, 0xFF111111);
        g.renderOutline(ox, oy, 240, 100, 0xFFFF0000);

        // Titel
        g.drawCenteredString(font,
                Component.literal("§f§l\"")
                        .append(shorten(contextEntry.name(), 14))
                        .append("\" ")
                        .append(Component.translatable("gps.add")),
                width / 2, oy + 8, 0xFFFFFFFF);

        g.drawString(font,
                Component.literal("§7")
                        .append(Component.translatable("gps.to.categorie")),
                ox + 10, oy + 22, 0xFFAAAAAA);

        // Dropdown-Feld
        Component selectedName = cats.isEmpty()
                ? Component.literal("§7")
                .append(Component.translatable("gps.no.categories"))
                : Component.literal("§f" + cats.get(selectedCategoryIndex).getName());

        g.fill(ox + 10, oy + 34, ox + 230, oy + 48, 0xFF222222);
        g.renderOutline(ox + 10, oy + 34, 220, 14, 0xFFFF0000);

        g.drawString(font, selectedName, ox + 14, oy + 37, 0xFFFFFFFF);

        g.drawString(font,
                Component.literal("§7▼"),
                ox + 218, oy + 37, 0xFFFFFFFF);

// Dropdown offen
        if (showCategoryDropdown) {
            int dropY = oy + 48;

            // Hinweis wenn leer
            if (cats.isEmpty()) {
                g.fill(ox + 10, dropY, ox + 230, dropY + 14, 0xFF222222);

                g.drawString(font,
                        Component.literal("§7")
                                .append(Component.translatable("gps.no.categories")),
                        ox + 14, dropY + 3, 0xFFAAAAAA);

                dropY += 14;
            }

            for (int i = 0; i < cats.size(); i++) {
                boolean hover = mouseHoverCheck(ox + 10, dropY + i * 14, 220, 14);

                g.fill(ox + 10, dropY + i * 14,
                        ox + 230, dropY + i * 14 + 14,
                        hover ? 0xFF333333 : 0xFF222222);

                g.renderOutline(ox + 10, dropY + i * 14, 220, 14, 0xFF444444);

                g.drawString(font,
                        Component.literal("§f" + cats.get(i).getName()),
                        ox + 14, dropY + i * 14 + 3, 0xFFFFFFFF);
            }

            // + Neuer Ordner Option am Ende
            int newY = dropY + cats.size() * 14;

            boolean hoverNew = mouseHoverCheck(ox + 10, newY, 220, 14);

            g.fill(ox + 10, newY, ox + 230, newY + 14,
                    hoverNew ? 0xFF334433 : 0xFF222222);

            g.renderOutline(ox + 10, newY, 220, 14, 0xFF44AA44);

            g.drawString(font,
                    Component.literal("§a+ ")
                            .append(Component.translatable("gps.new.folder")),
                    ox + 14, newY + 3, 0xFFFFFFFF);
        }

        // Buttons
        if (!showCategoryDropdown) {
            boolean hoverAdd =
                    mouseX >= ox + 10 && mouseX <= ox + 110 &&
                            mouseY >= oy + 72 && mouseY <= oy + 92;

            int addColor = hoverAdd ? 0xFF1B5E20 : 0xFF224422;
            int addBorder = hoverAdd ? 0xFF66BB6A : 0xFF44AA44;

            g.fill(ox + 10, oy + 72, ox + 110, oy + 92, addColor);
            g.renderOutline(ox + 10, oy + 72, 100, 20, addBorder);

            g.drawCenteredString(font,
                    Component.literal("§a✔ ")
                            .append(Component.translatable("gps.confirm.add")),
                    ox + 60, oy + 78, 0xFFFFFFFF);

            boolean hoverCancel =
                    mouseX >= ox + 130 && mouseX <= ox + 230 &&
                            mouseY >= oy + 72 && mouseY <= oy + 92;

            int cancelColor = hoverCancel ? 0xFF5A1A1A : 0xFF442222;
            int cancelBorder = hoverCancel ? 0xFFEF5350 : 0xFFAA4444;

            g.fill(ox + 130, oy + 72, ox + 230, oy + 92, cancelColor);
            g.renderOutline(ox + 130, oy + 72, 100, 20, cancelBorder);

            g.drawCenteredString(font,
                    Component.literal("§c✖ ")
                            .append(Component.translatable("gps.cancel")),
                    ox + 180, oy + 78, 0xFFFFFFFF);
        }
    }

    private void renderRouteOverlay(GuiGraphics g, int mouseX, int mouseY) {
        if (routeContextEntry == null) return;

        List<GPSRoute> routes = GPSRoutes.getAll();

        int ox = width / 2 - 120;
        int oy = height / 2 + 80;

        // Abdunklung
        g.fill(0, 0, width, height, 0x88000000);

        // Panel
        g.fill(ox, oy, ox + 240, oy + 100, 0xFF111111);
        g.renderOutline(ox, oy, 240, 100, 0xFFFF0000);

        // Titel
        g.drawCenteredString(font,
                Component.literal("§f§l\"")
                        .append(Component.literal(shorten(routeContextEntry.name(), 14)))
                        .append(Component.literal("\" "))
                        .append(Component.translatable("gps.add")),
                width / 2, oy + 8, 0xFFFFFFFF);

        g.drawString(font,
                Component.literal("§7")
                        .append(Component.translatable("gps.to.route")),
                ox + 10, oy + 22, 0xFFAAAAAA);

// Dropdown-Feld
        Component selectedName = routes.isEmpty()
                ? Component.literal("§7")
                .append(Component.translatable("gps.no.route"))
                : Component.literal("§f" + routes.get(selectedRouteIndex).getName());

        g.fill(ox + 10, oy + 34, ox + 230, oy + 48, 0xFF222222);
        g.renderOutline(ox + 10, oy + 34, 220, 14, 0xFFFF0000);

        g.drawString(font, selectedName, ox + 14, oy + 37, 0xFFFFFFFF);

        g.drawString(font,
                Component.literal("§7▼"),
                ox + 218, oy + 37, 0xFFFFFFFF);

// Dropdown offen
        if (showRouteDropdown) {
            int dropY = oy + 48;

            if (routes.isEmpty()) {
                g.fill(ox + 10, dropY, ox + 230, dropY + 14, 0xFF222222);

                g.drawString(font,
                        Component.literal("§7")
                                .append(Component.translatable("gps.no.route")),
                        ox + 14, dropY + 3, 0xFFAAAAAA);

                dropY += 14;
            }

            for (int i = 0; i < routes.size(); i++) {
                boolean hover = mouseHoverCheck(ox + 10, dropY + i * 14, 220, 14);

                g.fill(ox + 10, dropY + i * 14,
                        ox + 230, dropY + i * 14 + 14,
                        hover ? 0xFF333333 : 0xFF222222);

                g.renderOutline(ox + 10, dropY + i * 14, 220, 14, 0xFF444444);

                g.drawString(font,
                        Component.literal("§f" + routes.get(i).getName()),
                        ox + 14, dropY + i * 14 + 3, 0xFFFFFFFF);
            }

            int newY = dropY + routes.size() * 14;

            boolean hoverNew = mouseHoverCheck(ox + 10, newY, 220, 14);

            g.fill(ox + 10, newY, ox + 230, newY + 14,
                    hoverNew ? 0xFF334433 : 0xFF222222);

            g.renderOutline(ox + 10, newY, 220, 14, 0xFF44AA44);

            g.drawString(font,
                    Component.literal("§a+ ")
                            .append(Component.translatable("gps.new.route")),
                    ox + 14, newY + 3, 0xFFFFFFFF);
        }

        // Buttons
        if (!showRouteDropdown) {
            boolean hoverAdd =
                    mouseX >= ox + 10 && mouseX <= ox + 110 &&
                            mouseY >= oy + 72 && mouseY <= oy + 92;

            int addColor = hoverAdd ? 0xFF1B5E20 : 0xFF224422;
            int addBorder = hoverAdd ? 0xFF66BB6A : 0xFF44AA44;

            g.fill(ox + 10, oy + 72, ox + 110, oy + 92, addColor);
            g.renderOutline(ox + 10, oy + 72, 100, 20, addBorder);

            g.drawCenteredString(font,
                    Component.literal("§a✔ ")
                            .append(Component.translatable("gps.add")),
                    ox + 60, oy + 78, 0xFFFFFFFF);

            boolean hoverCancel =
                    mouseX >= ox + 130 && mouseX <= ox + 230 &&
                            mouseY >= oy + 72 && mouseY <= oy + 92;

            int cancelColor = hoverCancel ? 0xFF5A1A1A : 0xFF442222;
            int cancelBorder = hoverCancel ? 0xFFEF5350 : 0xFFAA4444;

            g.fill(ox + 130, oy + 72, ox + 230, oy + 92, cancelColor);
            g.renderOutline(ox + 130, oy + 72, 100, 20, cancelBorder);

            g.drawCenteredString(font,
                    Component.literal("§c✖ ")
                            .append(Component.translatable("gps.cancel")),
                    ox + 180, oy + 78, 0xFFFFFFFF);
        }
    }

    private boolean mouseHoverCheck(int x, int y, int w, int h) {
        double mx = Minecraft.getInstance().mouseHandler.xpos() * width / Minecraft.getInstance().getWindow().getScreenWidth();
        double my = Minecraft.getInstance().mouseHandler.ypos() * height / Minecraft.getInstance().getWindow().getScreenHeight();

        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void renderRouteView(GuiGraphics g, int mouseX, int mouseY,
                                 int left, int top, int tableTop, Minecraft mc) {

        List<GPSRoute> routes = GPSRoutes.getAll();

        if (selectedRoute == null) {

            // ── Routen-Liste ──────────────────────────────────────

            int btnY = top + GUI_H - 28;

            boolean hoverNew = mouseX >= left + 4 && mouseX <= left + 150 &&
                    mouseY >= btnY && mouseY <= btnY + 14;

            g.fill(left + 4, btnY, left + 150, btnY + 14,
                    hoverNew ? 0x44FFFFFF : 0x22FFFFFF);

            g.drawString(font,
                    Component.literal("§a+ ")
                            .append(Component.translatable("gps.new.route")),
                    left + 8, btnY + 3, 0xFFFFFFFF);

            if (routes.isEmpty()) {
                g.drawString(font,
                        Component.literal("§7")
                                .append(Component.translatable("gps.no.route.find")),
                        left + 8, tableTop + 20, 0xFFAAAAAA);
            } else {
                for (int i = 0; i < MAX_VISIBLE && i + routeScrollOffset < routes.size(); i++) {

                    GPSRoute route = routes.get(i + routeScrollOffset);
                    int rowBg = tableTop + 16 + i * ROW_H;
                    int rowY  = rowBg + (ROW_H - 9) / 2;

                    if (i % 2 == 0) {
                        g.fill(left + 2, rowBg,
                                left + GUI_W - 2, rowBg + ROW_H - 4, 0x22FFFFFF);
                    }

                    boolean hover = mouseX >= left + 2 && mouseX <= left + GUI_W - 60 &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;

                    if (hover) {
                        g.fill(left + 2, rowBg,
                                left + GUI_W - 60, rowBg + ROW_H - 4, 0x33FFFFFF);
                    }

                    boolean isActive = GPSRouteManager.isActive() &&
                            GPSRouteManager.getActiveRoute() == route;

                    g.drawString(font,
                            (isActive ? "§a▶ " : "§e📍 ") + shorten(route.getName(), 20),
                            left + 8, rowY, 0xFFFFFFFF);

                    g.drawString(font,
                            Component.literal("§7")
                                    .append(Component.literal(route.size() + " "))
                                    .append(Component.translatable("gps.points")),
                            left + 300, rowY, 0xFFAAAAAA);

                    boolean hoverStart = mouseX >= left + GUI_W - 55 &&
                            mouseX <= left + GUI_W - 35 &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;

                    g.drawString(font,
                            hoverStart ? Component.literal("§a[▶]") : Component.literal("§7[▶]"),
                            left + GUI_W - 55, rowY, 0xFFFFFFFF);

                    boolean hoverDel = mouseX >= left + GUI_W - 28 &&
                            mouseX <= left + GUI_W - 15 &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;

                    g.drawString(font,
                            hoverDel ? Component.literal("§c[X]") : Component.literal("§7[X]"),
                            left + GUI_W - 28, rowY, 0xFFFFFFFF);
                }
            }

        } else {

            // ── Einzelne Route anzeigen ───────────────────────────

            int routeBarY = tableTop - 30;

            boolean hoverBack = mouseX >= left + 4 && mouseX <= left + 100 &&
                    mouseY >= routeBarY && mouseY <= routeBarY + 12;

            g.fill(left + 4, routeBarY, left + 100, routeBarY + 12,
                    hoverBack ? 0x44FFFFFF : 0x22FFFFFF);

            g.drawString(font,
                    Component.literal("§f◀ ")
                            .append(Component.literal(shorten(selectedRoute.getName(), 12))),
                    left + 8, routeBarY + 2, 0xFFFFFFFF);

            boolean hoverAdd = mouseX >= left + GUI_W - 100 &&
                    mouseX <= left + GUI_W - 4 &&
                    mouseY >= routeBarY && mouseY <= routeBarY + 12;

            g.fill(left + GUI_W - 100, routeBarY,
                    left + GUI_W - 4, routeBarY + 12,
                    hoverAdd ? 0x44FFFFFF : 0x22FFFFFF);

            g.drawString(font,
                    Component.literal("§a+ ")
                            .append(Component.translatable("gps.add.history")),
                    left + GUI_W - 150, routeBarY + 2, 0xFFFFFFFF);

            g.fill(left, routeBarY + 14, left + GUI_W, routeBarY + 15, 0x44FFFFFF);

            List<GPSHistoryEntry> waypoints = selectedRoute.getWaypoints();

            if (waypoints.isEmpty()) {
                g.drawString(font,
                        Component.literal("§7")
                                .append(Component.translatable("gps.no.way")),
                        left + 8, tableTop + 22, 0xFFAAAAAA);

            } else {

                for (int i = 0; i < MAX_VISIBLE && i + routeScrollOffset < waypoints.size(); i++) {

                    GPSHistoryEntry wp = waypoints.get(i + routeScrollOffset);
                    int rowBg = tableTop + 18 + i * ROW_H;
                    int rowY  = rowBg + (ROW_H - 9) / 2;

                    boolean isCurrent = GPSRouteManager.isActive() &&
                            GPSRouteManager.getCurrentIndex() == i + routeScrollOffset;

                    if (i % 2 == 0) {
                        g.fill(left + 2, rowBg,
                                left + GUI_W - 2, rowBg + ROW_H - 4, 0x22FFFFFF);
                    }

                    if (isCurrent) {
                        g.fill(left + 2, rowBg,
                                left + GUI_W - 2, rowBg + ROW_H - 4, 0x33FF8800);
                    }

                    g.drawString(font,
                            Component.literal("§7" + (i + 1) + "."),
                            left + 8, rowY, 0xFFAAAAAA);

                    g.drawString(font,
                            (isCurrent ? "§a" : "§f") + shorten(wp.name(), 16),
                            left + 24, rowY, 0xFFFFFFFF);

                    g.drawString(font,
                            Component.literal("§f" + (int) wp.x()),
                            left + 150, rowY, 0xFFFFFFFF);

                    g.drawString(font,
                            Component.literal("§f" + (int) wp.y()),
                            left + 200, rowY, 0xFFFFFFFF);

                    g.drawString(font,
                            Component.literal("§f" + (int) wp.z()),
                            left + 250, rowY, 0xFFFFFFFF);

                    boolean hoverDel = mouseX >= left + GUI_W - 28 &&
                            mouseX <= left + GUI_W - 15 &&
                            mouseY >= rowBg && mouseY <= rowBg + ROW_H - 4;

                    g.drawString(font,
                            hoverDel ? Component.literal("§c[X]") : Component.literal("§7[X]"),
                            left + GUI_W - 28, rowY, 0xFFFFFFFF);
                }
            }
        }
    }

    // ── Hilfsmethoden ─────────────────────────────────────────────────────

    private boolean isCursorVisible() {
        return (System.currentTimeMillis() / 500) % 2 == 0;
    }

    private boolean isValidCoordChar(char c, String current) {
        if (Character.isDigit(c)) return true;
        return c == '-' && current.isEmpty();
    }

    private void closePortalOverlay() {
        showNewPortalOverlay = false;
        showPortalFromDropdown = false;
        showPortalToDropdown = false;

        portalFromXText = "";
        portalFromYText = "";
        portalFromZText = "";

        portalToXText = "";
        portalToYText = "";
        portalToZText = "";

        portalFromXFocused = false;
        portalFromYFocused = false;
        portalFromZFocused = false;

        portalToXFocused = false;
        portalToYFocused = false;
        portalToZFocused = false;
    }

    private void createPortalFromOverlay() {
        try {
            int fromX = Integer.parseInt(portalFromXText.trim());
            int fromY = Integer.parseInt(portalFromYText.trim());
            int fromZ = Integer.parseInt(portalFromZText.trim());

            int toX = Integer.parseInt(portalToXText.trim());
            int toY = Integer.parseInt(portalToYText.trim());
            int toZ = Integer.parseInt(portalToZText.trim());

            String fromDim = portalDimensions.get(selectedPortalFromDim).id();
            String toDim = portalDimensions.get(selectedPortalToDim).id();

            String portalName = GPSDimensionHelper.toDisplayName(fromDim)
                    + " <-> "
                    + GPSDimensionHelper.toDisplayName(toDim);

            GPSPortals.add(new GPSPortalLink(
                    portalName,
                    fromDim, fromX, fromY, fromZ,
                    toDim, toX, toY, toZ
            ));

            closePortalOverlay();
        } catch (NumberFormatException ignored) {
        }
    }

    private String formatDimension(String dim) {
        return switch (dim) {
            case "minecraft:overworld" -> "Overworld";
            case "minecraft:the_nether" -> "Nether";
            case "minecraft:the_end" -> "End";
            default -> dim;
        };
    }

    private String shorten(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Spiel läuft weiter während GUI offen
    }
}