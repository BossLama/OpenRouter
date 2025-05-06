package de.riemerjonas.openrouter.core.types;

public enum TurningIntroduction
{
    STRAIGHT(0),
    LEFT(1),
    RIGHT(2),
    U_TURN(3),
    SHARP_LEFT(4),
    SHARP_RIGHT(5),
    SLIGHT_LEFT(6),
    SLIGHT_RIGHT(7);

    private final int value;

    TurningIntroduction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TurningIntroduction fromValue(int value) {
        for (TurningIntroduction ti : values()) {
            if (ti.value == value) {
                return ti;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
