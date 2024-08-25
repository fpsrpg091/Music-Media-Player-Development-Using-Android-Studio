package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;

public class LyricsActivity extends AppCompatActivity {

    private TextView lyricsTextView;
    private TextView songTitleTextView;
    private TextView songArtistTextView;
    private Button reloadButton;
    private String songTitle;
    private String artistName;
    private static final String ACCESS_TOKEN = "6F7shvD4_UVnaJUvfYaU_SbfRjnPWpHytp0geDc3FOC8XES_Hq8YlhDrdfAZw5AX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        lyricsTextView = findViewById(R.id.lyricsTextView);
        reloadButton = findViewById(R.id.reloadButton);
        songTitleTextView = findViewById(R.id.songTitleTextView);
        songArtistTextView = findViewById(R.id.songArtistTextView);

        // Get song title and artist name from intent extras
        songTitle = getIntent().getStringExtra("SONG_TITLE");
        artistName = getIntent().getStringExtra("ARTIST_NAME");

        new FetchLyricsTask().execute(songTitle.endsWith(".mp3") ? songTitle.substring(0, songTitle.length() - 4):songTitle, artistName.equals("<unknown>") ? null : artistName);

        reloadButton.setOnClickListener(v -> showInputDialog());

        songTitleTextView.setText(songTitle);
        songArtistTextView.setText(artistName);
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Song Info");

        // Set up the input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Remove ".mp3" extension if it exists
        String displaySongTitle = songTitle.endsWith(".mp3") ? songTitle.substring(0, songTitle.length() - 4) : songTitle;

        // Create song title input field with label
        LinearLayout songTitleLayout = new LinearLayout(this);
        songTitleLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView songTitleLabel = new TextView(this);
        songTitleLabel.setText(" Song Title:     ");
        songTitleLayout.addView(songTitleLabel);

        final EditText inputSongTitle = new EditText(this);
        inputSongTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        inputSongTitle.setHint("Song Title");
        inputSongTitle.setText(displaySongTitle);
        songTitleLayout.addView(inputSongTitle);

        layout.addView(songTitleLayout);

        // Create artist name input field with label
        LinearLayout artistNameLayout = new LinearLayout(this);
        artistNameLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView artistNameLabel = new TextView(this);
        artistNameLabel.setText(" Artist Name: ");
        artistNameLayout.addView(artistNameLabel);

        final EditText inputArtistName = new EditText(this);
        inputArtistName.setInputType(InputType.TYPE_CLASS_TEXT);
        inputArtistName.setHint("Artist Name");
        inputArtistName.setText(artistName.equals("<unknown>") ? "" : artistName);
        artistNameLayout.addView(inputArtistName);

        layout.addView(artistNameLayout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                songTitle = inputSongTitle.getText().toString();
                artistName = inputArtistName.getText().toString();
                new FetchLyricsTask().execute(songTitle, artistName.equals("<unknown>") || artistName.isEmpty() ? null : artistName);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(layout);
        builder.show();
    }

    private class FetchLyricsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lyricsTextView.setText("Loading lyrics...");
        }

        @Override
        protected String doInBackground(String... params) {
            String songTitle = params[0].replaceAll("[_-]", " ").trim();
            String artistName = params[1];
            OkHttpClient client = new OkHttpClient();

            try {
                // Encode the query parameters, excluding artistName if it's null
                String query = java.net.URLEncoder.encode(songTitle + (artistName == null ? "" : " " + artistName), "UTF-8");
                String url = "https://api.genius.com/search?q=" + query;

                // Create the request to the Genius API
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + ACCESS_TOKEN)
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .addHeader("Accept-Language", "en-US,en;q=0.9")
                        .build();

                // Execute the request
                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();
                Log.d("GeniusAPI", "Response: " + jsonData);

                // Parse the JSON response
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONArray hits = jsonObject.getJSONObject("response").getJSONArray("hits");

                if (hits.length() > 0) {
                    JSONObject result = hits.getJSONObject(0).getJSONObject("result");
                    String lyricsUrl = result.getString("url");

                    // Fetch the lyrics from the Genius lyrics URL
                    Document lyricsPage = Jsoup.connect(lyricsUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .get();

                    Log.d("GeniusAPI", "Lyrics Page URL: " + lyricsUrl);

                    // Extract lyrics from all containers with data-lyrics-container=true
                    Elements lyricsDivs = lyricsPage.select("div[data-lyrics-container=true]");
                    StringBuilder fullLyrics = new StringBuilder();
                    Log.d("GeniusAPI", "Lyrics Page URL: " + lyricsDivs);
                    for (org.jsoup.nodes.Element lyricsDiv : lyricsDivs) {
                        // Extract HTML content
                        String lyricsHtml = lyricsDiv.html();

                        // Replace <br> tags with new lines and remove other HTML tags
                        String lyricsText = lyricsHtml
                                .replaceAll("(<br[^>]*>){2,}", "\n")  // Replace two or more consecutive <br> tags with a single newline
//                                .replaceAll("<br[^>]*>", "\n")       // Replace single <br> tags with a newline
                                .replaceAll("</?[^>]+>", "");          // Remove all other HTML tags

                        fullLyrics.append(lyricsText).append("\n");
                    }

                    return fullLyrics.toString().trim();
                }
            } catch (IOException | JSONException e) {
                Log.e("GeniusAPI", "Error fetching lyrics", e);
            }

            return "Lyrics not found.";
        }

        @Override
        protected void onPostExecute(String lyrics) {
            lyricsTextView.setText(lyrics);
        }
    }


}
