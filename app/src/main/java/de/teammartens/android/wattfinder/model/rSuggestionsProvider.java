package de.teammartens.android.wattfinder.model;

import android.content.SearchRecentSuggestionsProvider;

public class rSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.teammartens.android.wattfinder.model.rSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;


    public rSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}
