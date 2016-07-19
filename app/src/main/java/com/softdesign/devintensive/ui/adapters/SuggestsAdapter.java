package com.softdesign.devintensive.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softdesign.devintensive.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SuggestsAdapter extends RecyclerView.Adapter<SuggestsAdapter.SuggestViewHolder> {

    private List<SuggestModel> mSuggestList;
    private List<SuggestModel> mSuggestListCopy;

    private SuggestViewHolder.OnSuggestionClickListener mSuggestClickListener;

    public SuggestsAdapter(List<SuggestModel> suggestList,
                           SuggestViewHolder.OnSuggestionClickListener suggestClickListener) {

        mSuggestList = suggestList;
        mSuggestListCopy = new ArrayList<>();
        mSuggestListCopy.addAll(mSuggestList);

        mSuggestClickListener = suggestClickListener;
    }

    @Override
    public SuggestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_list, parent, false);

        return new SuggestViewHolder(view, mSuggestClickListener);
    }

    @Override
    public void onBindViewHolder(SuggestViewHolder holder, int position) {
        holder.mUserName.setText(mSuggestList.get(position).getUserName());
    }

    @Override
    public int getItemCount() {
        return mSuggestList.size();
    }

    public void filter(String filterText) {
        if(filterText.isEmpty()){
            mSuggestList.clear();
            mSuggestList.addAll(mSuggestListCopy);
        } else{
            List<SuggestModel> filterResult = new ArrayList<>();
            filterText = filterText.toLowerCase();
            for(SuggestModel suggestion: mSuggestListCopy){
                if(suggestion.getUserName().toLowerCase().contains(filterText)){
                    filterResult.add(suggestion);
                }
            }
            mSuggestList.clear();
            mSuggestList.addAll(filterResult);
        }
        notifyDataSetChanged();
    }

    public void update(List<SuggestModel> suggestList) {
        mSuggestList = suggestList;
        mSuggestListCopy = new ArrayList<>();
        mSuggestListCopy.addAll(mSuggestList);
        notifyDataSetChanged();
    }

    public SuggestModel getModel(int position) {
        return mSuggestList.get(position);
    }

    public static class SuggestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView mUserName;
        private OnSuggestionClickListener mListener;

        public SuggestViewHolder(View itemView, OnSuggestionClickListener suggestClickListener) {
            super(itemView);
            this.mListener = suggestClickListener;
            itemView.setOnClickListener(this);

            mUserName = (TextView) itemView.findViewById(R.id.user_name_txt);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onSuggestionClickListener(getAdapterPosition());
            }
        }

        public interface OnSuggestionClickListener {
            void onSuggestionClickListener(int position);
        }
    }

    public static class SuggestModel {

        private String mUserName;
        private int mPosition;

        public SuggestModel(String userName, int position) {
            mUserName = userName;
            mPosition = position;
        }

        public String getUserName() {
            return mUserName;
        }

        public int getPosition() {
            return mPosition;
        }
    }
}
