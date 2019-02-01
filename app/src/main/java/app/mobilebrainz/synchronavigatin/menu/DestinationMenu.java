package app.mobilebrainz.synchronavigatin.menu;

public class DestinationMenu {

    private final int destinationId;
    private final int bottomMenuId;
    private final int drawerMenuId;
    private final int optionsMenuId;

    public DestinationMenu(int destinationId, int bottomMenuId, int drawerMenuId, int optionsMenuId) {
        this.destinationId = destinationId;
        this.bottomMenuId = bottomMenuId;
        this.drawerMenuId = drawerMenuId;
        this.optionsMenuId = optionsMenuId;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public int getBottomMenuId() {
        return bottomMenuId;
    }

    public int getDrawerMenuId() {
        return drawerMenuId;
    }

    public int getOptionsMenuId() {
        return optionsMenuId;
    }
}
