package co.adrianblan.fastbrush;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.gson.Gson;
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

    @Bind(R.id.fab_undo)
    FloatingActionButton fabUndo;

    @Bind(R.id.fab_delete)
    FloatingActionButton fabDelete;

    private MyGLSurfaceView glSurfaceView;
    private FrameLayout mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(co.adrianblan.fastbrush.R.layout.activity_main);
        mainView = (FrameLayout) findViewById(co.adrianblan.fastbrush.R.id.frame);
        ButterKnife.bind(this);

        glSurfaceView = new MyGLSurfaceView(this);
        mainView.addView(glSurfaceView, 0);

        if(!Utils.isTablet()){
            fabBrush.setSize(FloatingActionButton.SIZE_MINI);
            fabInk.setSize(FloatingActionButton.SIZE_MINI);
            fabSave.setSize(FloatingActionButton.SIZE_MINI);
            fabUndo.setSize(FloatingActionButton.SIZE_MINI);
            fabDelete.setSize(FloatingActionButton.SIZE_MINI);

            // Resize fab menu so all buttons fit on phones
            fabMenu.setScaleX(0.9f);
            fabMenu.setScaleY(0.9f);

            // Decrease the bottom margin to reposition
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            lp.bottomMargin = (int) -Utils.convertDpToPixel(16);
            lp.leftMargin = (int) -Utils.convertDpToPixel(16);
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            fabMenu.setLayoutParams(lp);
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

        // Check that we have external storage write permission on Marshmallow devices
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Snackbar.make(mainView, "Saving image...", Snackbar.LENGTH_SHORT).show();
            glSurfaceView.saveImage();
        }
    }

    @OnClick(R.id.fab_delete)
    public void onClickFabDelete() {
        fabMenu.collapse();
        glSurfaceView.clearScreen();
    }


    @OnClick(R.id.fab_undo)
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
