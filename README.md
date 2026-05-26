# SimpleGPS

![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-orange)
![Version](https://img.shields.io/badge/Version-1.0.0-blue)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)

---

## [ENG]

A lightweight navigation mod for Minecraft 1.21.1 that adds GPS-style coordinate navigation — including dimension-aware routing, history, favorites, categories, and routes.

### Features

- **HUD Arrow** — a colored directional arrow guides you to your destination
- **History** — the last 50 destinations are saved automatically, oldest entries are deleted first
- **Dimension-aware** — destinations are saved with their dimension, navigation works across dimensions if portals are saved
- **Favorites** — save important destinations permanently
- **Categories** — organize destinations into custom categories via right-click in the history window
- **Routes** — create multi-point routes to navigate through multiple destinations in order
- **Share** — share your current navigation in chat, any player can click the message to start navigation
- **Multiplayer compatible** — no dependencies required

### Commands

| Command | Description |
|---|---|
| `/gps set "name" "x" "y" "z" "color"` | Start navigation to coordinates and save to history |
| `/gps history` | Open the history window |
| `/gps share` | Share current navigation in chat |
| `/gps end` | End current navigation |

### Usage

1. Use `/gps set "name" "x" "y" "z"` to start navigation
2. A colored arrow appears on your HUD pointing towards the destination
3. Press **G** (configurable in options) or type `/gps history` to open the history
4. From the history you can:
    - Restart navigation
    - Share coordinates with other players
    - Add to favorites
    - Add to a category
    - Add to a route

### Keybinds

| Key | Action |
|---|---|
| `G` | Open history window (configurable) |

### Colors

The arrow color can be set optionally via the `/gps set` command:

`white` `red` `green` `blue` `yellow`

### Compatibility

- **Minecraft:** 1.21.1
- **ModLoader:** NeoForge
- **Dependencies:** none
- **Side:** both (Client + Server)

---

## [GER]

Eine leichtgewichtige Navigationsmodifikation für Minecraft 1.21.1 mit GPS-artiger Koordinatennavigation — inklusive Dimensionsnavigation, Verlauf, Favoriten, Kategorien und Routen.

### Features

- **HUD-Pfeil** — ein farbiger Richtungspfeil führt dich zu deinem Ziel
- **Verlauf** — die letzten 50 Ziele werden automatisch gespeichert, älteste Einträge werden zuerst gelöscht
- **Dimensionsabhängig** — Ziele werden mit ihrer Dimension gespeichert, Navigation funktioniert dimensionsübergreifend wenn Portale gespeichert sind
- **Favoriten** — wichtige Ziele dauerhaft speichern
- **Kategorien** — Ziele per Rechtsklick im Verlaufsfenster in eigene Kategorien organisieren
- **Routen** — Mehrziel-Routen erstellen um mehrere Ziele der Reihe nach abzufahren
- **Teilen** — aktuelle Navigation im Chat teilen, jeder Spieler kann auf die Nachricht klicken um die Navigation zu starten
- **Multiplayer-kompatibel** — keine Abhängigkeiten erforderlich

### Befehle

| Befehl | Beschreibung |
|---|---|
| `/gps set "Name" "x" "y" "z" "Farbe"` | Navigation zu Koordinaten starten und im Verlauf speichern |
| `/gps history` | Verlaufsfenster öffnen |
| `/gps share` | Aktuelle Navigation im Chat teilen |
| `/gps end` | Aktuelle Navigation beenden |

### Verwendung

1. Mit `/gps set "Name" "x" "y" "z"` Navigation starten
2. Ein farbiger Pfeil erscheint im HUD und zeigt in Richtung des Ziels
3. **G** drücken (in den Optionen änderbar) oder `/gps history` eingeben um den Verlauf zu öffnen
4. Aus dem Verlauf heraus kannst du:
    - Navigation neu starten
    - Koordinaten mit anderen Spielern teilen
    - Zu Favoriten hinzufügen
    - Einer Kategorie zuweisen
    - Einer Route hinzufügen

### Tastenbelegung

| Taste | Aktion |
|---|---|
| `G` | Verlaufsfenster öffnen (änderbar) |

### Farben

Die Pfeilfarbe kann optional über den `/gps set` Befehl gesetzt werden:

`white` `red` `green` `blue` `yellow`

### Kompatibilität

- **Minecraft:** 1.21.1
- **ModLoader:** NeoForge
- **Abhängigkeiten:** keine
- **Seite:** beide (Client + Server)

---

## Author

MrDeadPool