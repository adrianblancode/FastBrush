package co.adrianblan.fastbrush;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.FrameLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.adrianblan.fastbrush.dialog.BrushDialogFragment;
import co.adrianblan.fastbrush.dialog.InkDialogFragment;
import co.adrianblan.fastbrush.settings.SettingsManager;
import co.adrianblan.fastbrush.utils.Utils;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.fab_menu)
    FloatingActionsMenu fabMenu;

    @Bind(R.id.fab_brush)
    FloatingActionButton fabBrush;

    @Bind(R.id.fab_ink)
    FloatingActionButton fabInk;

    @Bind(R.id.fab_save)
    FloatingActionButton fabSave;

    @Bind(R.id.fab_delete)
    FloatingActionButton fabDelete;

    private MyGLSurfaceView glSurfaceView;
    private SettingsManager settingsManager;

    private FrameLayout mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(co.adrianblan.fastbrush.R.layout.activity_main);
        mainView = (FrameLayout) findViewById(co.adrianblan.fastbrush.R.id.frame);
        ButterKnife.bind(this);

        glSurfaceView = new MyGLSurfaceView(this);
        mainView.addView(glSurfaceView, 0);

        settingsManager = SettingsManager.getInstance(this);

        if(!Utils.isTablet()){
            fabBrush.setSize(FloatingActionButton.SIZE_MINI);
            fabInk.setSize(FloatingActionButton.SIZE_MINI);
            fabSave.setSize(FloatingActionButton.SIZE_MINI);
            fabDelete.setSize(FloatingActionButton.SIZE_MINI);
        }

        hideSystemUi();
    }

    @OnClick(R.id.fab_brush)
    public void onClickFabBrush() {

        fabMenu.collapse();
        DialogFragment newFragment = new BrushDialogFragment();
        newFragment.show(getSupportFragmentManager(), "brushDialog");
    }

    @OnClick(R.id.fab_ink)
    public void onClickFabInk() {

        fabMenu.collapse();
        DialogFragment newFragment = new InkDialogFragment();
        newFragment.show(getSupportFragmentManager(), "inkDialog");
    }

    @OnClick(R.id.fab_save)
    public void onClickFabSave() {
        fabMenu.collapse();
        Snackbar.make(mainView, "Saving image...", Snackbar.LENGTH_SHORT).show();
        glSurfaceView.saveImage();
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
