package com.example.todoapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String PREFS = "todo_prefs";
    private static final String KEY = "todos";

    static class Todo {
        String title;
        boolean done;

        Todo(String title, boolean done) {
            this.title = title;
            this.done = done;
        }
    }

    private final List<Todo> todos = new ArrayList<>();
    private TodoAdapter adapter;
    private EditText etTask;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTask = findViewById(R.id.etTask);
        Button btnAdd = findViewById(R.id.btnAdd);
        ListView listView = findViewById(R.id.listView);
        tvEmpty = findViewById(R.id.tvEmpty);

        load();
        adapter = new TodoAdapter(this, todos);
        listView.setAdapter(adapter);
        updateEmpty();

        btnAdd.setOnClickListener(v -> addTodo());
        etTask.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTodo();
                return true;
            }
            return false;
        });
    }

    private void addTodo() {
        String text = etTask.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        todos.add(0, new Todo(text, false));
        etTask.setText("");
        changed();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etTask.getWindowToken(), 0);
        }
    }

    private void changed() {
        adapter.notifyDataSetChanged();
        updateEmpty();
        save();
    }

    private void updateEmpty() {
        tvEmpty.setVisibility(todos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void save() {
        JSONArray arr = new JSONArray();
        try {
            for (Todo t : todos) {
                JSONObject o = new JSONObject();
                o.put("title", t.title);
                o.put("done", t.done);
                arr.put(o);
            }
        } catch (JSONException ignored) {
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply();
    }

    private void load() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String json = sp.getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                todos.add(new Todo(o.getString("title"), o.getBoolean("done")));
            }
        } catch (JSONException ignored) {
        }
    }

    private class TodoAdapter extends ArrayAdapter<Todo> {

        TodoAdapter(Context ctx, List<Todo> items) {
            super(ctx, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_todo, parent, false);
            }
            final Todo todo = getItem(position);
            CheckBox cb = convertView.findViewById(R.id.cbDone);
            TextView tv = convertView.findViewById(R.id.tvTitle);
            Button del = convertView.findViewById(R.id.btnDelete);

            tv.setText(todo.title);
            applyStyle(tv, todo.done);

            cb.setOnCheckedChangeListener(null);
            cb.setChecked(todo.done);
            cb.setOnCheckedChangeListener((b, checked) -> {
                todo.done = checked;
                applyStyle(tv, checked);
                save();
            });

            del.setOnClickListener(v -> {
                todos.remove(todo);
                changed();
            });
            return convertView;
        }

        private void applyStyle(TextView tv, boolean done) {
            if (done) {
                tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tv.setAlpha(0.5f);
            } else {
                tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tv.setAlpha(1f);
            }
        }
    }
}
