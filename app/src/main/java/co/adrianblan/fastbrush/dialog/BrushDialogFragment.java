package co.adrianblan.fastbrush.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.adrianblan.fastbrush.R;
import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.settings.SettingsManager;

/**
 * Fragment for showing a brush dialog.
 */
public class BrushDialogFragment extends DialogFragment {

    @Bind(R.id.seekBarBrushSize)
    SeekBar seekBarBrushSize;

    @Bind(R.id.brushSizeSubtitle)
    TextView brushSizeSubtitle;

    @Bind(R.id.seekBarBrushSizePressureFactor)
    SeekBar seekBarBrushSizePressureFactor;

    @Bind(R.id.brushSizePressureFactorSubtitle)
    TextView brushSizePressureFactorSubtitle;

    @Bind(R.id.seekBarBristleAmount)
    SeekBar seekBarBristleAmount;

    @Bind(R.id.bristleAmountSubtitle)
    TextView bristleAmountSubtitle;

    @Bind(R.id.seekBarBristleThickness)
    SeekBar seekBarBristleThickness;

    @Bind(R.id.bristleThicknessSubtitle)
    TextView bristleThickessSubtitle;

    @Bind(R.id.showBrushView)
    CheckedTextView showBrushView;

    private SettingsManager settingsManager;
    private SettingsData settingsData;
    private View mainView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        settingsManager = SettingsManager.getInstance(getActivity());
        settingsData = settingsManager.getSettingsData().clone();

        mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_brush, null);
        ButterKnife.bind(this, mainView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Brush Settings")
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
                seekBarBrushSize.setProgress((int) (settingsData.getSize() * 100));
                seekBarBrushSizePressureFactor.setProgress((int) (settingsData.getPressureFactor() * 100));
                seekBarBristleAmount.setProgress(settingsData.getNumBristles());
                seekBarBristleThickness.setProgress((int) (settingsData.getBristleThickness() * 100));
                showBrushView.setChecked(settingsData.isShowBrushView());
            }
        });

        // Set listeners for all elements
        seekBarBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float result = progress / 100f;

                settingsData.setSize(result);
                brushSizeSubtitle.setText(String.valueOf(result));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarBrushSizePressureFactor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float result = progress / 100f;

                settingsData.setPressureFactor(result);
                brushSizePressureFactorSubtitle.setText(String.valueOf(result));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarBristleAmount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int result = Math.round(progress / 10) * 10;

                if (result > 0) {
                    settingsData.setNumBristles(result);
                    bristleAmountSubtitle.setText(String.valueOf(result));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarBristleThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float result = progress / 100f;

                if (progress > 0) {
                    settingsData.setBristleThickness(result);
                    bristleThickessSubtitle.setText(String.valueOf(result));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        showBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBrushView.toggle();
                settingsData.setShowBrushView(showBrushView.isChecked());
            }
        });

        return alertDialog;
    }
}