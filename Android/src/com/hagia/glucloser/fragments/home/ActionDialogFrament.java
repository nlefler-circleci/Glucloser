package com.hagia.glucloser.fragments.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class ActionDialogFrament extends DialogFragment {
	private CharSequence[] actions;
	private OnClickListener clickListener;

	public ActionDialogFrament(CharSequence[] availableActions,
			OnClickListener listener) {
		actions = availableActions;
		clickListener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setItems(actions, clickListener);	

		return builder.create();
	}
}
