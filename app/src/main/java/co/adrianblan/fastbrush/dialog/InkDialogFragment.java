package co.adrianblan.fastbrush.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.adrianblan.fastbrush.MainActivity;
import co.adrianblan.fastbrush.R;
import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.settings.SettingsManager;

/**
 * Fragment for showing a ink dialog.
 */
public class InkDialogFragment extends DialogFragment {

    @Bind(R.id.seekBarInkOpacity)
    SeekBar seekBarInkOpacity;

    @Bind(R.id.inkBarOpacitySubtitle)
    TextView inkOpacitySubtitle;

    @Bind(R.id.inkFluidity)
    RadioGroup inkFluidity;

    @Bind(R.id.radioButtonDry)
    RadioButton radioButtonDry;

    @Bind(R.id.radioButtonWet)
    RadioButton radioButtonWet;

    @Bind(R.id.buttonSelectColor)
    AppCompatButton buttonSelectColor;

    @Bind(R.id.colorCircle)
    ImageView colorCircle;

    private SettingsManager settingsManager;
    private SettingsData settingsData;
    private View mainView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        settingsManager = SettingsManager.getInstance(getActivity());
        settingsData = settingsManager.getSettingsData();

        mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_ink, null);
        ButterKnife.bind(this, mainView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Ink Settings")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        settingsManager.saveSettingsData(settingsData);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.setView(mainView);

        final AlertDialog alertDialog = builder.create();

        // Set color to accent color
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(getActivity(), R.color.colorAccent));

                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                        ContextCompat.getColor(getActivity(), R.color.colorAccent));

                // Set default values, has no effect until dialog is shown
                seekBarInkOpacity.setProgress(settingsData.getColorWrapper().getAlpha());
                radioButtonDry.setChecked(settingsData.isDry());
                radioButtonWet.setChecked(!settingsData.isDry());
                colorCircle.setColorFilter(settingsData.getColorWrapper().getColor());
            }
        });

        // Set listeners for all elements
        seekBarInkOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float result = progress / 255f;

                if(progress > 0) {
                    settingsData.getColorWrapper().setAlpha(progress);
                    colorCircle.setColorFilter(settingsData.getColorWrapper().getColor());
                    inkOpacitySubtitle.setText(String.format("%.2f", result));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        inkFluidity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonDry) {
                    settingsData.setIsDry(true);
                } else if (checkedId == R.id.radioButtonWet) {
                    settingsData.setIsDry(false);
                }
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ColorPicker cp = new ColorPicker(getActivity(), settingsData.getColorWrapper().getRed(),
                        settingsData.getColorWrapper().getGreen(), settingsData.getColorWrapper().getBlue());

                cp.show();

                Button b = (Button) cp.findViewById(R.id.okColorButton);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsData.getColorWrapper().setColorWithoutAlpha(cp.getColor());
                        colorCircle.setColorFilter(settingsData.getColorWrapper().getColor());
                        cp.dismiss();
                    }
                });
            }
        };

        buttonSelectColor.setOnClickListener(onClickListener);
        colorCircle.setOnClickListener(onClickListener);

        return alertDialog;
    }
}