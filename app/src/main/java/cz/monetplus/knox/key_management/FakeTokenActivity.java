package cz.monetplus.knox.key_management;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cz.krajcovic.tokenlibrary.control.TokensControl;
import cz.krajcovic.tokenlibrary.devices.Terminal;
import cz.krajcovic.tokenlibrary.handler.MessageHandler;
import cz.krajcovic.tokenlibrary.handler.UserEvents;
import cz.krajcovic.tokenlibrary.utils.AppConfiguration;
import cz.monetplus.smartterminallibrary.devices.BeeperState;
import cz.monetplus.smartterminallibrary.devices.LedState;
import cz.monetplus.smartterminallibrary.devices.TerminalEvents;
import cz.monetplus.smartterminallibrary.devices.TextState;
import cz.monetplus.smartterminallibrary.printer.PrintedLine;
import cz.monetplus.smartterminallibrary.utils.Hex;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class FakeTokenActivity extends BaseTokenActivity {
    private static final String TAG = FakeTokenActivity.class.getName();
    private AppConfiguration config;

    private MessageHandler messageHandler;

    private Terminal terminal;
    private MediaPlayer mediaPlayer;
    private Toast toast;

    private EditText output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_token);

        try {
            tokensControl = new TokensControl(getApplicationContext());
            initTokensControl(tokensControl);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Cannot use keyUtil", e);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        config = AppConfiguration.getInstance(getApplicationContext(), APP_FILE_DIR);

        output = (EditText) findViewById(R.id.edTextOutput);

        messageHandler = new MessageHandler(new UserEvents() {
            @Override
            public void eventMessage(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        output.setText(message);
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
                        try {
                            onClickCreateToken();
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot get token", e);
                            showAToast(e.getMessage());
                        }
                    }
                });
            }

            @Override
            public void removeCard() {
                try {
                    terminal.startDetectCard(0);
                } catch (IllegalAccessException e) {
                    output.setText("Start detect card failed");
                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(FakeTokenActivity.this);
//                        builder.setMessage("Výjmi kartu!")
//                                .setCancelable(false)
//                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        try {
//                                            terminal.startDetectCard(0);
//                                        } catch (IllegalAccessException e) {
//                                            //Toast.makeText(getApplicationContext(), "Start detect card failed.", Toast.LENGTH_SHORT).show();
//                                            output.setText("Start detect card failed");
//                                        }
//                                    }
//                                });
//                        AlertDialog alert = builder.create();
//                        alert.show();
//                    }
//                });
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

            @Override
            public void notSupportedCard(final IsoDep isoDep) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(getApplicationContext(), "Not supported card", Toast.LENGTH_SHORT).show();
                        output.setText("Not supported card: " + isoDep.toString());
                    }
                });
            }

            //            @Override
//            public void notSupportedCard(final PiccMifare mifare, PiccCardInfo cardInfo) {
//                mifare.m1Auth(EM1KeyType.TYPE_A, (byte) 0, new byte[]{(byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5}, cardInfo.getSerialInfo());
//                byte[] result = mifare.m1Read((byte) 0);
//
//                if (result != null) {
//                    Log.i(TAG, "NotSupportedCard Hex: " + Hex.bcdToStr(result));
//                }
//
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        PiccCardInfo piccCardInfo = null;
//                        do {
//                            piccCardInfo = mifare.detect();
//                            if (piccCardInfo != null) {
//                                showAToast("Vyjmi kartu!");
////                                    Toast.makeText(getApplicationContext(), "Vyjmi kartu!", Toast.LENGTH_SHORT).show();
//
////                                    try {
////                                        Thread.sleep(1000);
////                                    } catch (InterruptedException e) {
////                                        e.printStackTrace();
////                                    }
//                            }
//                        } while (piccCardInfo != null);
//                    }
//                });
//
//            }

        }, config);

        EditText etCardNumber = (EditText) findViewById(R.id.etCardNumber);
        etCardNumber.setText(config.getSharedPreferences().getString(AppConfiguration.SHARED_LAST_CARD_NUMBER, "5334770600000131"));
        EditText etExp = (EditText) findViewById(R.id.etExpiration);
        etExp.setText(String.valueOf(config.getSharedPreferences().getInt(AppConfiguration.SHARED_LAST_EXPIRATION, 2211)));
        EditText etKeyId = (EditText) findViewById(R.id.etKeyId);
        etKeyId.setText(String.valueOf(config.getSharedPreferences().getInt(AppConfiguration.SHARED_LAST_KEY_ID, 13)));


//        readers = new ReadersControl();
//        readers.init(messageHandler);

//        Button btnStart = (Button) findViewById(R.id.btnStartRead);
//        btnStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                terminal.stopDetectCard();
//                messageHandler.sendMessage(Messages.TERM_START_DETECT_CARD);
//            }
//        });
//
//        Button btnStop = (Button) findViewById(R.id.btnStopRead);
//        btnStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                terminal.stopDetectCard();
//            }
//        });

        Button btCreateToken = (Button) findViewById(R.id.btCreateToken);
        btCreateToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    onClickCreateToken();
                } catch (Exception e) {
                    Log.e(TAG, "Cannot get token.",e );
                }
            }
        });

