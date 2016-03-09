package co.adrianblan.fastbrush.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
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

    @Bind(R.id.editTextBrushBristles)
    EditText editTextBrushBristles;

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
        settingsData = settingsManager.getSettingsData();

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
                editTextBrushBristles.setText(String.valueOf(settingsData.getNumBristles()));
                seekBarBristleThickness.setProgress((int) (settingsData.getBristleThickness() * 100));
                showBrushView.setChecked(settingsData.isShowBrushView());
            }
        });

        // Set listeners for all elements
        seekBarBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float result = progress / 100f;

                if(progress > 0) {
                    settingsData.setSize(result);
                    brushSizeSubtitle.setText(String.valueOf(result));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarBrushSizePressureFactor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float result = progress / 100f;

                settingsData.setPressureFactor(result);
                brushSizePressureFactorSubtitle.setText(String.valueOf(result));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        editTextBrushBristles.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    int result = Integer.parseInt(s.toString());
                    settingsData.setNumBristles(result);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
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

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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