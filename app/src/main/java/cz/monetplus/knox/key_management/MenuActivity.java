package cz.monetplus.knox.key_management;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import cz.krajcovic.knoxsupport.KnoxActivateActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btn = (Button) findViewById(R.id.btnStartToken);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, FakeTokenActivity.class);
                startActivity(intent);
            }
        });

//        btn = (Button) findViewById(R.id.btnStartTokenGui);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MenuActivity.this, FakeTokenGuiActivity.class);
//                startActivity(intent);
//            }
//        });

        btn = (Button) findViewById(R.id.btnStartTms);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, FakeTmsActivity.class);
                startActivity(intent);
            }
        });

        btn = (Button) findViewById(R.id.btnKnoxActivate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, KnoxActivateActivity.class);
                startActivity(intent);
            }
        });


        btn = (Button) findViewById(R.id.btnAesGenerate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, KeyManagementActivity.class);
                startActivity(intent);
            }
        });
    }
}
