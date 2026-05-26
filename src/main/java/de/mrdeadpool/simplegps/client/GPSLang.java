package de.mrdeadpool.simplegps.client;

public class GPSLang {

    public static String t(String key) {

        String lang = net.minecraft.client.Minecraft.getInstance()
                .getLanguageManager()
                .getSelected();

        boolean de = lang.startsWith("de"); // <- später automatisch ändern

        switch (key) {

            case "gps.history":
                return de ? "GPS Verlauf" : "GPS History";

            case "gps.route.history":
                return de ? "Routen Verlauf" : "Route History";

            case "gps.portal":
                return de ? "Portale" : "Portals";

            case "gps.back":
                return de ? "Zurück" : "Back";

            case "gps.delete":
                return de ? "Löschen" : "Delete";

            case "gps.start":
                return de ? "Starten" : "Start";

            case "gps.end":
                return de ? "Navigation Beenden" : "Stop Navigation";

            case "gps.close":
                return de ? "Schließen" : "Close";

            case "gps.connection":
                return de ? "Verbindung" : "Connection";

            case "gps.dist":
                return de ? "Entf." : "Dist.";

            case "gps.date":
                return de ? "Datum" : "Date";

            case "gps.fav":
                return de ? "Favoriten" : "Favorites";

            case "gps.no.fav":
                return de ? "Keine Favoriten" : "No Favorites";

            case "gps.no.route":
                return de ? "Keine Routen" : "No Routes";

            case "gps.full":
                return de ? "Voll" : "Full";

            case "gps.categories":
                return de ? "Kategorien" : "Categories";

            case "gps.new.folder":
                return de ? "Neuer Ordner" : "New Folder";

            case "gps.no.categories":
                return de ? "Keine Kategorien" : "No Categories";

            case "gps.empty":
                return de ? "Leer" : "Empty";

            case "gps.foot.history":
                return de ? "Verlauf:" : "Historie:";

            case "gps.new.route":
                return de ? "Neue Route" : "New Route";

            case "gps.create":
                return de ? "Erstellen" : "Create";

            case "gps.cancel":
                return de ? "Abbrechen" : "Cancel";

            case "gps.points":
                return de ? "Ziele" : "Destinations";

            case "gps.new.portal":
                return de ? "Neues Portal" : "New Portal";

            case "gps.no.portal":
                return de ? "Keine Portale Vorhanden" : "No Portals available.";

            case "gps.add.history":
                return de ? "Aus Verlauf hinzufügen" : "Add from History";

            case "gps.no.route.find":
                return de ? "Keine Routen Vorhanden" : "No Routes available.";

            case "gps.no.way":
                return de ? "Noch keine Wegpunkte" : "No waypoints yet";

            case "gps.from":
                return de ? "Von:" : "From:";

            case "gps.to":
                return de ? "Nach:" : "To:";

            case "gps.add":
                return de ? "hinzufügen" : "add";

            case "gps.confirm.add":
                return de ? "Hinzufügen" : "Add";

            case "gps.to.categorie":
                return de ? "zu Kategorie:" : "to Categorie:";

            case "gps.to.route":
                return de ? "zu Route:" : "to Route:";

            case "gps.beginn":
                return de ? "Start:" : "Start:";

            case "gps.desti":
                return de ? "Ziel:" : "Destination:";

            case "gps.destin":
                return de ? "Ziel Dimension" : "Target Dimension";

            case "gps.start.destin":
                return de ? "Start Dimension" : "Start Dimension";

            case "gps.start.portal":
                return de ? "Start Portal" : "Start Portal";

            case "gps.tar.portal":
                return de ? "Ziel Portal" : "Target Portal";

            case "gps.confirm":
                return de ? "Bestätigen" : "Confirm";

            case "gps.start.ask.navi":
                return de ? "Navigation Starten?" : "Start Navigation?";

            case "gps.yes":
                return de ? "Ja" : "Yes";

            case "gps.no":
                return de ? "Nein" : "No";

            case "gps.sure":
                return de ? "Bist du sicher?" : "Are you sure?";

            case "gps.option":
                return de ? "Optionen" : "Options";

            case "gps.on":
                return de ? "An" : "On";

            case "gps.off":
                return de ? "Aus" : "Off";

            case "gps.overworld":
                return de ? "Oberwelt" : "Overworld";

            case "gps.save.portal":
                return de ? "Zwischenziel gesetzt: gespeichertes Portal" : "Intermediate objective set: Saved Portal";

            case "gps.to.portal":
                return de ? "Portal: " : "Portal: ";

            case "gps.no.save.portal":
                return de ? "Kein gespeichertes Portal für diese Dimensionsreise gefunden." : "No saved portal found for this dimensional journey.";

            case "gps.temp.way":
                return de ? "Temporärer JourneyMap-Wegpunkt erstellt!" : "Temporary JourneyMap waypoint created!";

            case "gps.nav.to":
                return de ? "Navigiere zu:" : "Navigate to:";

            case "gps.nav.no":
                return de ? "Du hast kein aktives Navigationsziel!" : "You have no active navigation destination!";

            case "gps.nav.end":
                return de ? "Navigation beendet." : "Navigation ended";

            case "gps.nav.shares":
                return de ? "teilt:" : "shares";

            case "gps.nav.nav":
                return de ? "Navigieren" : "Navigate";

            case "gps.nav.click":
                return de ? "Klicken um Navigation zu starten" : "Click to start navigation";

            case "gps.nav.target":
                return de ? "Ziel" : "Target";

            case "gps.nav.was.shared":
                return de ? "wurde geteilt" : "was shared";

            case "gps.nav.dim.reached":
                return de ? "Zieldimension erreicht. Navigation wird fortgesetzt." : "Target dimension reached. Navigation continues.";

            case "gps.nav.route.end":
                return de ? "Route abgeschlossen! 🎉" : "Route completed! 🎉";

            case "gps.nav.reached":
                return de ? "Wegpunkt erreicht! Weiter..." : "Waypoint reached! Continue...";

            case "gps.nav.porta.reached":
                return de ? "Portal erreicht. Nach Dimensionswechsel geht es weiter." : "Portal reached. Proceeding after dimension shift.";

            case "gps.nav.you":
                return de ? "Du hast" : "You have reached";

            case "gps.nav.you.reach":
                return de ? "Erreicht!" : "!";
        }

        return key;
    }
}