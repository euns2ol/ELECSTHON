package com.bethejustice.elecchargingstation;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class StartDialog extends Dialog {

    static int state;

    Button searchButton;
    Button startButton;
    CheckBox checkBox;
    EditText destinationEditView;
    TextView destinationTextView;
    String destination=null;


    public StartDialog(@NonNull Context context) {
        super(context);
    }

    public StartDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_start);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        searchButton = findViewById(R.id.btn_search);
        startButton = findViewById(R.id.btn_start);
        checkBox = findViewById(R.id.radio_set1);
        destinationEditView = findViewById(R.id.textView_destination);
        destinationTextView = findViewById(R.id.textView_destinationset);

        //도로명 주소 찾기
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Address> list = null;
                checkBox.setChecked(false); // selected button cancel

                Intent intent=new Intent(getContext(),JusoListActivity.class);

                if ( destinationEditView.getText().toString().length() == 0 ) {
                    Toast.makeText(getContext(), "값을 알려주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    //공백이 아니라면 해당 키워드로 이동
                    String keyword = destinationEditView.getText().toString();
                    intent.putExtra("Keyword",String.valueOf(keyword));
                    getContext().startActivity(intent);
                }
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()){
                    destinationEditView.setText("");
                    destinationTextView.setText("");

                }
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destination = destinationEditView.getText().toString();

                /**
                 * setDestination 이 true 일때만 넘어가게 해야 한다.
                 */
                Log.d("here", "desination");
                if(!checkBox.isChecked() && isEditNull()){

                    Log.d("here", "desination!");
                    Toast.makeText(getContext(), "목적지를 설정해 주세요", Toast.LENGTH_LONG).show();

                    return;
                }

                destination = destinationEditView.getText().toString();
                if(destination.length() <=0){
                    destination = "Not";
                }
                /**
                 *
                 */

                dismiss();
            }
        });

    }

    private boolean isEditNull(){
        String s = destinationEditView.getText().toString();

        if(s.length()<=0){
            return true;
        }

        return false;
    }


}
