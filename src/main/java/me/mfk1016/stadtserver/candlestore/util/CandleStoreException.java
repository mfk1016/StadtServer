package me.mfk1016.stadtserver.candlestore.util;

public class CandleStoreException extends Exception {

    private static final String MESSAGE_FULL = "The store is full";

    public static CandleStoreException storeFull() {
        return new CandleStoreException(MESSAGE_FULL);
    }

    private static final String MESSAGE_ITEM_NOT_FOUND = "The store does not contain this item";

    public static CandleStoreException itemNotFound() {
        return new CandleStoreException(MESSAGE_ITEM_NOT_FOUND);
    }

    private static final String MESSAGE_INVALID_ITEM = "Candle stores can only store stackable unenchanted items";

    public static CandleStoreException invalidItem() { return new CandleStoreException(MESSAGE_INVALID_ITEM); }

    private CandleStoreException(String message) {
        super(message);
    }
}
