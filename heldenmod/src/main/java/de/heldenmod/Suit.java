package de.heldenmod;

public enum Suit {
    VOLTSTRAHL("voltstrahl", "Voltstrahl", true, "Blitzklinge", 0xFFE23C,
            new String[]{"Blitzschlag", "Blitzsprint", "Stromstoß"}),
    EISWAECHTER("eiswaechter", "Eiswächter", true, "Frostschwert", 0x9FE3FF,
            new String[]{"Frostlanze", "Eisschild", "Blizzard"}),
    SCHATTENFUERST("schattenfuerst", "Schattenfürst", false, "Schattenklinge", 0x8A2BE2,
            new String[]{"Schattenschritt", "Seelenraub", "Dunkle Welle"}),
    INFERNUS("infernus", "Infernus", false, "Flammenklinge", 0xFF5A00,
            new String[]{"Feuerstrahl", "Lavasprung", "Inferno"});

    public final String id, display, weaponName;
    public final boolean hero;
    public final int color;
    public final String[] abilities;

    Suit(String id, String display, boolean hero, String weaponName, int color, String[] abilities) {
        this.id = id; this.display = display; this.hero = hero;
        this.weaponName = weaponName; this.color = color; this.abilities = abilities;
    }

    public String weaponId() { return id + "_" + weaponName.toLowerCase(); }
}
