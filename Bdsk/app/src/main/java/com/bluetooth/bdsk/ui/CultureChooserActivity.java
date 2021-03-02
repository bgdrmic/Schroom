package com.bluetooth.bdsk.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bluetooth.bdsk.Constants;
import com.bluetooth.bdsk.R;

import java.util.ArrayList;
import hr.drmic.db.model.Culture;
import hr.drmic.db.dao.CultureDatabaseHelper;

public class CultureChooserActivity extends AppCompatActivity {

    static class ViewHolder {
        public TextView text;
    }

    private class ListAdapter extends BaseAdapter {
        private ArrayList<Culture> cultures;

        ListAdapter() {
            super();
            cultures = new ArrayList<>();
        }

        void addCulture(Culture culture) {
            if (!cultures.contains(culture)) {
                cultures.add(culture);
            }
        }

        Culture getCulture(int position) {
            return cultures.get(position);
        }

        @Override
        public int getCount() {
            return cultures.size();
        }
        @Override
        public Object getItem(int i) {
            return cultures.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = CultureChooserActivity.this.getLayoutInflater().inflate(R.layout.list_row, null);
                viewHolder = new ViewHolder();
                viewHolder.text = view.findViewById(R.id.textView);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            Culture culture = cultures.get(i);
            String cultureName = culture.getName();
            if (cultureName != null && cultureName.length() > 0) {
                viewHolder.text.setText(cultureName);
            } else {
                viewHolder.text.setText(Constants.UNKNOWN_CULTURE);
            }
            return view;
        }
    }

    private ListAdapter culture_list_adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent dataIntent = getIntent();
        boolean search = dataIntent.getBooleanExtra("search", false);

        CultureDatabaseHelper cultureDB = new CultureDatabaseHelper(this);
        int id = MainActivity.sharedPreferences.getInt(Constants.PARCEL_CURRENT_CULTURE, -1);

        if(id != -1 && !search) {
            Intent intent = new Intent(CultureChooserActivity.this, PeripheralControlActivity.class);
            MainActivity.culture = cultureDB.getCulture(id);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_culture_chooser);

        culture_list_adapter = new ListAdapter();

        ListView listView = this.findViewById(R.id.cultureList);
        listView.setAdapter(culture_list_adapter);

        for (Culture cult : cultureDB.getAllCultures()) {
            culture_list_adapter.addCulture(cult);
        }
        culture_list_adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Culture culture = culture_list_adapter.getCulture(position);
                MainActivity.culture = culture;

                SharedPreferences.Editor editor = MainActivity.sharedPreferencesEditor;
                editor.putInt(Constants.PARCEL_CURRENT_CULTURE, culture.getId());
                editor.putBoolean(Constants.PARCEL_CULTURE_CHANGE_NOTIFIED, false);
                editor.apply();

                Intent intent = new Intent(CultureChooserActivity.this, PeripheralControlActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
