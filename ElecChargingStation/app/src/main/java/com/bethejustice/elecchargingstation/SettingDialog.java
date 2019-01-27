package com.bethejustice.elecchargingstation;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class SettingDialog extends Dialog {

    Button saveButton;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    EditText nickName;
    EditText safeDist;
    Spinner chargingType;

    public SettingDialog(@NonNull Context context) {
        super(context);
    }

    public SettingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setting);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        nickName = findViewById(R.id.edit_nickname);
        safeDist = findViewById(R.id.edit_safe);
        chargingType = findViewById(R.id.spinner_type);

        preferences = getContext().getSharedPreferences("firstVisit", MODE_PRIVATE);
        editor = preferences.edit();

        saveButton = findViewById(R.id.btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkNull()) {
                    if (preferences.contains("first")) {
                        editor.remove("first");
                        editor.putBoolean("first", true);
                    } else {
                        editor.putBoolean("first", false);
                    }
                    editor.commit();

                    SharedPreferences dataPref = getContext().getSharedPreferences("data", MODE_PRIVATE);
                    SharedPreferences.Editor dataEditor = dataPref.edit();

                    dataEditor.putString("nickname", nickName.getText().toString());
                    dataEditor.putInt("type", chargingType.getSelectedItemPosition());
                    dataEditor.putInt("safe", Integer.parseInt(safeDist.getText().toString()));
                    dataEditor.commit();

                    Log.d("initdata", nickName.getText().toString() + chargingType.getSelectedItemPosition() + safeDist.getText().toString());

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    getContext().startActivity(intent);
                    dismiss();

                }else{
                    Toast.makeText(getContext(), "빈칸을 채우세요", Toast.LENGTH_LONG);
                }
            }
        });
    }

    public boolean checkNull(){
        if(nickName.getText()==null & safeDist.getText()==null) return false;
        return true;
    }

}
