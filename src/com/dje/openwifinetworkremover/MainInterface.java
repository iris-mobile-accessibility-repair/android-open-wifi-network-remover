/*
 * This file is part of 'Open Wifi Network Remover'
 * 
 * Copyright 2013 Duncan Eastoe <duncaneastoe@gmail.com>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package com.dje.openwifinetworkremover;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

public class MainInterface extends ListActivity {
	
	private ArrayList<String> whitelistedSSIDS;
	private ArrayAdapter<String> whitelistAdapter;
	
	private Settings settings;
	private UiGoodies uiGoodies;
	
	// Interface components
	private CheckBox enabledCheckBox;
	private CheckBox notificationCheckBox;
	private ListView whitelist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_interface);
		
		settings = new Settings(this);
		uiGoodies = new UiGoodies(this);
		whitelistedSSIDS = new ArrayList<String>();
		
		whitelistAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, whitelistedSSIDS);
		setListAdapter(whitelistAdapter);
		
		enabledCheckBox = (CheckBox) findViewById(R.id.enabled_checkbox);
		notificationCheckBox = (CheckBox) findViewById(R.id.notification_checkbox);
		whitelist = (ListView) findViewById(android.R.id.list);
		
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			findViewById(R.id.whitelist_header_layout).setVisibility(View.INVISIBLE);
			findViewById(android.R.id.list).setVisibility(View.INVISIBLE);
		}
		
		updateUI();
	}

	// Performs updates to the UI after major events
	public void updateUI() {
		// Set enabled checkbox
		if (settings.get("enabled") == 1)
			enabledCheckBox.setChecked(true);
		else
			enabledCheckBox.setChecked(false);
		
		// Set notifications checkbox
		if (settings.get("notifications") == 1)
			notificationCheckBox.setChecked(true);
		else
			notificationCheckBox.setChecked(false);
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			// Clear list and re-load it from stored whitelist
			for (int i = 0; i < whitelist.getCount(); i++)
				whitelist.setItemChecked(i, false);
			whitelistedSSIDS.clear();
			whitelistedSSIDS.addAll(settings.getList("whitelist"));
			whitelistAdapter.notifyDataSetChanged();
			
			// Show/hide SSID remove button according to whitelist length
			if (whitelistedSSIDS.size() <= 0)
				findViewById(R.id.whitelistRemoveButton).setVisibility(View.INVISIBLE);
			else
				findViewById(R.id.whitelistRemoveButton).setVisibility(View.VISIBLE);
		}
	}
	
	// Handle a click on a checkbox
	public void checkBoxClick(View view) {
		boolean checked = ((CheckBox) view).isChecked();
		
		if (view.getId() == R.id.enabled_checkbox) {
			if (checked)
				settings.set("enabled", 1);
			else
				settings.set("enabled", 0);
		}
		if (view.getId() == R.id.notification_checkbox) {
			if (checked)
				settings.set("notifications", 1);
			else
				settings.set("notifications", 0);
		}
	}

	// Pops up a dialog box prompting for an SSID and adds it to the whitelist
	public void whitelistAddHandler(View view) {	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Enter SSID");
		final EditText edit = new EditText(this);
		builder.setView(edit);
		final String alreadyInWhitelistMessage = this.getString(R.string.ssid_already_in_whitelist);
			
		// Handle click on the Ok button
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String ssidInput = edit.getText().toString();
				if (ssidInput.length() == 0) {
					if (whitelistedSSIDS.contains(ssidInput)) {
						uiGoodies.displayToastNotification(alreadyInWhitelistMessage);
					}
					else {
						whitelistedSSIDS.add(edit.getText().toString());
						settings.set("whitelist", whitelistedSSIDS);
						updateUI();
					}
				}
			}
		});
		
		// Handle click on the Cancel button
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {}
		});

		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}
	
	// Removes the selected SSID(s) from the whitelist
	public void whitelistRemoveHandler(View view) {
		SparseBooleanArray checkedIds = whitelist.getCheckedItemPositions();
		
		int removedCount = 0;
		for (int i = 0; i < checkedIds.size(); i++) {
			if (checkedIds.get(i) == true) {
				whitelistedSSIDS.remove(i-removedCount); // Fix shifted indexes
				removedCount++;
			}
		}
		
		if (removedCount == 0)
			uiGoodies.displayToastNotification(this.getString(R.string.no_ssid_selected));
		
		settings.set("whitelist", whitelistedSSIDS);
		updateUI();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main_interface, menu);
		return true;
	}

}
