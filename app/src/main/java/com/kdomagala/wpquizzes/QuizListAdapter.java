package com.kdomagala.wpquizzes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class QuizListAdapter extends RecyclerView.Adapter {

    private List<Quiz> mQuizList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    Context context;
    TextView titleTextView;
    //private final Context mContext;

    private static final String TAG = "WPQuizzes";

    /*public QuizListAdapter(Context context) {

        mContext = context;
    }*/

    public void add(Quiz quiz) {

        mQuizList.add(quiz);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {

        return mQuizList.size();
    }

    public int getCount() {

        return mQuizList.size();
    }

    // Retrieve the number of Items

    public Object getItem(int pos) {

        return mQuizList.get(pos);
    }

    // Get the position for the Item

    @Override
    public long getItemId(int pos) {

        return pos;
    }

    // Create a View for the Item at specified position

    private class ViewHolder extends RecyclerView.ViewHolder {
        //RelativeLayout itemLayout;
        TextView mTitleAdapterView;
        TextView mResultAdapterView;
        ImageView mAdapterImageView;

        public ViewHolder(View pItem) {
            super(pItem);
            mTitleAdapterView = (TextView) pItem.findViewById(R.id.quizAdapterTitle);
            mResultAdapterView = (TextView) pItem.findViewById(R.id.quizAdapterResult);
            mAdapterImageView = (ImageView) pItem.findViewById(R.id.quizAdapterImage);
        }
    }

    public QuizListAdapter(ArrayList<Quiz> pQuizzes, RecyclerView pRecyclerView){
        mQuizList = pQuizzes;
        mRecyclerView = pRecyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        // tworzymy layout artykułu oraz obiekt ViewHoldera
        final View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.quiz_adapter, viewGroup, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mRecyclerView.getChildAdapterPosition(v);
                Intent intent = new Intent(v.getContext(), QuizActivity.class);
                titleTextView = (TextView)v.findViewById(R.id.quizAdapterTitle);

                // Quiz enteredQuiz = (Quiz) viewHolder.getItemAtPosition(position);

                intent.putExtra("position", position);
                intent.putExtra("title", titleTextView.getText().toString());
                ((Activity) v.getContext()).startActivityForResult(intent, 1);
            }
        });

        // dla elementu listy ustawiamy obiekt OnClickListener,
        // który usunie element z listy po kliknięciu na niego

        /*view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // odnajdujemy indeks klikniętego elementu
                int positionToDelete = mRecyclerView.getChildAdapterPosition(v);
                // usuwamy element ze źródła danych
                mQuizList.remove(positionToDelete);
                // poniższa metoda w animowany sposób usunie element z listy
                notifyItemRemoved(positionToDelete);
            }
        });*/

        // tworzymy i zwracamy obiekt ViewHolder
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int i) {
        // uzupełniamy layout artykułu
        Quiz quiz = mQuizList.get(i);
        ((ViewHolder) viewHolder).mTitleAdapterView.setText(quiz.getTitle());
        ((ViewHolder) viewHolder).mResultAdapterView.setText(quiz.getResult());
        ((ViewHolder) viewHolder).mAdapterImageView.setImageBitmap(quiz.getBitmap());

    }

    /*@Override
    public int getItemCount() {
        return mArticles.size();
    }*/

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        ViewHolder holder;
        if(row == null) {

            // Inflate the View for this Item from xml
            LayoutInflater mInflater = LayoutInflater.from(mContext); // context pass to the constructor of adapter
            row = mInflater.inflate(R.layout.quiz_adapter, parent, false);
            //Now create the ViewHolder
            holder = new ViewHolder();
            //and set its textView field to the proper value
            holder.itemLayout =  row.findViewById(R.id.RelativeLayout1);
            holder.titleAdapterView = row.findViewById(R.id.quizAdapterTitle);
            holder.resultAdapterView = row.findViewById(R.id.quizAdapterResult);
            holder.quizAdapterImage = row.findViewById(R.id.quizAdapterImage);

            //and store it as the 'tag' of our view
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        // Get the current Item
        final Quiz quiz = (Quiz) getItem(position);

        holder.titleAdapterView.setText(quiz.getTitle());
        holder.resultAdapterView.setText(quiz.getResult());
        holder.quizAdapterImage.setImageBitmap(quiz.getBitmap());

        return row;

    }*/
}

