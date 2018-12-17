package cz.monetplus.knox.key_management;

interface BaseTokenActivityInterface {
    void updateCardNumber(String cardNumber);
    void updateCardExpiration(Integer expiration);

    void readingCardFailed();
}
