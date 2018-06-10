package de.teammartens.android.wattfinder.model;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.worker.GeoWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;

/**
 * Created by felix on 10.05.15.
 */
public class ArrayAdapterSearchView extends SearchView {

    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private OnSearchViewCollapsedEventListener mSearchViewCollapsedEventListener;

    public ArrayAdapterSearchView(Context context) {
        super(context);
        initialize();
    }


    public ArrayAdapterSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {
        mSearchAutoComplete = (SearchAutoComplete) findViewById(android.support.v7.appcompat.R.id.search_src_text);
        this.setAdapter(null);
        this.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) parent.getAdapter().getItem(position);
                String query = c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY));
                mSearchAutoComplete.setText(c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)));
                if (LogWorker.isVERBOSE())
                    LogWorker.d("ArrayAdapterSearchView", "Itemclicked " + position + " " + query);
                if (c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA)).equals("RecentSuggestion"))
                    GeoWorks.starteSuche(query);
                else
                    GeoWorks.starteSucheSuggested(query);
                c.close();
                InputMethodManager imm = (InputMethodManager) KartenActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
    }

    @Override
    public void setSuggestionsAdapter(CursorAdapter adapter) {
        // don't let anyone touch this
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSearchAutoComplete.setOnItemClickListener(listener);
    }

    public void setAdapter(ArrayAdapter<?> adapter) {
        mSearchAutoComplete.setAdapter(adapter);
    }

    public void setText(String text) {
        mSearchAutoComplete.setText(text);
    }

    @Override
    public void onActionViewCollapsed() {
        if (mSearchViewCollapsedEventListener != null){
            mSearchViewCollapsedEventListener.onSearchViewCollapsed();

        }
        mSearchAutoComplete.setVisibility(GONE);
        super.onActionViewCollapsed();
    }

    public interface OnSearchViewCollapsedEventListener{
         void onSearchViewCollapsed();
    }

    public void setOnSearchViewCollapsedEventListener(OnSearchViewCollapsedEventListener eventListener) {
        mSearchViewCollapsedEventListener = eventListener;
    }

}