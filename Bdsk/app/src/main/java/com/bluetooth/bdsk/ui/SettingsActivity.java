package com.bluetooth.bdsk.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bluetooth.bdsk.Constants;
import com.bluetooth.bdsk.R;

import java.util.ArrayList;
import java.util.Objects;

import hr.drmic.db.dao.MeasurementDatabaseHelper;

public class SettingsActivity extends AppCompatActivity {

    private SettingsListAdapter settings_list_adapter;

    private static class ViewHolder {
        TextView text;
    }

    private class SettingsListAdapter extends BaseAdapter {

        public class Setting {
            private String key;
            private String description;

            Setting(String key, String description) {
                this.key = key;
                this.description = description;
            }

            String getKey() {
                return key;
            }

            String getDescription() {
                return description;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Setting setting = (Setting) o;
                return Objects.equals(key, setting.key);
            }
        }

        private ArrayList<Setting> settings = new ArrayList<>();

        void addSetting(String key, String description) {
            Setting setting = new Setting(key, description);
            if (!settings.contains(setting)) {
                settings.add(setting);

            }
        }

        String getSetting(int position) {
            return settings.get(position).getKey();
        }

        @Override
        public int getCount() {
            return settings.size();
        }
        @Override
        public Object getItem(int i) {
            return settings.get(i).getKey();
        }
        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint({"SetTextI18n", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = SettingsActivity.this.getLayoutInflater().inflate(R.layout.list_row, null);
                viewHolder = new ViewHolder();
                viewHolder.text = view.findViewById(R.id.textView);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            Setting setting = settings.get(i);
            if (setting.getDescription() != null && setting.getDescription().length() > 0) {
                viewHolder.text.setText(setting.getDescription());
            } else {
                viewHolder.text.setText("unknown setting");
            }
            return view;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings_list_adapter = new SettingsListAdapter();

        ListView listView = this.findViewById(R.id.settingsList);
        listView.setAdapter(settings_list_adapter);

        settings_list_adapter.addSetting("BleChooserActivity", "Change Schroom device");
        settings_list_adapter.addSetting("CultureChooserActivity", "Change culture");
        settings_list_adapter.addSetting("Reset", "Reset measurements database");
        settings_list_adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key =  settings_list_adapter.getSetting(position);
                Intent intent = null;

                switch(key) {
                    case "BleChooserActivity":
                        intent = new Intent(SettingsActivity.this, BleChooserActivity.class);
                        intent.putExtra("search", true);
                        break;
                    case "CultureChooserActivity":
                        intent = new Intent(SettingsActivity.this, CultureChooserActivity.class);
                        intent.putExtra("search", true);
                        break;
                    case "Reset":
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == DialogInterface.BUTTON_POSITIVE) {
                                    (new MeasurementDatabaseHelper(SettingsActivity.this)).reset();
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
                        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        return;
                }
                startActivity(intent);
                finish();
            }
        });
    }

    public void onCheckBoxClicked(View view) {
        CheckBox checkBox = findViewById(R.id.usingLightsCheckbox);

        if (checkBox.isChecked()) {
            MainActivity.sharedPreferencesEditor.putBoolean(Constants.PARCEL_ARTIFICIAL_LIGHT_SOURCE, true);
        } else {
            MainActivity.sharedPreferencesEditor.putBoolean(Constants.PARCEL_ARTIFICIAL_LIGHT_SOURCE, false);
        }

        MainActivity.sharedPreferencesEditor.apply();
    }

}
