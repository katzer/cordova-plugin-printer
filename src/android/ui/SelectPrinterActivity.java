/*
    Copyright 2013-2016 appPlant GmbH

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */

package de.appplant.cordova.plugin.printer.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.appplant.cordova.plugin.printer.ext.PrintManager;
import de.appplant.cordova.plugin.printer.ext.PrinterDiscoverySession;
import de.appplant.cordova.plugin.printer.ext.PrinterDiscoverySession.OnPrintersChangeListener;
import de.appplant.cordova.plugin.printer.reflect.Meta;

@SuppressLint("SetTextI18n")
public final class SelectPrinterActivity extends Activity {

    /**
     * The extra string to identify the data from the intents bundle.
     */
    public static final String EXTRA_PRINTER_ID =
            "INTENT_EXTRA_PRINTER_ID";

    /**
     * The action of the intent.
     */
    public static final String ACTION_SELECT_PRINTER =
            "ACTION_SELECT_PRINTER";

    /**
     * Reference to the main view which lists all discovered printers.
     */
    private ListView listView;

    /**
     * Session for printer discovering within the network.
     */
    private PrinterDiscoverySession session;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized
     *                           after previously being shut down then this
     *                           Bundle contains the data it most recently
     *                           supplied in.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(Meta.getResId(
                this, "layout", "select_printer_activity"));

        session  = new PrintManager(this).createPrinterDiscoverySession();
        listView = (ListView) findViewById(android.R.id.list);

        initListView();
        startPrinterDiscovery();
        updateEmptyView();
    }

    /**
     * Perform any final cleanup before an activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        session.destroy();
        super.onDestroy();
    }

    /**
     * Sent intent with empty result.
     */
    @Override
    public void onBackPressed() {
        onPrinterSelected(null);
        super.onBackPressed();
    }

    /**
     * Sends an intent with the specified printer back to the owning activity
     * and finishes that activity.
     *
     * @param printerId The printerId object of the printer containing
     *                  everything necessary to identify and contact him.
     */
    private void onPrinterSelected (PrinterId printerId) {
        Intent intent = new Intent();

        intent.putExtra(EXTRA_PRINTER_ID, printerId);
        intent.setAction(ACTION_SELECT_PRINTER);
        setResult(RESULT_OK, intent);

        finish();
    }

    /**
     * Assigns the adapter and the click listener to the list.
     */
    private void initListView() {
        listView.setAdapter(new ListViewAdapter());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listView.getAdapter().isEnabled(position)) {
                    return;
                }

                PrinterInfo printer = (PrinterInfo)
                        listView.getAdapter().getItem(position);

                onPrinterSelected(printer.getId());
            }
        });
    }

    /**
     * Start printer discovery if there are installed services found on the
     * device.
     */
    private void startPrinterDiscovery() {
        PrintManager pm = new PrintManager(this);

        if (pm.getInstalledPrintServices().isEmpty())
            return;

        session.startPrinterDiscovery();
    }

    /**
     * Shows or hides the empty-view of the list view depend on if the
     * adapter contains any printers or not.
     */
    private void updateEmptyView() {
        TextView titleView = (TextView) findViewById(android.R.id.title);
        View progressBar   = findViewById(android.R.id.progress);

        if (listView.getEmptyView() == null) {
            View emptyView = findViewById(android.R.id.empty);
            listView.setEmptyView(emptyView);
        }

        if (session.isPrinterDiscoveryStarted()) {
            titleView.setText("Searching for printers");
            progressBar.setVisibility(View.VISIBLE);
        } else {
            titleView.setText("No printers found");
            progressBar.setVisibility(View.GONE);
        }
    }

    private final class ListViewAdapter extends BaseAdapter {

        /**
         * Wait lock for synchronization.
         */
        private final Object lock = new Object();

        /**
         * List of all received printer to display in the list.
         */
        private final List<PrinterInfo> printers = new ArrayList<PrinterInfo>();

        /**
         * Constructor registers a listener to monitor about newly discovered
         * printers.
         */
        ListViewAdapter() {
            session.setOnPrintersChangeListener(new OnPrintersChangeListener() {
                @Override
                public void onPrintersChanged (List<PrinterInfo> printerInfos) {
                    printers.addAll(printerInfos);
                    notifyDataSetChanged();
                }
            });
        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            synchronized (lock) {
                return printers.size();
            }
        }

        /**
         * Get the data item associated with the specified position in the
         * data set.
         *
         * @param position Position of the item whose data we want within the
         *                 adapter's data set.
         *
         * @return The data at the specified position.
         */
        @Override
        public Object getItem (int position) {
            synchronized (lock) {
                return printers.get(position);
            }
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data
         *                 set whose row id we want.
         *
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId (int position) {
            return position;
        }

        /**
         * Gets a View that displays in the drop down popup the data at the
         * specified position in the data set.
         *
         * @param pos The index of the item whose view we want.
         * @param view The old view to reuse, if possible. Note: You should
         *             check that this view is non-null and of an appropriate
         *             type before using. If it is not possible to convert
         *             this view to display the correct data.
         * @param parent The parent that this view will eventually be
         *               attached to.
         *
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getDropDownView (int pos, View view, ViewGroup parent) {
            return getView(pos, view, parent);
        }

        /**
         * Get a View that displays the data at the specified position in the
         * data set.
         *
         * @param pos The position of the item within the adapter's data set
         *            of the item whose view we want.
         * @param view The old view to reuse, if possible. Note: You should
         *             check that this view is non-null and of an appropriate
         *             type before using. If it is not possible to convert
         *             this view to display the correct data.
         * @param parent The parent that this view will eventually be
         *               attached to.
         *
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView (int pos, View view, ViewGroup parent) {
            PrinterInfo printer   = (PrinterInfo) getItem(pos);
            CharSequence title    = printer.getName();
            CharSequence subtitle = null;
            Drawable icon         = null;

            if (view == null) {
                view = getLayoutInflater().inflate(
                        Meta.getResId(getApplicationContext(), "layout", "printer_list_item"),
                        parent, false);
            }

            try {
                PackageManager pm = getPackageManager();
                PrinterId pId  = printer.getId();
                Object cmpName = Meta.invokeMethod(pId, "getServiceName");
                Object pkgName = Meta.invokeMethod(cmpName, "getPackageName");

                PackageInfo packageInfo = pm.getPackageInfo(
                        (String) pkgName, 0);

                subtitle = packageInfo.applicationInfo.loadLabel(pm);
                icon     = packageInfo.applicationInfo.loadIcon(pm);
            } catch (NameNotFoundException e) {
                /* ignore */
            }

            TextView titleView = (TextView) view.findViewById(android.R.id.title);
            titleView.setText(title);

            TextView subtitleView = (TextView) view.findViewById(android.R.id.hint);
            if (!TextUtils.isEmpty(subtitle)) {
                subtitleView.setText(subtitle);
                subtitleView.setVisibility(View.VISIBLE);
            } else {
                subtitleView.setText(null);
                subtitleView.setVisibility(View.GONE);
            }

            ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
            if (icon != null) {
                iconView.setImageDrawable(icon);
                iconView.setVisibility(View.VISIBLE);
            } else {
                iconView.setVisibility(View.GONE);
            }

            return view;
        }

        /**
         * Indicate whether the specified printer by position is available.
         *
         * @param position The position of the printer in the list.
         *
         * @return A truthy value means that the printer is available.
         */
        @Override
        public boolean isEnabled (int position) {
            PrinterInfo printer =  (PrinterInfo) getItem(position);
            return printer.getStatus() != PrinterInfo.STATUS_UNAVAILABLE;
        }
    }
}
