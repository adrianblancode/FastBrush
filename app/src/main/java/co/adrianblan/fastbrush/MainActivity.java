package co.adrianblan.fastbrush;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.adrianblan.fastbrush.dialog.BrushDialogFragment;
import co.adrianblan.fastbrush.dialog.InkDialogFragment;
import co.adrianblan.fastbrush.utils.Utils;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private MyGLSurfaceView glSurfaceView;
    private RelativeLayout mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(co.adrianblan.fastbrush.R.layout.activity_main);
        mainView = (RelativeLayout) findViewById(co.adrianblan.fastbrush.R.id.frame);
        ButterKnife.bind(this);

        glSurfaceView = new MyGLSurfaceView(this);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        lp.addRule(RelativeLayout.LEFT_OF, toolbar.getId());
        glSurfaceView.setLayoutParams(lp);

        mainView.addView(glSurfaceView, 0);

        hideSystemUi();
    }

    @OnClick(R.id.button_brush)
    public void onClickFabBrush() {

        DialogFragment newFragment = new BrushDialogFragment();
        newFragment.show(getSupportFragmentManager(), "brushDialog");
    }

    @OnClick(R.id.button_ink)
    public void onClickFabInk() {
        DialogFragment newFragment = new InkDialogFragment();
        newFragment.show(getSupportFragmentManager(), "inkDialog");
    }

    @OnClick(R.id.button_save)
    public void onClickFabSave() {
        // Check that we have external storage write permission on Marshmallow devices
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Snackbar.make(mainView, "Saving image...", Snackbar.LENGTH_SHORT).show();
            glSurfaceView.saveImage();
        }
    }

    @OnClick(R.id.button_delete)
    public void onClickFabDelete() {
        glSurfaceView.clearScreen();
    }


    @OnClick(R.id.button_undo)
    public void onClickFabUndo() {
        glSurfaceView.undo();
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
