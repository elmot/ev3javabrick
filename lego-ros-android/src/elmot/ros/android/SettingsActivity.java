package elmot.ros.android;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Bundle;
import android.preference.*;

/**
 * @author elmot
 *         Date: 09.09.14
 */
public class SettingsActivity extends PreferenceActivity {

    public static final String BLUETOOTH_DEVICE_KEY = "bluetooth_device";
    private BroadcastReceiver pickerReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_preference);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(newValue.toString());
                return true;
            }
        };
        setListener(preferenceScreen, changeListener);

        final Preference btConnect = preferenceScreen.findPreference(BLUETOOTH_DEVICE_KEY);
        setBtSummary(btConnect, Settings.getPreferences(this).getString(BLUETOOTH_DEVICE_KEY, null));
        pickerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BluetoothDevice device = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                SharedPreferences sharedPreferences = Settings.getPreferences(SettingsActivity.this);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                try {
                    edit.putString(BLUETOOTH_DEVICE_KEY, device.getAddress());
                    setBtSummary(btConnect, device.getAddress());
                } finally {
                    edit.commit();
                }
            }

        };
        registerReceiver(pickerReceiver, new IntentFilter("android.bluetooth.devicepicker.action.DEVICE_SELECTED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pickerReceiver);
    }

    private void setBtSummary(Preference btConnect, String address) {
        String name;
        if (address == null) {
            name = getString(R.string.NoneBluetooth);
        } else {
            BluetoothDevice remoteDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            name = remoteDevice.getName();
            name = name == null ? address : (name + "(" + address + ")");
        }
        btConnect.setSummary(name);
    }

    private void setListener(PreferenceGroup preferenceGroup, Preference.OnPreferenceChangeListener editTextChangeListener) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference instanceof EditTextPreference) {
                preference.setOnPreferenceChangeListener(editTextChangeListener);
                preference.setSummary(((EditTextPreference) preference).getText());
            }
            if (preference instanceof PreferenceGroup) {
                setListener((PreferenceGroup) preference, editTextChangeListener);
            }
        }
    }
}