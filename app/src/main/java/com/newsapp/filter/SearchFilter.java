package com.newsapp.filter;

import android.widget.Filter;

import com.newsapp.adapter.ArticleAdapter;
import com.newsapp.model.Article;

import java.util.ArrayList;
import java.util.List;

public class SearchFilter extends Filter {

    private ArticleAdapter adapter;
    private List<Article> filterList;

    public SearchFilter(List<Article> filterList, ArticleAdapter adapter) {
        this.adapter = adapter;
        this.filterList = filterList;

    }

    //filtering occurs
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        //check constraint validity
        if (constraint != null && constraint.length() > 0) {
            //change to upper
            constraint = constraint.toString().toUpperCase();
            //store our filtered item
            ArrayList<Article> list = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                //check if the string contains the search text
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    //add article to filtered articles
                    list.add(filterList.get(i));
                }
            }

            results.count = list.size();
            results.values = list;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.setFilteredItem((List<Article>) results.values);

    }
}
