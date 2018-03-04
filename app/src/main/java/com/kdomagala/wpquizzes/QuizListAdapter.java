package com.kdomagala.wpquizzes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class QuizListAdapter extends BaseAdapter {

    private final List<Quiz> mQuizList = new ArrayList<>();
    private final Context mContext;

    private static final String TAG = "BuildDiary";

    public QuizListAdapter(Context context) {

        mContext = context;
    }

    // Add a Item to the adapter
    // Notify observers that the data set has changed

    public void add(Quiz quiz) {

        mQuizList.add(quiz);
        notifyDataSetChanged();
        //Log.i(TAG, "Add quiz from QuizListAdapter");
    }

   public void edit(Quiz quiz, int position){
        mQuizList.set(position, quiz);
        notifyDataSetChanged();
        Log.i(TAG, "Edit item from ItemListAdapter");

    }

    @Override
    public int getCount() {

        return mQuizList.size();
    }

    // Retrieve the number of Items

    @Override
    public Object getItem(int pos) {

        return mQuizList.get(pos);
    }

    // Get the position for the Item

    @Override
    public long getItemId(int pos) {

        return pos;
    }

    // Create a View for the Item at specified position
    // Remember to check whether convertView holds an already allocated View
    // before created a new View.
    // Consider using the ViewHolder pattern to make scrolling more efficient
    // See: http://developer.android.com/training/improving-layouts/smooth-scrolling.html

    private static class ViewHolder {
        RelativeLayout itemLayout;
        TextView titleAdapterView;
        TextView resultAdapterView;
        ImageView quizAdapterImage;
    }

    @Override
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
            //We've already seen this one before!
            holder = (ViewHolder) row.getTag();
        }

        // Get the current Item
        final Quiz quiz = (Quiz) getItem(position);

        holder.titleAdapterView.setText(quiz.getTitle());
        holder.resultAdapterView.setText(quiz.getResult());
        holder.quizAdapterImage.setImageBitmap(quiz.getBitmap());

        return row;

    }
}

