package com.kdomagala.wpquizzes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class QuizActivity extends AppCompatActivity {

    TextView quizTitle;
    TextView quizQuestion;
    TextView quizResult;
    TextView quizResultPercent;
    TextView percentTextView;
    TextView goodWrongTextView;
    //ImageView quizImage;
    ProgressBar progressBar;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    String quiz;
    String answer[] = new String[4];
    int correctAnswer=0;
    int k = 0;
    JSONArray jQuestionsArray;
    int numberOfCorrectAnswers=0;
    String result="";
    int position;
    String percents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz);

        prepareQuiz();
    }

    public void prepareQuiz(){

        quizTitle = findViewById(R.id.quizTitle);
        quizQuestion = findViewById(R.id.quizQuestion);
        //quizImage = findViewById(R.id.quizImage);
        progressBar = findViewById(R.id.quizProgressBar);
        quizResult = findViewById(R.id.quizResult);
        percentTextView = findViewById(R.id.percentTextView);
        goodWrongTextView = findViewById(R.id.goodWrongTextView);
        button0 = findViewById(R.id.answer0);
        button1 = findViewById(R.id.answer1);
        button2 = findViewById(R.id.answer2);
        button3 = findViewById(R.id.answer3);

        Intent i = getIntent();
        position = i.getIntExtra("position", 0);
        String title = i.getStringExtra("title");

        quiz = readFromFile(getApplicationContext(),"quiz"+position);
        //Bitmap quizBitmap = BitmapFactory.decodeFile(getApplicationContext().getFilesDir().getPath()+"/image"+position+".jpg");

        quizTitle.setText(title);
        quizResult.setText(i.getStringExtra("id"));
        //quizImage.setImageBitmap(quizBitmap);

        createQuestion();
    }

    public void createQuestion() {

        try {
            JSONObject jsonObject = new JSONObject(quiz);
            jQuestionsArray = jsonObject.getJSONArray("questions");

            JSONObject oneObject = jQuestionsArray.getJSONObject(k);
            String question = "";

            question = oneObject.getString("text");

            quizQuestion.setText(question);

            JSONArray jsonAnswers = oneObject.getJSONArray("answers");

            for (int j = 0; j < jsonAnswers.length(); j++) {
                JSONObject answersObject = jsonAnswers.getJSONObject(j);
                answer[j] = answersObject.getString("text");

                if (answersObject.has("isCorrect"))
                    correctAnswer = j;
            }

            button0.setText(answer[0]);
            if (answer[0] == null) button0.setVisibility(View.INVISIBLE);
            button1.setText(answer[1]);
            if (answer[1] == null) button1.setVisibility(View.INVISIBLE);
            button2.setText(answer[2]);
            if (answer[2] == null) button2.setVisibility(View.INVISIBLE);
            button3.setText(answer[3]);
            if (answer[3] == null) button3.setVisibility(View.INVISIBLE);

        } catch (JSONException e) {

            Toast.makeText(getApplicationContext(), "Cannot download3", Toast.LENGTH_LONG).show();
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Błąd: quiz ma więcej niż 4 odpowiedzi", Toast.LENGTH_SHORT).show();
        }
    }

    public void answer(final View v){

        //Correct asnwer
        if (v.getTag().toString().equals(Integer.toString(correctAnswer))) {
            v.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            goodWrongTextView.setText("Dobrze!");
            numberOfCorrectAnswers++;
            button0.setEnabled(false);
            button1.setEnabled(false);
            button2.setEnabled(false);
            button3.setEnabled(false);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 0.5s = 500ms
                    v.getBackground().clearColorFilter();
                    afterClick();
                }
            }, 500);

        //Wrong answer
        } else {
            v.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            goodWrongTextView.setText("Źle");
            button0.setEnabled(false);
            button1.setEnabled(false);
            button2.setEnabled(false);
            button3.setEnabled(false);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 0.5s = 500ms
                    v.getBackground().clearColorFilter();
                    afterClick();
                }
            }, 500);
        }
    }

    public void afterClick(){
        k++;
        progressBar.setProgress(100*k/jQuestionsArray.length());
        percents = 100*k/jQuestionsArray.length() +" %";
        percentTextView.setText(percents);
        goodWrongTextView.setText("");
        button0.setEnabled(true);
        button1.setEnabled(true);
        button2.setEnabled(true);
        button3.setEnabled(true);

        quizResult.setText("Poprawne: "+numberOfCorrectAnswers +" / "+k);

        if(k<jQuestionsArray.length()) {
            createQuestion();
        } else {
            setContentView(R.layout.quiz_end_screen);
            int percents = 100*numberOfCorrectAnswers/jQuestionsArray.length();
            quizResultPercent = findViewById(R.id.quizResultPercent);
            result = numberOfCorrectAnswers +" / "+ jQuestionsArray.length()+"    "+percents+"%";
            quizResultPercent.setText(result);
        }
    }

    //Make sure that back button pressed saves the result
    @Override
    public void onBackPressed()
    {
        if(k==jQuestionsArray.length()) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("position",position);
            returnIntent.putExtra("result","Ostatni wynik: "+ result);
            setResult(RESULT_OK, returnIntent);
            finish();
        }

        super.onBackPressed();
    }

    public void backToList(View v){

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result","Ostatni wynik: "+ result);
        returnIntent.putExtra("position",position);

        setResult(RESULT_OK, returnIntent);

        finish();
    }

    public void playAgain(View v){
        k=0;
        numberOfCorrectAnswers=0;
        setContentView(R.layout.quiz);
        prepareQuiz();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(k<jQuestionsArray.length()){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result","Quiz rozwiązany w: "+ percents+"%");
            returnIntent.putExtra("position",position);

            setResult(RESULT_OK, returnIntent);
        }
    }

    private String readFromFile(Context context, String filename) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename+".txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_LONG).show();
            Log.e("Exception", "File not found: " + e.toString());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Can not read file", Toast.LENGTH_LONG).show();
            Log.e("Exception", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
