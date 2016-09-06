package de.petendi.ethereum.android.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SampleListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_list);
        setButtons();
    }

    private void setButtons() {

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                Class<? extends Activity> activityClass;
                switch(id) {
                    case R.id.sample_account_balance_button:
                        activityClass = AccountBalanceActivity.class;
                        break;
                    case R.id.sample_simplestorage_button:
                        activityClass = SimpleStorageActivity.class;
                        break;
                    default:
                        throw new IllegalArgumentException("unknown button: " + id);

                }
                Intent intent = new Intent(SampleListActivity.this,activityClass);
                SampleListActivity.this.startActivity(intent);
            }
        };
        ViewGroup parent = (ViewGroup) findViewById(R.id.samplelist_layout);
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;
                button.setOnClickListener(clickListener);

            }
        }

    }
}
