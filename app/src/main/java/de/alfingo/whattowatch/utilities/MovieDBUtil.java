package de.alfingo.whattowatch.utilities;

import android.content.Context;
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

import de.alfingo.whattowatch.Movie;
import de.alfingo.whattowatch.R;

/**
 * A class for getting all the information from MovieDB that is needed. This will create an
 * ArrayList with all the Movies inside, handle connections with the DB and so on.
 *
 * @author Rafael
 * @since 23.01.2017
 */
@SuppressWarnings("unused")
public abstract class MovieDBUtil {

    private static final String TAG = MovieDBUtil.class.getSimpleName();

    /**
     * URLs for the MovieDB utilities and the image servers from other sites.
     */
    private static final String
            MOVIEDB_URL = "https://api.themoviedb.org/3/",
            IMAGE_SERVER_URL = "http://image.tmdb.org/t/p",
            YOUTUBE_IMAGE_SERVER_URL = "https://img.youtube.com/vi",
            YOUTUBE_MOBILE_URL = "https://www.youtube.com";

    /**
     * Different query parameters
     */
    final private static String
            API_PARAM = "api_key",
            PAGE_PARAM = "page",
            REGION_PARAM = "region",
            LANGUAGE_PARAM = "language",
            YOUTUBE_VIDEO_PARAM = "v";
    /**
     * The paths for different queries
     */
    @SuppressWarnings("WeakerAccess")
    final public static String
            POPULAR_PATH = "popular",
            TOP_PATH = "top_rated",
            MOVIE_PATH = "movie",
            REVIEWS_PATH = "reviews",
            VIDEOS_PATH = "videos",
            IMAGE_SIZE_185_PATH = "w185",
            IMAGE_SIZE_ORIGINAL_PATH = "original",
            YOUTUBE_STD_QUALITY_PATH = "mqdefault.jpg",
            YOUTUBE_WATCH_PATH = "watch";

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
     * Gets all the movie details, reviews and videos included.
     *
     * @return a movie object with all the information needed on the details page.
     * @throws IOException if something didn't go quite as planned, duh!
     */
    public static Movie getMovieDetails(String movieID) throws IOException {
        Gson gson = new Gson();

        // getting the movie object
        Movie movie = getMovie(movieID);

        // now to get the reviews
        URL reviewsUrl = buildUrl(null, MOVIE_PATH, movieID, REVIEWS_PATH);
        String reviewsAnswer = NetworkUtils.getResponseFromHttpUrl(reviewsUrl);
        JsonArray reviewsArray = new JsonParser()
                .parse(reviewsAnswer)
                .getAsJsonObject()
                .getAsJsonArray(RESULTS_ANSWER);
        movie.reviews = new ArrayList<>();
        for (int i = 0; i < reviewsArray.size(); i++) {
            movie.reviews.add(gson.fromJson(reviewsArray.get(i), Movie.Review.class));
        }

        // and to get the videos
        URL videosUrl = buildUrl(null, MOVIE_PATH, movieID, VIDEOS_PATH);
        String videosAnswer = NetworkUtils.getResponseFromHttpUrl(videosUrl);
        JsonArray videosArray = new JsonParser()
                .parse(videosAnswer)
                .getAsJsonObject()
                .getAsJsonArray(RESULTS_ANSWER);
        movie.videos = new ArrayList<>();
        for (int i = 0; i < videosArray.size(); i++) {
            movie.videos.add(gson.fromJson(videosArray.get(i), Movie.MovieVideo.class));
        }

        return movie;
    }

    /**
     * Returns a movie from the movie db.
     *
     * @param movieID the movie ID for the movie which should be returned
     * @return the movie POJO
     * @throws IOException could be said, that something went wrong.
     */
    public static Movie getMovie(String movieID) throws IOException {
        Gson gson = new Gson();

        URL movieUrl = buildUrl(null, MOVIE_PATH, movieID);
        String movieDBAnswer = NetworkUtils.getResponseFromHttpUrl(movieUrl);
        JsonObject jsonAnswer = new JsonParser().parse(movieDBAnswer).getAsJsonObject();
        return gson.fromJson(jsonAnswer, Movie.class);
    }

    /**
     * Returns the URL path to a picture in the MovieDB server.
     *
     * @param picturePath The path of the picture, to be appended.
     * @param pSize       which size it should be.
     * @return the URL, ready to be picasso-ed
     */
    public static Uri getPictureUri(@NonNull String picturePath, @Nullable String pSize) {
        String size = pSize != null ? pSize : IMAGE_SIZE_185_PATH;
        return Uri.parse(IMAGE_SERVER_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(picturePath).build();
    }

    /**
     * This method gets from the website in question the Uri for the thumbnail image for a video.
     *
     * @param site    which site hosts the video, at the moment only youtube is supported
     * @param filmKey the key for the trailer
     * @return a formed uri pointing to the thumbnail image
     */
    public static Uri getThumbnailUri(@NonNull String site, @NonNull String filmKey) {
        if (Movie.MovieVideo.YOUTUBE.equalsIgnoreCase(site)) {
            return Uri.parse(YOUTUBE_IMAGE_SERVER_URL).buildUpon()
                    .appendPath(filmKey)
                    .appendPath(YOUTUBE_STD_QUALITY_PATH)
                    .build();
        } else
            throw new UnsupportedOperationException("only youtube images are supported at the moment.");
    }

    /**
     * Gets the video Uri for a given film and a site
     * @param site which site the video is hosted by, only supports youtube for now
     * @param filmKey the path/ key for the video
     * @return a built Uri for the video to be played.
     */
    public static Uri getVideoUri(@NonNull String site, @NonNull String filmKey) {
        if(Movie.MovieVideo.YOUTUBE.equalsIgnoreCase(site)) {
            return Uri.parse(YOUTUBE_MOBILE_URL).buildUpon()
                    .appendPath(YOUTUBE_WATCH_PATH)
                    .appendQueryParameter(YOUTUBE_VIDEO_PARAM, filmKey).build();
        } else
            throw new UnsupportedOperationException("only youtube videos are supported at the moment.");
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
