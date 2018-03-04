package com.kdomagala.wpquizzes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    QuizListAdapter mAdapter;
    ListView quizList;
    //int i=0;
    //int j=0;
    LinkedHashMap<String, String> results = new LinkedHashMap<>();
    ProgressDialog progressDialog;
    String[] imageUrls;
    private static Context mContext;
    DownloadImage mDownloadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        quizList = findViewById(R.id.quizList);
        mAdapter = new QuizListAdapter(getApplicationContext());

        quizList = findViewById(R.id.quizList);
        quizList.setAdapter(mAdapter);
        registerForContextMenu(quizList);

        File file = new File (getApplicationContext().getFilesDir().getPath()+"/quizList.txt");
        if(file.exists() && mAdapter.getCount()==0){
                loadQuizzes();
        }
        else {
            DownloadQuizList downloadQuizList = new DownloadQuizList();
            downloadQuizList.execute("http://quiz.o2.pl/api/v1/quizzes/0/100");
            Log.i("File", "File doesn't exists");
        }

        File resultsfile = new File (getApplicationContext().getFilesDir().getPath()+"/results");
        if(resultsfile.exists()){
            Log.e("File", "File exists");
            loadResults();
        }

        quizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                Quiz enteredQuiz = (Quiz) adapterView.getItemAtPosition(position);

                intent.putExtra("position", position);
                intent.putExtra("title", enteredQuiz.getTitle());
                startActivityForResult(intent, 1);
            }
        });
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
            quizList.setSelection(position);
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

                for (int i = 0; i < jsonQuizArray.length(); i++) {

                    String title;
                    JSONObject quizObject = jsonQuizArray.getJSONObject(i);
                    BitmapFactory.decodeFile(getApplicationContext().getFilesDir().getPath()+"/image"+i+".jpg", options);
                    options.inSampleSize = calculateInSampleSize(options, 160, 120);
                    options.inJustDecodeBounds = false;
                    Bitmap quizImage = BitmapFactory.decodeFile(getApplicationContext().getFilesDir().getPath()+"/image"+i+".jpg");
                    Log.e("Size",String.valueOf(quizImage.getByteCount()));
                    title = quizObject.getString("title");
                    mAdapter.add(new Quiz(title, "", quizImage));
                }

                //i=0;

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Cannot download3", Toast.LENGTH_LONG).show();
            }
    }

    private void saveResults(){

        for (int i=0; i<mAdapter.getCount(); i++){
            Quiz quiz = (Quiz) mAdapter.getItem(i);
            results.put(quiz.getTitle(), quiz.getResult());
        }

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

        for (int i=0; i<mAdapter.getCount(); i++){
            Quiz quiz = (Quiz) mAdapter.getItem(i);
            quiz.setResult(results.get(quiz.getTitle()));
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
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Can not read file", Toast.LENGTH_LONG).show();
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public class DownloadQuizList extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Pobieranie danych...");
            progressDialog.setCanceledOnTouchOutside(false);
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

                    //"http://quiz.o2.pl/api/v1/quiz/"+quizIds[i]+"/0";
                    quizIds[i] = "http://quiz.o2.pl/api/v1/quiz/"+quizObject.getString("id")+"/0";
                    JSONObject jPhotoObject = quizObject.getJSONObject("mainPhoto");
                    String photoUrl = "";
                    photoUrl = jPhotoObject.getString("url");
                    imageUrls[i] = jPhotoObject.getString("url");
                }

                DownloadQuiz downloadQuiz = new DownloadQuiz();
                downloadQuiz.execute(quizIds);

                //i=0;
                //j=0;

            } catch (JSONException e) {

                Toast.makeText(getApplicationContext(), "Cannot download3", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class DownloadQuiz extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {

            String result;
            URL url;
            HttpURLConnection urlConnection;

            try {

            for (int i = 0; i < urls.length; i++) {

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

                //i++;
                //return result;
            }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Cannot download4", Toast.LENGTH_LONG).show();
                }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            DownloadImage downloadImage = new DownloadImage();
            downloadImage.execute(imageUrls);
        }
    }

    /*public class BitmapWithName {
        private final Bitmap mBitmap;
        private final String mName;

        public BitmapWithName(Bitmap bitmap, String name)
        {
            this.mBitmap = bitmap;
            this.mName = name;
        }

        public Bitmap getBitmap(){
            return mBitmap;
        }

        public String getName() {
            return mName;
        }
    }*/

    public static Context getContext() {
        return mContext;
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {

       // private WeakReference<MainActivity> mainActivityReference;
        //private WeakReference<DownloadQuiz> activityReference;

        //WeakReference<Context> cReference = new WeakReference<Context>(getContext());

        // only retain a weak reference to the activity

        /*DownloadImage(MainActivity context) {
            mainActivityReference = new WeakReference<>(context);
        }

        DownloadImage(DownloadQuiz context) {
            activityReference = new WeakReference<>(context);
        }
   */

        @Override
        protected Bitmap doInBackground(String... urls) {

            FileOutputStream outputStream;

            String imagename;
            //BitmapWithName bitmapWithName;

            //DownloadQuiz activity = activityReference.get();
            //MainActivity mainActivity = mainActivityReference.get();


            for (int i=0; i<urls.length; i++) {
                    imagename = "image" + i + ".jpg";

                    try {
                        Bitmap bitmap = Glide.with(getContext())
                                .load(urls[i])
                                .asBitmap()
                                .skipMemoryCache(true)
                                .centerCrop()
                                .into(320, 240)
                                .get();

                        //bitmapWithName = new BitmapWithName(bitmap, imagename);

                            outputStream = openFileOutput(imagename, Context.MODE_PRIVATE);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                            outputStream.flush();
                            outputStream.close();

                       // mainActivity.saveImage(bitmap, imagename);
                        Log.i("image","save "+i+" image");
                        //j++;
                        //return bitmapWithName;

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
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //MainActivity activity = mainActivityReference.get();
            //if (activity == null || activity.isFinishing()) return;
            progressDialog.dismiss();
            loadQuizzes();
        }
    }

    private void saveImage(Bitmap image, String imagename) {

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(imagename, Context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
    }

   @Override
    public void onResume() {
        super.onResume();

        /*if (mAdapter.getCount() == 0)
            loadQuizzes();*/
    }

    @Override
    public void onStart() {
        super.onStart();

        /*if (mAdapter.getCount() == 0)
            loadQuizzes();*/
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveResults();
    }
}
