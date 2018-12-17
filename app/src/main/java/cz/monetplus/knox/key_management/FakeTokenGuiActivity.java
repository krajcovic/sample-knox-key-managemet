package cz.monetplus.knox.key_management;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import cz.krajcovic.tokenlibrary.control.TokensControl;
import cz.krajcovic.tokenlibrary.devices.Terminal;
import cz.krajcovic.tokenlibrary.handler.MessageHandler;
import cz.krajcovic.tokenlibrary.handler.UserEvents;
import cz.krajcovic.tokenlibrary.utils.AppConfiguration;
import cz.monetplus.smartterminallibrary.devices.*;
import cz.monetplus.smartterminallibrary.printer.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FakeTokenGuiActivity extends BaseTokenActivity {

    private static final String TAG = FakeTokenGuiActivity.class.getName();
    private AppConfiguration config;

    private MessageHandler messageHandler;

    private Terminal terminal;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_token_gui);

        config = AppConfiguration.getInstance(getApplicationContext(), APP_FILE_DIR);
        Log.i(TAG, config.loadConfiguration());

        try {
            tokensControl = new TokensControl(getApplicationContext());
            initTokensControl(tokensControl);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Cannot use keyUtil", e);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        messageHandler = new MessageHandler(new UserEvents() {
            @Override
            public void eventMessage(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void setCardNumber(final String cardNumber) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCardNumber(cardNumber);
                    }
                });
            }

            @Override
            public void setCardExpiration(final Integer cardExpiration) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCardExpiration(cardExpiration);
                    }
                });
            }

            @Override
            public void removeCard() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FakeTokenGuiActivity.this);
                        builder.setMessage("Výjmi kartu!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        try {
                                            terminal.startDetectCard(0);
                                        } catch (IllegalAccessException e) {
                                            Toast.makeText(getApplicationContext(), "Start detect card failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }

//            @Override
//            public void notSupportedCard(PiccMifare mifare, PiccCardInfo cardInfo) {
//                mifare.m1Auth(EM1KeyType.TYPE_A, (byte) 0, new byte[]{(byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5}, cardInfo.getSerialInfo());
//                byte[] result = mifare.m1Read((byte) 0);
//                Log.i(TAG, Hex.bcdToStr(result));
//            }

            @Override
            public void notSupportedCard(IsoDep isoDep) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Not supported card", Toast.LENGTH_SHORT).show();
                    }
                });
            }



            @Override
            public void readingFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readingCardFailed();
                    }
                });
            }
        }, config);

        try {
            terminal = new Terminal(this, new TerminalEvents() {
                @Override
                public void display(final int id) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextState textState = TextState.valueOf(id);
                            Toast.makeText(getApplicationContext(), getResources().getText(textState.getResourceId()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void beeper(BeeperState state) {
                    switch (state) {
                        case BEEPER_NONE:
                            break;
                        case BEEPER_SUCCESS:
                            playSound(cz.monetplus.smartterminallibrary.R.raw.terminal_beeper_success);
                            break;
                        case BEEPER_ERROR:
                            playSound(cz.monetplus.smartterminallibrary.R.raw.terminal_beeper_error);
                            break;
                    }
                }

                @Override
                public void blink(final LedState state) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Terminal leds state: " + state.getId(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void log(String TAG, String message) {
                    Log.d(TAG, message);
                }

                @Override
                public void print(PrintedLine printedLine) {
                    Log.i("PRINT", printedLine.getText());
                }

                @Override
                public void displayLogo() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Terminal logo", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }, messageHandler);

//            terminal.startReaders();

        } catch (IllegalAccessException e) {
//            e.printStackTrace();
            Log.e(TAG, "Terminal not initialized.");
        }
    }

    private void proceedTokenize() {
        String cardNumber = config.getSharedPreferences().getString(AppConfiguration.SHARED_LAST_CARD_NUMBER, null);
        Integer expiration = config.getSharedPreferences().getInt(AppConfiguration.SHARED_LAST_EXPIRATION, -1);
        Byte keyIdTdk = Integer.valueOf(config.getSharedPreferences().getInt(AppConfiguration.SHARED_LAST_KEY_ID, -1)).byteValue();

        ImageView iv = (ImageView) findViewById(R.id.ivTokenState);

        if (cardNumber == null || expiration == -1 || keyIdTdk == -1) {
            //TextView tvToken = (TextView) findViewById(R.id.tvToken);
            //tvToken.setText("Invalid values");
            iv.setImageResource(android.R.drawable.presence_invisible);
        } else {
            try {
//                String token = MediaSession.Token.getToken(cardNumber, expiration, null);
                String token = tokensControl.getToken(terminal, cardNumber, expiration, null);
                if(isOnList(token.trim())) {
                    iv.setImageResource(android.R.drawable.presence_online);
                } else {
                    iv.setImageResource(android.R.drawable.presence_busy);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                iv.setImageResource(android.R.drawable.presence_away);
            }


        }
    }

    private boolean isTokenBlocked(String token) {
        if(token != null) {
            List<String> blockedList = config.getList("BlockedTokens");
            for (String blockedHash : blockedList) {
                if (token.trim().equalsIgnoreCase(blockedHash.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isOnList(String token) {
        if(token != null) {
            List<String> list = config.getList("ActiveTokens");
            for (String item : list) {
                if (token.trim().equalsIgnoreCase(item.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    private void saveLastCardData(String etCardNumber, Integer etExp, Byte keyIdTdk) {
        SharedPreferences.Editor editor = config.getSharedPreferences().edit();
        editor.putString(AppConfiguration.SHARED_LAST_CARD_NUMBER, etCardNumber);
        editor.putInt(AppConfiguration.SHARED_LAST_EXPIRATION, etExp);

        editor.putInt(AppConfiguration.SHARED_LAST_KEY_ID, keyIdTdk);
        editor.commit();
    }

    private void playSound(int rawId) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this.getApplicationContext(), rawId);
        mediaPlayer.start();
    }

    @Override
    protected void onResume() {
        if (terminal != null) {
            try {
                terminal.startDetectCard(0);
            } catch (IllegalAccessException e) {
                Toast.makeText(getApplicationContext(), "Start detect card failed.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (terminal != null) {
            terminal.stopDetectCard();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    private String getToken(String cardNumber, Integer expiration, Character seq) throws IllegalStateException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
////                pan + d + exp + padding (f)        (celkem 32)
//        byte[] binDecoded = tokensControl.prepare(cardNumber, expiration, seq);
//        byte[] encryptedToken = tokensControl.encrypt(terminal, binDecoded);
//
////        // Tohle je tu pouze pro testovaci ucely
////        if(!encryptedToken.equals(TokensControl.encrypt(binDecoded))) {
////            Log.e(TAG, "Nesouhlasi vypocet SW a PED");
////        }
//
//        byte[] token = null;
//
//        if (encryptedToken != null) {
//            try {
//                token = TokensControl.digest(encryptedToken);
//            } catch (NoSuchAlgorithmException e) {
//                Log.e(TAG, "Cannot init SHA256", e);
//            }
//        }
//
//        if (token != null) {
//            return TokensControl.hex(token);
//
//        } else {
//            Toast.makeText(getApplicationContext(), "Token not counted.", Toast.LENGTH_LONG).show();
//
//            throw new IllegalStateException("Token not counted");
//        }
//    }

    @Override
    public void updateCardNumber(String cardNumber) {
        SharedPreferences.Editor editor = config.getSharedPreferences().edit();
        editor.putString(AppConfiguration.SHARED_LAST_CARD_NUMBER, cardNumber);
        editor.commit();
    }

    @Override
    public void updateCardExpiration(Integer expiration) {
        SharedPreferences.Editor editor = config.getSharedPreferences().edit();
        editor.putInt(AppConfiguration.SHARED_LAST_EXPIRATION, expiration);
        editor.commit();

        // TODO: asi dodelat nejake konkrenti informaci, ze cteni probehlo v poradku
        proceedTokenize();
    }

    @Override
    public void readingCardFailed() {
        ImageView iv = (ImageView) findViewById(R.id.ivTokenState);
        iv.setImageResource(android.R.drawable.presence_away);
    }

}
