package com.github.spook.masks.enums;

public enum RomanNumeral {
    I(1),
    IV(4),
    V(5),
    IX(9),
    X(10),
    XL(40),
    L(50),
    XC(90),
    C(100),
    CD(400),
    D(500),
    CM(900),
    M(1000);

    private final int value;

    RomanNumeral(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static String toRoman(int number) {
        if (number < 1 || number > 3999) {
            throw new IllegalArgumentException("Number must be between 1 and 3999");
        }

        StringBuilder result = new StringBuilder();

        for (RomanNumeral romanNumeral : RomanNumeral.values()) {
            while (number >= romanNumeral.getValue()) {
                result.append(romanNumeral.name());
                number -= romanNumeral.getValue();
            }
        }

        return result.toString();
    }
}
