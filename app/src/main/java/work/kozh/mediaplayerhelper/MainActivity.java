package work.kozh.mediaplayerhelper;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import work.kozh.media.MediaPlayerHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaPlayerHelper helper = new MediaPlayerHelper(MainActivity.this);
        helper.setUp("haha", "a", "", "http://m7.music.126.net/20200325191658/ec5494fa76f22ee029903396d93aa886/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3");

    }
}
