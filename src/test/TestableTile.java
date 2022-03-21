package test;

import notifier.ITileStateNotifier;

public interface TestableTile {
    boolean open();
    void flag();
    void unflag();
    boolean isFlagged();
    boolean isExplosive();
    void setTileNotifier(ITileStateNotifier notifier);
}
