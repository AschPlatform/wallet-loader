package so.asch.wallet.loader;

public interface Upgrader {
    boolean checkUpgradable();
    boolean upgrade();
}
