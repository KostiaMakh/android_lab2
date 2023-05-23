package com.example.myapplication;

import static java.nio.charset.StandardCharsets.UTF_8;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parseTasks();
        currentTask = -1;
        candidate = "";

        initRound();
        initMainButton();
        initLetters();
        initComponents();
    }

    void initRound() {
        if (++currentTask >= tasks.length())
            currentTask = 0;

        try {
            JSONObject obj = (JSONObject) tasks.get(currentTask);
            mainWord = (String) obj.get("main");

            int id = (currentTask+1)*10;
            components = new TreeMap<>();
            JSONArray group = (JSONArray) obj.get("components");
            for (int i = 0; i < group.length(); i++) {
                components.put((String) group.get(i), ++id);
            }
        } catch (JSONException e) {
            terminateApplication();
        }

        candidate = "";
    }

    void initMainButton() {
        setMainButtonText("Let's go!!!");
    }

    void initLetters() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayoutButtons);
        ll.removeAllViews();

        int id = 110;
        mainLettersIds = new ArrayList<>();
        for (char letter : mainWord.toCharArray()) {
            Button button = new Button(this);
            button.setText(String.valueOf(letter));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            button.setLayoutParams(params);
            button.setId(++id);
            button.setOnClickListener(v -> {
                Button currentButton = (Button) v;
                candidate += currentButton.getText();
                currentButton.setEnabled(false);
                setMainButtonText(candidate);
            });
            mainLettersIds.add(id);

            ll.addView(button);
        }
    }

    void initComponents() {
        TableLayout table = (TableLayout) findViewById(R.id.tableLayoutComponents);
        table.removeAllViews();

        final int columns = 2;
        int rows = components.size() / columns + (components.size() % columns != 0 ? 1 : 0);
        Iterator<Map.Entry<String, Integer>> it = components.entrySet().iterator();

        do {
            TableRow tr = new TableRow(this);
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;

            for (int i = 0; i < columns && it.hasNext(); i++) {
                NavigableMap.Entry<String, Integer> entry = it.next();
                TextView component = new TextView(this);
                component.setText("?".repeat(entry.getKey().length()));
                component.setId(entry.getValue());
                TableRow.LayoutParams textParams = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                textParams.weight = 1.0f;
                component.setTextSize(24);
                component.setLayoutParams(textParams);
                component.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                component.setPadding(10, 120, 10, 10);
                component.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
                tr.addView(component);
            }

            table.addView(tr, params);
        } while(--rows > 0);
    }

    public void mainButtonListener(View view) {
        Integer value = components.get(candidate);
        if (value != null) {
            TextView component = findViewById(value);
            component.setText(candidate);
            component.setTextColor(0xFFFFFFFF);
            component.setBackground(ContextCompat.getDrawable(this, R.drawable.border_inactive));
            components.remove(candidate);
        }

        candidate = "";
        if (components.isEmpty()) {
            initRound();
            initMainButton();
            initLetters();
            initComponents();
        } else {
            setMainButtonText(candidate);
            for (Integer id : mainLettersIds) {
                Button currentButton = findViewById(id);
                currentButton.setEnabled(true);
            }
        }
    }

    private void setMainButtonText(String text) {
        ((Button) findViewById(R.id.mainButton)).setText(text);
    }

    private String loadTasksFile() {
        String json;

        try {
            InputStream stream = getAssets().open("tasks.json");
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            stream.close();
            json = new String(buffer, UTF_8);
        } catch (IOException e) {
            terminateApplication();
            return null;
        }

        return json;
    }

    private void parseTasks() {
        try {
            JSONObject obj = new JSONObject(Objects.requireNonNull(loadTasksFile()));
            tasks = obj.getJSONArray("words");
        } catch (JSONException e) {
            terminateApplication();
        }
    }

    void terminateApplication() {
        new AlertDialog.Builder(this)
                .setTitle("Critical problem!")
                .setMessage("Application will be terminated!")
                .setCancelable(false)
                .setPositiveButton("ok", (DialogInterface dialog, int which) -> this.finishAffinity()).show();
    }

    private String mainWord;
    private ArrayList<Integer> mainLettersIds;
    private NavigableMap<String, Integer> components;
    private String candidate;
    private JSONArray tasks;
    private int currentTask;
}