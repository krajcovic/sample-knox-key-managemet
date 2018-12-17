package cz.krajcovic.tokenlibrary.handler;

public enum Messages {
    TERM_START_DETECT_CARD(1),
    TERM_SET_TRANSACTION(2),
    TERM_REMOVE_CARD(3),

    DIS_SHOW_EVENT_MESSAGE(100),
//    DIS_SHOW_TOAST_SHORT(101),

    DIS_SET_CARD_NUMBER(102),
    DIS_SET_EXPIRATION(103),

    // ---------------------------------------------------------------------------------------------
//    MAG_OPEN(200),
//    MAG_CLOSE(201),
//    MAG_READ_PREPARED(202),
//    MAG_READING(203),
//    MAG_READED(204),
//    MAG_MAX_LOOP_REACHED(205),

    // ---------------------------------------------------------------------------------------------
    // Cless cards
    PICC_OPEN(300),
    PICC_CLOSE(301),
    PICC_CARD_STRING(304),
    PICC_DETECTED(305),
    PICC_READED(306),
    PICC_FAILED(307),
    PICC_MAX_LOOP_REACHED(308),
    PICC_NOT_SUPPORTED_CARD(309),

    // ---------------------------------------------------------------------------------------------
//    IICC_OPEN(400),
//    IICC_CLOSE(401),
//    IICC_DETECTED(405),
//    IICC_READED(406),
//    IICC_FAILED(407),
//    IICC_MAX_LOOP_REACHED(408),

    ;

    int id;

    Messages(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Messages valueOf(int id) {
        for(Messages m: Messages.class.getEnumConstants()) {
            if(m.getId() == id) {
               return m;
            }
        }

        throw new IllegalArgumentException(
                "Unknown " + Messages.class.getName() + " enum code:" + String.format("%d", id));


    }

}
