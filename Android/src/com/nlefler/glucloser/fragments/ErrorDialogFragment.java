package com.nlefler.glucloser.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Created by lefler on 11/23/13.
 */
public class ErrorDialogFragment  extends DialogFragment {
    private Dialog _dialog;

    public void setDialog(Dialog dialog) {
        _dialog = dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedIntanceState) {
        return _dialog;
    }
}
