# Helden & Schurken (Fabric)

**Helden:** Voltstrahl (Blitz), Eiswächter (Eis)
**Schurken:** Schattenfürst (Schatten), Infernus (Feuer)

Jeder hat: 4-teiligen Anzug (eigene Rüstungstextur), eine Waffe mit Treffer-Effekt,
3 Fähigkeiten per Taste (R / G / V, im Steuerungsmenü änderbar) und passive Anzug-Effekte + Partikel.
Fähigkeiten funktionieren nur mit KOMPLETTEM Anzug. Alles im Kreativmenü unter "Kampf".

## Einbauen
1. Vorlage für deine MC-Version holen: https://fabricmc.net/develop/template/
   (Mod-ID: heldenmod, Package: de.heldenmod, "Split client and common sources" AN)
2. Den Ordner src/ aus diesem ZIP über den src/ der Vorlage kopieren
   (Beispiel-Klassen der Vorlage vorher löschen).
3. ./gradlew build  ->  fertige Mod in build/libs/
