package pl.datequests.gui;

public enum SortType {

    NEWEST,
    OLDEST,
    COMPLETED,
    NOT_COMPLETED;

    public static SortType getNext(SortType sortType) {
        if(sortType.equals(NEWEST)) {
            return OLDEST;
        }
        if(sortType.equals(OLDEST)) {
            return COMPLETED;
        }
        if(sortType.equals(COMPLETED)) {
            return NOT_COMPLETED;
        }
        return NEWEST;
    }

    public static int getIndex(SortType sortType) {
        if(sortType.equals(NEWEST)) {
            return 0;
        }
        if(sortType.equals(OLDEST)) {
            return 1;
        }
        if(sortType.equals(COMPLETED)) {
            return 2;
        }
        return 3;
    }

}