//        Button btSwCreateToken = (Button) findViewById(R.id.btnSwCreateToken);
//        btCreateToken.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                onClickCreateToken(true);
//            }
//        });

//        Button btnLoad = (Button) findViewById(R.id.btnLoad);
//        btnLoad.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                EditText etKeyId = (EditText) findViewById(R.id.etKeyId);
//                Byte keyIdTdk = Byte.valueOf(etKeyId.getText().toString());
//
//                TokensControl.loadTestSwKey(keyIdTdk);
//            }
//        });


        try {
            terminal = new Terminal(this, new TerminalEvents() {
                @Override
                public void display(final int id) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextState textState = TextState.valueOf(id);
                            //Toast.makeText(getApplicationContext(), getResources().getText(textState.getResourceId()), Toast.LENGTH_SHORT).show();
                            output.setText(getResources().getText(textState.getResourceId()));
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
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Terminal leds state: " + state.getId(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
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
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Terminal logo", Toast.LENGTH_SHORT).show();
//                        }
//                    });

                }
            }, messageHandler);

//            terminal.startReaders();

        } catch (IllegalAccessException e) {
//            e.printStackTrace();
            Log.e(TAG, "Terminal not initialized.");
        }
    }

    private void showAToast(String st) { //"Toast toast" is declared in the class
        try {
            toast.getView().isShown();     // true if visible
            toast.setText(st);
        } catch (Exception e) {         // invisible if exception
            toast = Toast.makeText(getApplicationContext(), st, Toast.LENGTH_SHORT);
        }
        toast.show();  //finally display it
    }

    private void onClickCreateToken() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        EditText etCardNumber = (EditText) findViewById(R.id.etCardNumber);
        EditText etExp = (EditText) findViewById(R.id.etExpiration);
//                EditText etSeq = (EditText)findViewById(R.id.etSequence);

        EditText etKeyId = (EditText) findViewById(R.id.etKeyId);
        Byte keyIdTdk = Byte.valueOf(etKeyId.getText().toString());

//                String token = getToken(etCardNumber.getText().toString(), Integer.valueOf(etExp.getText().toString()), null, keyIdTdk);
//        String token = TokensControl.getToken(etCardNumber.getText().toString(), Integer.valueOf(etExp.getText().toString()), null, isSw);

        Integer expiration = 0;
        String cardNumber = etCardNumber.getText().toString();
        try {
            expiration = Integer.valueOf(etExp.getText().toString());
        } catch (Exception e) {
            expiration = 9999;
        }

        String token = tokensControl.getToken(terminal, cardNumber, expiration, null);

        TextView tvToken = (TextView) findViewById(R.id.tvToken);
        tvToken.setText(token);

        saveLastCardData(etCardNumber.getText().toString(), Integer.valueOf(etExp.getText().toString()), keyIdTdk);
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

//    private String getToken(String cardNumber, Integer expiration, Character seq, Boolean isSw) {
////                pan + d + exp + padding (f)        (celkem 32)
//        byte[] binDecoded = TokensControl.prepare(cardNumber, expiration, seq);
//
//        byte[] encryptedToken = isSw ? TokensControl.encrypt(binDecoded) : TokensControl.encrypt(terminal, binDecoded);
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
//        } else {
//            Toast.makeText(getApplicationContext(), "Token not counted.", Toast.LENGTH_LONG).show();
//            return "Token not counted";
//        }
//    }

//    /**
//     * @param cardNumber
//     * @param expiration
//     * @param seq
//     * @param keyIdTdk
//     * @return
//     */
//    @Deprecated
//    private String getToken(String cardNumber, Integer expiration, Character seq, Byte keyIdTdk) {
////                pan + d + exp + padding (f)        (celkem 32)
//        byte[] binDecoded = TokensControl.prepare(cardNumber, expiration, seq);
////        byte[] encryptedToken = TokensControl.encrypt(terminal, binDecoded);
//        byte[] encryptedToken = TokensControl.encrypt(keyIdTdk, binDecoded);
//
//        // Tohle je tu pouze pro testovaci ucely
//        if (!encryptedToken.equals(TokensControl.encrypt(binDecoded))) {
//            Log.e(TAG, "Nesouhlasi vypocet SW a PED");
//        }
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
//        } else {
//            Toast.makeText(getApplicationContext(), "Token not counted.", Toast.LENGTH_LONG).show();
//            return "Token not counted";
//        }
//    }

    @Override
    public void updateCardNumber(String cardNumber) {
        EditText et = (EditText) this.findViewById(R.id.etCardNumber);
        if (et != null) {
            et.setText(cardNumber);
        }
    }

    @Override
    public void updateCardExpiration(Integer expiration) {
        EditText et = (EditText) this.findViewById(R.id.etExpiration);
        if (et != null) {
            et.setText(String.valueOf(expiration));
        }
    }

    @Override
    public void readingCardFailed() {
        TextView et = (TextView) this.findViewById(R.id.tvToken);
        if (et != null) {
            et.setText("Card reading failed.");
        }
    }
}
