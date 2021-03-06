package com.kdomagala.wpquizzes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    QuizListAdapter mAdapter;
    ListView quizList;
    LinkedHashMap<String, String> results = new LinkedHashMap<>();
    ProgressDialog progressDialog;
    String[] imageUrls;
    DownloadQuiz downloadQuiz;
    DownloadQuizList downloadQuizList;
    DownloadImage downloadImage;
    ArrayList<Quiz> quizzes;
    RecyclerView recyclerView;



    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable()
                    && cm.getActiveNetworkInfo().isConnected()) {
                return true;
            } else {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.quizList);
        // w celach optymalizacji
        recyclerView.setHasFixedSize(true);

        // ustawiamy LayoutManagera
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ustawiamy animatora, który odpowiada za animację dodania/usunięcia elementów listy
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // tworzymy źródło danych - tablicę z artykułami
        ArrayList<Quiz> quizzes = new ArrayList<>();


        /*for (int i = 0; i < 20; ++i)
            quizzes.add(new Quiz());
*/
        // tworzymy adapter oraz łączymy go z RecyclerView
        //recyclerView.setAdapter(new QuizListAdapter(quizzes, recyclerView));


        /*quizList = findViewById(R.id.quizList);
        mAdapter = new QuizListAdapter(getApplicationContext());

        quizList = findViewById(R.id.quizList);
        quizList.setAdapter(mAdapter);
        registerForContextMenu(quizList);*/

        File file = new File (getApplicationContext().getFilesDir().getPath()+"/quizList.txt");
        //TODO SPRAWDZIĆ CZY DZIAŁA gdy nie ma plików i jak przerwane pobieranie
        if(file.exists() && quizzes.size()==0){
                loadQuizzes();
                Log.i("Load", "Quizzes loaded");
            Log.i("Adapter", "Adapter setted");

        }
        else {
            if(isNetworkAvailable()) {
                downloadQuizList = new DownloadQuizList();
                downloadQuizList.execute("http://quiz.o2.pl/api/v1/quizzes/0/100");
                Log.i("File", "File doesn't exists");
            }
            else {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);
                }
                builder.setTitle("Brak połączenia")
                        .setMessage("Upewnij się, że masz połączenie z internetem i spróbuj ponownie")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        }

        File resultsfile = new File (getApplicationContext().getFilesDir().getPath()+"/results");
        if(resultsfile.exists()){
            Log.e("File", "File exists");
            loadResults();
        }



        /*quizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                Quiz enteredQuiz = (Quiz) adapterView.getItemAtPosition(position);

                intent.putExtra("position", position);
                intent.putExtra("title", enteredQuiz.getTitle());
                startActivityForResult(intent, 1);
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            int position;
            position = data.getIntExtra("position", 0);
            Log.i("onactresultpos: ", String.valueOf(position));

            Quiz quiz = (Quiz) mAdapter.getItem(position);
            quiz.setResult(data.getStringExtra("result"));
            mAdapter.notifyDataSetChanged();
            //mAdapter.edit(editedItem, position);
            recyclerView.getLayoutManager().scrollToPosition(position);
            //quizList.setSelection(position);
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void loadQuizzes(){

            String quizList = readFromFile(getApplicationContext(),"quizList");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            try {
                JSONObject jsonQuizList = new JSONObject(quizList);
                JSONArray jsonQuizArray = jsonQuizList.getJSONArray("items");

                quizzes = new ArrayList<>();


        /*for (int i = 0; i < 20; ++i)
            quizzes.add(new Quiz());
*/


                for (int i = 0; i < jsonQuizArray.length(); i++) {

                    String title;
                    JSONObject quizObject = jsonQuizArray.getJSONObject(i);
                    BitmapFactory.decodeFile(getApplicationContext().getFilesDir().getPath()+"/image"+i+".jpg", options);
                    options.inSampleSize = calculateInSampleSize(options, 160, 120);
                    options.inJustDecodeBounds = false;
                    Bitmap quizImage = BitmapFactory.decodeFile(getApplicationContext().getFilesDir().getPath()+"/image"+i+".jpg");
                    title = quizObject.getString("title");

                    quizzes.add(new Quiz(title, "", quizImage));
                }
                Log.i("quizzes size", String.valueOf(quizzes.size()));
                mAdapter = new QuizListAdapter(quizzes,recyclerView);
                recyclerView.setAdapter(mAdapter);

                // tworzymy adapter oraz łączymy go z RecyclerView

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Cannot download3", Toast.LENGTH_LONG).show();
            }
    }

    private void saveResults(){
        Log.i("result",String.valueOf(quizzes.size()));

        for (int i=0; i<quizzes.size(); i++){
            Quiz quiz = (Quiz) mAdapter.getItem(i);
            results.put(quiz.getTitle(), quiz.getResult());
            if (i<2)
                Log.i("result", results.toString());
           // Log.i("result",quiz.getTitle() +" "+results.get(quiz.getTitle()));
        }

        Log.i("savedresult",results.toString());

        try {
            FileOutputStream out = openFileOutput("results", Context.MODE_PRIVATE);
            ObjectOutputStream outputStream = new ObjectOutputStream(out);
            outputStream.writeObject(results);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadResults(){
        try
        {
            FileInputStream fileInputStream = openFileInput("results");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            LinkedHashMap myHashMap = (LinkedHashMap)objectInputStream.readObject();
            results = null;
            results = myHashMap;
        }
        catch(ClassNotFoundException | IOException | ClassCastException e) {
            e.printStackTrace();
        }

        for (int i=0; i<quizzes.size(); i++){
            Quiz quiz = (Quiz) mAdapter.getItem(i);
            quiz.setResult(results.get(quiz.getTitle()));
            Log.i("loadedresult",results.get(quiz.getTitle()));
        }
    }

    private void writeToFile(String data, Context context, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename+".txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), "File write failed", Toast.LENGTH_LONG).show();
            Log.e("Exception", "File write failed: " + e.toString());
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
            Log.e("File", "File not found: " + e.toString());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Can not read file", Toast.LENGTH_LONG).show();
            Log.e("File", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public class DownloadQuizList extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Pobieranie danych...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            String result;
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line).append("\n");
                }
                result = sb.toString();

                in.close();

                writeToFile(result,getApplicationContext(),"quizList");

                return result;

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Cannot download4", Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonQuizList = new JSONObject(result);
                JSONArray jsonQuizArray = jsonQuizList.getJSONArray("items");

                String[] quizIds = new String[jsonQuizArray.length()];
                imageUrls = new String[jsonQuizArray.length()];

                for (int i = 0; i < jsonQuizArray.length(); i++) {

                    JSONObject quizObject = jsonQuizArray.getJSONObject(i);
                    quizIds[i] = "http://quiz.o2.pl/api/v1/quiz/"+quizObject.getString("id")+"/0";
                    JSONObject jPhotoObject = quizObject.getJSONObject("mainPhoto");
                    imageUrls[i] = jPhotoObject.getString("url");
                }

                downloadQuiz = new DownloadQuiz();
                downloadQuiz.execute(quizIds);

            } catch (JSONException e) {

                Toast.makeText(getApplicationContext(), "Cannot download3", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class DownloadQuiz extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... urls) {

            String result;
            URL url;
            HttpURLConnection urlConnection;

            try {

            for (int i = 0; i < urls.length; i++) {
                publishProgress(i);
                url = new URL(urls[i]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                result = sb.toString();

                in.close();

                writeToFile(result, getApplicationContext(), "quiz" + i);
                Log.i("image","save "+i+" quiz");
            }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Cannot download4", Toast.LENGTH_LONG).show();
                }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... i) {
            progressDialog.setMessage("Pobieranie quizów: "+i[0]+" / 100");
        }


        @Override
        protected void onPostExecute(String result) {
            if (isCancelled()) {
                File file = new File(getApplicationContext().getFilesDir().getPath() + "/quizList.txt");
                if (file.exists()) {
                    boolean delete = file.delete();
                    Log.i("delete", "file deleted");
                }
            }
            downloadImage = new DownloadImage();
            downloadImage.execute(imageUrls);
        }
    }

    public class DownloadImage extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            FileOutputStream outputStream;

            String imagename;

            for (int i=0; i<urls.length; i++) {
                    publishProgress(i);
                    imagename = "image" + i + ".jpg";

                    try {
                        Bitmap bitmap = Glide.with(getApplicationContext())
                                .load(urls[i])
                                .asBitmap()
                                .skipMemoryCache(true)
                                .centerCrop()
                                .into(240, 160)
                                .get();

                            outputStream = openFileOutput(imagename, Context.MODE_PRIVATE);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                            outputStream.flush();
                            outputStream.close();

                        Log.i("image","save "+i+" image");

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
        }

        @Override
        protected void onProgressUpdate(Integer... i) {
            progressDialog.setMessage("Pobieranie obrazów: "+i[0]+" / 100");
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (isCancelled()) {
                File file = new File(getApplicationContext().getFilesDir().getPath() + "/quizList.txt");
                if (file.exists()) {
                    boolean delete = file.delete();
                    Log.i("delete", "file deleted");
                }
            }

            if (progressDialog.isShowing()) { progressDialog.dismiss();}
            loadQuizzes();
        }
    }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    public void onStart() { super.onStart();  }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() { super.onDestroy(); }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("pause","entered on pause");
        saveResults();
        Log.i("save","after save");

    }
}
