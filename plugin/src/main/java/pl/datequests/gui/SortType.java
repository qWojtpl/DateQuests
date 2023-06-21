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

}
