package com.vuw.audiogeotagger;

import android.content.SearchRecentSuggestionsProvider;

/**
 * This class provides search suggestions based upon recent search queries for the map search dialog
 * @author Weiya Xu
 *
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.vuw.audiogeotagger.SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
    
    public SearchSuggestionProvider() {
        super();
        setupSuggestions(AUTHORITY, MODE);
    }
}
