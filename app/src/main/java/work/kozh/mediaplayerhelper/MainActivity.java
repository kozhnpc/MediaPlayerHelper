package work.kozh.mediaplayerhelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import work.kozh.media.MediaPlayerHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MediaPlayerHelper(MainActivity.this).setUp("haha", "a", "", "http://m7.music.126" +
                        ".net/20200326104419/96f26e7cd7cdcc53fa8620ed461e99d1/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3");
            }
        });


    }
}
