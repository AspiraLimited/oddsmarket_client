package oddsmarket_api.client.cmd;

public enum RequestCMD {
    AUTHORIZATION, SUBSCRIBE, PING, ERROR, UNKNOWN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
