package com.bethejustice.elecchargingstation;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bethejustice.elecchargingstation.Model.Juso;

public class ChangeDestDialog extends Dialog{

    ImageView closeButton;
    Button searchButton;
    EditText destEdit;
    Button changeButton;
    String keyword;

    public ChangeDestDialog(@NonNull Context context) {
        super(context);
    }

    public ChangeDestDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_change);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        closeButton = findViewById(R.id.btn_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        changeButton = findViewById(R.id.btn_change);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();

            }
        });


        searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(),JusoListActivity.class);

                if ( destEdit.getText().toString().length() == 0 ) {
                    Toast.makeText(getContext(), "값을 알려주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    //공백이 아니라면 해당 키워드로 이동
                    String keyword = destEdit.getText().toString();
                    intent.putExtra("Keyword",String.valueOf(keyword));
                    getContext().startActivity(intent);
                }

            }
        });

        destEdit = findViewById(R.id.text_dest);

    }

}
