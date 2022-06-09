package com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator;

/**
 * "Specification" (key) of an outcome - all parameters to identify odds within one bookmaker
 */
public class BetSpec {
    public final short marketAndBetType;
    public final float marketAndBetTypeParam;
    public final short period;
    public final boolean isLay;
    public final int playerId1;
    public final int playerId2;
    private final transient int hashcode;

    public BetSpec(short marketAndBetType, float marketAndBetTypeParam, short period, boolean isLay, int playerId1, int playerId2) {
        this.marketAndBetType = marketAndBetType;
        this.marketAndBetTypeParam = marketAndBetTypeParam;
        this.period = period;
        this.isLay = isLay;
        this.playerId1 = playerId1;
        this.playerId2 = playerId2;
        this.hashcode = hashCodeCalc();
    }

    @Override
    public String toString() {
        return marketAndBetType + "," + marketAndBetTypeParam + "," + period + "," + (isLay ? 1 : 0) + "," + playerId1 + "," + playerId2;
    }

    public static BetSpec createFromString(String str) {
        String[] arr = str.split(",");
        if (arr.length != 6) {
            throw new IllegalArgumentException("Expected 6 arguments in string: " + str);
        }
        return new BetSpec(
                Short.parseShort(arr[0]),
                Float.parseFloat(arr[1]),
                Short.parseShort(arr[2]),
                "1".equals(arr[3]),
                Integer.parseInt(arr[4]),
                Integer.parseInt(arr[5])
        );
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BetSpec)) return false;
        final BetSpec other = (BetSpec) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.marketAndBetType != other.marketAndBetType) return false;
        if (Float.compare(this.marketAndBetTypeParam, other.marketAndBetTypeParam) != 0) return false;
        if (this.period != other.period) return false;
        if (this.isLay != other.isLay) return false;
        if (this.playerId1 != other.playerId1) return false;
        if (this.playerId2 != other.playerId2) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BetSpec;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    public int hashCodeCalc() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + marketAndBetType;
        result = result * PRIME + Float.floatToIntBits(this.marketAndBetTypeParam);
        result = result * PRIME + this.period;
        result = result * PRIME + (this.isLay ? 79 : 97);
        result = result * PRIME + this.playerId1;
        result = result * PRIME + this.playerId2;
        return result;
    }
}
