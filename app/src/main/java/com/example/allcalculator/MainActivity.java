package com.example.allcalculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.content.Context;

import java.util.Stack;

public class MainActivity extends Activity {

    EditText display;
    TextView history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GridLayout root = new GridLayout(this);
        root.setColumnCount(4);
        root.setPadding(12, 12, 12, 12);

        history = new TextView(this);
        history.setText("");
        history.setTextSize(16);
        history.setPadding(10, 10, 10, 10);

        display = new EditText(this);
        display.setTextSize(28);
        display.setGravity(Gravity.RIGHT);
        display.setSingleLine(true);

        root.addView(history, new GridLayout.LayoutParams(
                GridLayout.spec(0, 1),
                GridLayout.spec(0, 4)
        ));

        root.addView(display, new GridLayout.LayoutParams(
                GridLayout.spec(1, 1),
                GridLayout.spec(0, 4)
        ));

        String[] buttons = {
                "C", "⌫", "(", ")",
                "7", "8", "9", "÷",
                "4", "5", "6", "×",
                "1", "2", "3", "−",
                "0", ".", "%", "+",
                "√", "x²", "π", "="
        };

        for (String text : buttons) {
            Button button = new Button(this);
            button.setText(text);
            button.setTextSize(18);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 100;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

            root.addView(button, params);

            button.setOnClickListener(v -> press(text));
        }

        setContentView(root);
    }

    private void press(String value) {

        if (value.equals("C")) {
            display.setText("");
            return;
        }

        if (value.equals("⌫")) {
            String s = display.getText().toString();
            if (s.length() > 0) {
                display.setText(s.substring(0, s.length() - 1));
            }
            return;
        }

        if (value.equals("=")) {
            calculate();
            return;
        }

        if (value.equals("√")) {
            display.append("sqrt(");
            return;
        }

        if (value.equals("x²")) {
            display.append("^2");
            return;
        }

        if (value.equals("π")) {
            display.append("pi");
            return;
        }

        display.append(value);
    }

    private void calculate() {

        try {
            String expression = display.getText().toString();

            if (expression.length() == 0)
                return;

            expression = expression
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace("−", "-");

            double result = evaluate(expression);

            history.setText(expression + " = " + result);
            display.setText(String.valueOf(result));

        } catch (Exception e) {
            display.setText("Error");
        }
    }

    private double evaluate(String expression) {

        expression = expression.replace("pi",
                String.valueOf(Math.PI));

        expression = expression.replace("sqrt",
                "sqrt");

        return new Parser(expression).parse();
    }

    private static class Parser {

        private final String input;
        private int position = -1;
        private int ch;

        Parser(String input) {
            this.input = input;
            nextChar();
        }

        void nextChar() {
            position++;
            ch = position < input.length()
                    ? input.charAt(position)
                    : -1;
        }

        boolean eat(int charToEat) {

            while (ch == ' ')
                nextChar();

            if (ch == charToEat) {
                nextChar();
                return true;
            }

            return false;
        }

        double parse() {

            double x = parseExpression();

            if (position < input.length())
                throw new RuntimeException("Unexpected character");

            return x;
        }

        double parseExpression() {

            double x = parseTerm();

            while (true) {

                if (eat('+'))
                    x += parseTerm();

                else if (eat('-'))
                    x -= parseTerm();

                else
                    return x;
            }
        }

        double parseTerm() {

            double x = parsePower();

            while (true) {

                if (eat('*'))
                    x *= parsePower();

                else if (eat('/'))
                    x /= parsePower();

                else
                    return x;
            }
        }

        double parsePower() {

            double x = parseFactor();

            if (eat('^'))
                x = Math.pow(x, parsePower());

            return x;
        }

        double parseFactor() {

            if (eat('+'))
                return parseFactor();

            if (eat('-'))
                return -parseFactor();

            double x;

            int start = position;

            if ((ch >= '0' && ch <= '9') || ch == '.') {

                while ((ch >= '0' && ch <= '9') || ch == '.')
                    nextChar();

                x = Double.parseDouble(
                        input.substring(start, position)
                );

            } else if (eat('(')) {

                x = parseExpression();

                if (!eat(')'))
                    throw new RuntimeException("Missing )");

            } else {

                throw new RuntimeException("Unexpected character");
            }

            return x;
        }
    }
    }
