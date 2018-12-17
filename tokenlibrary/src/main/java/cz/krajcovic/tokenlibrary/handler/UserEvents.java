package cz.krajcovic.tokenlibrary.handler;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

public interface UserEvents {
    // Message for customer, display it.
    void eventMessage(String s);

    // The terminal found a card number
    void setCardNumber(String s);

    // The terminal found expiration of card
    void setCardExpiration(Integer integer);

    // Remove card from reader, please
    void removeCard();

//    void notSupportedCard(PiccMifare mifare, PiccCardInfo cardInfo);

    // Card found, but not supported.
    void notSupportedCard(IsoDep isoDep);

    // Card reading failed.
    void readingFailed();
}
