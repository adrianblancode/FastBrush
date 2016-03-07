package co.adrianblan.fastbrush;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.FrameLayout;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.adrianblan.fastbrush.settings.SettingsManager;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.fab_menu)
    FloatingActionsMenu fabMenu;

    private MyGLSurfaceView glSurfaceView;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(co.adrianblan.fastbrush.R.layout.activity_main);
        FrameLayout frame = (FrameLayout) findViewById(co.adrianblan.fastbrush.R.id.frame);
        ButterKnife.bind(this);

        glSurfaceView = new MyGLSurfaceView(this);
        frame.addView(glSurfaceView, 0);

        settingsManager = SettingsManager.createInstance(this);

        hideSystemUi();
    }

    @OnClick(R.id.fab_brush)
    public void onClickFabBrush() {

        fabMenu.collapse();

        new AlertDialog.Builder(this)
                .setTitle("Brush Settings")
                .setView(R.layout.dialog_brush)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        settingsManager.save();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @OnClick(R.id.fab_ink)
    public void onClickFabInk() {

        fabMenu.collapse();

        new AlertDialog.Builder(this)
                .setView(R.layout.dialog_ink)
                .setTitle("Ink Settings")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        settingsManager.save();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @OnClick(R.id.fab_delete)
    public void onClickFabDelete() {
        fabMenu.collapse();
        glSurfaceView.clearScreen();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Hides system UI when focus is regained
        if(hasFocus) {
            hideSystemUi();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    /** Hides the system UI and enters immersive mode if available */
    private void hideSystemUi() {

        int uiVisibilityFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        // Set immersive mode if >= Android 4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiVisibilityFlags = uiVisibilityFlags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(uiVisibilityFlags);
    }
}
