package de.alfingo.whattowatch.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.alfingo.whattowatch.Movie;
import de.alfingo.whattowatch.R;

/**
 * A class for getting all the information from MovieDB that is needed. This will create an
 * ArrayList with all the Movies inside, handle connections with the DB and so on.
 *
 * @author Rafael
 * @since 23.01.2017
 */
public abstract class MovieDBUtil {

    private static final String TAG = MovieDBUtil.class.getSimpleName();

    /**
     * URLs for the MovieDB utilities
     */
    private static final String
            MOVIEDB_URL = "https://api.themoviedb.org/3/",
            IMAGE_SERVER_URL = "http://image.tmdb.org/t/p";

    /**
     * Different query parameters
     */
    final private static String
            API_PARAM = "api_key",
            PAGE_PARAM = "page",
            REGION_PARAM = "region",
            LANGUAGE_PARAM = "language";
    /**
     * The paths for different queries
     */
    final public static String
            POPULAR_PATH = "popular",
            TOP_PATH = "top_rated",
            MOVIE_PATH = "movie",
            IMAGE_SIZE_185_PATH = "w185",
            IMAGE_SIZE_ORIGINAL_PATH = "original";

    /**
     * Answer constants from the MovieDB JSON
     */
    final private static String
            RESULTS_ANSWER = "results",
            MAX_PAGES_ANSWER = "total_pages";

    /**
     * This will get all the movies that will fill our Grid, for unlimited scrolling, the page
     * param is needed.
     *
     * @param context For getting our sorting methods.
     * @param sorting Which sorting should be used, the available ones are listed in the MovieDB
     *                site, by default it will sort by popularity.
     * @param page    Which page should be returned, by default it will return page one.
     * @return An array with all the Movie Objects returned.
     * @throws IOException        If something with the connection is not right.
     * @throws JsonParseException If the JSON Object was malformed, or the site is down.
     */
    public static ArrayList<Movie> getAllMovies(@NonNull Context context, Integer sorting, int page)
            throws JsonParseException, IOException {
        int topRatedConstant = context.getResources().getInteger(R.integer.top_rated_index);
        int pageParamValue = page > 1 ? page : 1;
        // should extend to a switch case when more sorting methods are available
        String sortPath = (topRatedConstant == sorting) ? TOP_PATH : POPULAR_PATH;
        String[][] queries = {{PAGE_PARAM, String.valueOf(pageParamValue)}};
        URL builtURL = buildUrl(queries, MOVIE_PATH, sortPath);

        String movieDBAnswer = NetworkUtils.getResponseFromHttpUrl(builtURL);
        JsonObject jsonAnswer = new JsonParser().parse(movieDBAnswer).getAsJsonObject();
        JsonArray jsonArray = jsonAnswer.getAsJsonArray(RESULTS_ANSWER);

        Gson gsonInstance = new Gson();

        ArrayList<Movie> moviesList = new ArrayList<>();

        // iterates over the JSON Array and creates every Movie Object inside it.
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement movieJson = jsonArray.get(i);
            Movie movie = gsonInstance.fromJson(movieJson, Movie.class);
            moviesList.add(movie);
        }

        return moviesList.isEmpty() ? null : moviesList;
    }

    /**
     * Returns the URL path to a picture in the MovieDB server.
     *
     * @param picturePath The path of the picture, to be appended.
     * @param pSize        which size it should be.
     * @return the URL, ready to be picasso-ed
     */
    public static Uri getPictureUri(@NonNull String picturePath, @Nullable String pSize) {
        String size = pSize != null? pSize : IMAGE_SIZE_185_PATH;
        return Uri.parse(IMAGE_SERVER_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(picturePath).build();
    }

    /**
     * Builds the URL to our MovieDB, with the paths and queries specified in the parameters.
     *
     * @param queries    Given as string array tuple containing the query name and value each time
     * @param extraPaths One or more paths to be added, at least
     * @return The URL to use to query the weather server.
     */
    private static URL buildUrl(@Nullable String[][] queries, @NonNull String... extraPaths) {

        Uri.Builder builder = Uri.parse(MOVIEDB_URL).buildUpon();
        // appends all the extra paths
        for (String path : extraPaths) {
            builder.appendPath(path);
        }
        // appends the API Key
        builder.appendQueryParameter(API_PARAM, APIKeys.MOVIEDB_API_KEY);
        // appends all the extra queries
        if (queries != null)
            for (String[] query : queries) {
                builder.appendQueryParameter(query[0], query[1]);
            }

        Uri builtUri = builder.build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

}
