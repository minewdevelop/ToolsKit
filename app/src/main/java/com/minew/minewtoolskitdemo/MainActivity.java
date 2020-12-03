package com.minew.minewtoolskitdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.minewtech.minewtoolsKit.bean.DeviceInfo;
import com.minewtech.minewtoolsKit.bean.LockModule;
import com.minewtech.minewtoolsKit.enums.BluetoothState;
import com.minewtech.minewtoolsKit.enums.ConnectionState;
import com.minewtech.minewtoolsKit.enums.OperationType;
import com.minewtech.minewtoolsKit.interfaces.outside.OnConnStateListener;
import com.minewtech.minewtoolsKit.interfaces.outside.OnFirmwareUpgradeListener;
import com.minewtech.minewtoolsKit.interfaces.outside.OnScanLockResultListener;
import com.minewtech.minewtoolsKit.manager.MinewLockCenterManager;
import com.minewtech.minewtoolsKit.utils.BLETool;

import java.util.ArrayList;
import java.util.List;

import static com.minewtech.minewtoolsKit.enums.ConnectionState.DeviceLinkStatus_OTA;

public class MainActivity extends AppCompatActivity implements OnScanLockResultListener, View.OnClickListener, OnConnStateListener {

    private boolean isOpenBle = false;
    private PermissionManager mPermissionManager;
    Handler mHandler = new Handler();
    private MinewLockCenterManager mLockCenterManager;
    private MinewLockCenterManager mCenterManager;
    public static List<LockModule> scanLockList = new ArrayList<>();//用来存放扫描到的数据
    private View tvOpenLock;
    private View tvReadInfo;
    private View tvOta;
    private EditText etMac;
    private boolean sendOtaData = true;//是否可以发送 ota数据
    private String tag = "tag";
    private String tag_opera = "opera_tag";
    private KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissionManager = PermissionManager.newInstance();
        initView();
        initListener();
        initPermission();
        checkBluetooth();

    }

    private void initView() {
        etMac = findViewById(R.id.et_mac);
        tvOpenLock = findViewById(R.id.tv_open_lock);
        tvReadInfo = findViewById(R.id.tv_read_info);
        tvOta = findViewById(R.id.tv_ota);
        hud = KProgressHUD.create(this);
    }

    private void initListener() {
        mLockCenterManager = MinewLockCenterManager.getInstance(this);
        tvOpenLock.setOnClickListener(this);
        tvReadInfo.setOnClickListener(this);
        tvOta.setOnClickListener(this);


    }

    private void checkBluetooth() {
        BluetoothState bluetoothState = BLETool.checkBluetooth(this);
        switch (bluetoothState) {
            case BLE_NOT_SUPPORT:
                isOpenBle = false;
                Toast.makeText(this, "Not Support BLE", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BLUETOOTH_OFF:
                isOpenBle = false;
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, 4);
                break;
            case BLUETOOTH_ON:
                isOpenBle = true;
                mPermissionManager.requireMultiPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        110, R.string.permission_location);
                break;
            default:
                break;
        }
    }

    private void initPermission() {
        mCenterManager = MinewLockCenterManager.getInstance(this);

        mPermissionManager.setPermissionInfoListener(new PermissionManager.PermissionInfoListener() {
            @Override
            public void grantPermissions(int requestCode, @NonNull String[] permissions) {

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isOpenBle) {
                            Log.e(tag, "grantPermissions");
                            startScan();
                        }
                    }
                }, 50);
            }

            @Override
            public void refusePermissions(int requestCode, String[] successPermissions, String[] refusePermissions) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                }
                //如果已经有定位权限，就可以直接扫描
                if (mPermissionManager.hasPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        && mPermissionManager.hasPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isOpenBle) {
                                Log.e(tag, "hasPermission");
                                startScan();
                            }
                        }
                    }, 50);
                } else {
                    final CommonDialogFragment dialog = CommonDialogFragment.newInstance(getString(R.string.permission_location));
                    dialog.setOnSelectCallback(new OnSelectCallback() {
                        @Override
                        public void onPositive(String msg) {
                            mPermissionManager.requirePermissionAgain();
                            Log.e(tag, "mPermissionManager.requirePermissionAgain() onPositive");
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegative() {
                            Log.e(tag, "mPermissionManager.requirePermissionAgain() onNegative");
                            finish();
                            dialog.dismiss();
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "require_permission");
                }
            }

            @Override
            public void refusePermissionsAndNotAsk(int requestCode, String[] successPermissions,
                                                   String[] refusePermissions, String[] refuseNoAskPermissions) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                }
                //如果已经有定位权限，就可以直接扫描
                if (mPermissionManager.hasPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        && mPermissionManager.hasPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isOpenBle) {
                                startScan();
                            }
                        }
                    }, 50);
                } else {
                    final CommonDialogFragment dialog = CommonDialogFragment.newInstance("需要定位权限");
                    dialog.setOnSelectCallback(new OnSelectCallback() {
                        @Override
                        public void onPositive(String msg) {
                            Log.e(tag, "onPositive ");
                            mPermissionManager.goToAppSettingActivity(111);
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegative() {
                            Log.e(tag, "onNegative ");
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "require_permission");
                }
            }
        });
    }

    //蓝牙扫描时间持续 90 秒
    public void startScan() {
        Log.e("startScan", "startScan");
        mLockCenterManager.getScanLockManager().startScan(this);
    }

    @Override
    public void onScanLockResult(List<LockModule> list) {
        //此处获取扫描到的数据，连接蓝牙
        this.scanLockList.clear();
        this.scanLockList.addAll(list);
        Log.e(tag, "MainActivity " + scanLockList.size());

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_open_lock:
                operaDevice(1);
                break;
            case R.id.tv_read_info:
                operaDevice(2);
                break;
            case R.id.tv_ota:
                operaDevice(3);
                break;
            default:
                break;
        }
    }

    private void operaDevice(int type) {
        String mac = etMac.getText().toString().trim();
        String replaceMac = mac.replaceAll(":", "");
        boolean isHaveDevice = false;
        LockModule lockModule = null;
        for (LockModule lock_module : scanLockList) {
            if (lock_module.getMacAddress().equalsIgnoreCase(mac)
                    || lock_module.getMacAddress().replaceAll(":", "").equalsIgnoreCase(replaceMac)) {
                lockModule = lock_module;
                isHaveDevice = true;
                break;
            }
        }
        if (!isHaveDevice) {
            Toast.makeText(this, "没有扫描到这个设备", Toast.LENGTH_SHORT).show();
            return;
        }
        if (type == 1) {
            mCenterManager.getConnLockManager().connect(this, lockModule, OperationType.OPERA_OPEN_LOCK, this);
            mCenterManager.getConnLockManager().initOperaState();
        } else if (type == 2) {
            mCenterManager.getConnLockManager().connect(this, lockModule, OperationType.OPERA_READ_DEVICE, this);
            mCenterManager.getConnLockManager().initOperaState();
        } else if (type == 3) {
            mCenterManager.getConnLockManager().connect(this, lockModule, OperationType.OPERA_OTA1, this);
            mCenterManager.getConnLockManager().initOperaState();
        }
        hud.show();
        hud.setBackgroundColor(R.color.offlineColor);
    }

    /**
     * @param macAddress      设备mac
     * @param connectionState 连接状态
     * @param operaState      操作状态
     * @param operaResult     操作是否成功
     * @param result          提示语
     */
    @Override
    public void onOperaLockState(final String macAddress, ConnectionState connectionState, final ConnectionState operaState, final boolean operaResult, final String result) {
        if (connectionState == ConnectionState.DeviceLinkStatus_Disconnect) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //opera_time_out
                    if (operaState == ConnectionState.DeviceLinkStatus_Time_Out) {
                        hud.dismiss();
                        if ("OPERA_READ_DEVICE".equals(result)) {
                            Log.e(tag_opera, "读取设备信息失败，超时");
                        } else if ("OPERA_OTA".equals(result)) {
                            Log.e(tag_opera, "固件升级失败，超时");
                            sendOtaData = false;
                        } else if ("OPERA_OPEN_LOCK".equals(result)) {
                            Log.e(tag_opera, "开锁失败，超时");
                        }
                    } else if (operaState == ConnectionState.DeviceLinkStatus_OpenLock) {
                        hud.dismiss();
                        if (operaResult) {
                            Log.e(tag_opera, "开锁成功");
                        } else {
                            Log.e(tag_opera, "开锁失败");
                        }
                    } else if (operaState == ConnectionState.DeviceLinkStatus_Read_Device_Info) {
//                        hud.dismiss();
//                          读取设备信息的返回信息放在 onReadLockInfo(DeviceInfo deviceInfo) 方法
                    } else if (operaState == DeviceLinkStatus_OTA) {
//                         hud.dismiss();

                        sendOtaData = true;
                    }
                }
            });
        } else if (connectionState == ConnectionState.DeviceLinkStatus_Connected) {
            if (operaState == DeviceLinkStatus_OTA) {
                hud.dismiss();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("request_ota", "requestCode " + " :getUpgradePackage");

                        if (sendOtaData) {
                            sendOtaData = false;
                            /*
                             * 发送固件升级包
                             * @param data 字节数组是从手机内存中选择文件，转成字节数组后传入手机，此处随意创建了数组
                             * @param macAddress，需要升级设备的 mac地址
                             */
                            byte[] data = new byte[]{};
                            sendUpgradePackage(data, macAddress);
                        }
                    }
                }, 100);
            }
        }
    }

    //deviceState 0 未开锁状态;1 已开锁; 2 故障
    @Override
    public void onReadLockInfo(final DeviceInfo deviceInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hud.dismiss();
                if (deviceInfo.isReadState()) {
                    Toast.makeText(MainActivity.this,
                            "devicName：" + deviceInfo.getDeviceName()
                                    + "mac:" + deviceInfo.getAddress()
                                    + "version:" + deviceInfo.getVersion()
                                    + "deviceState:" + deviceInfo.getDeviceState()
                            , Toast.LENGTH_SHORT).show();
                    Log.e("ConnLockManager", "devicName：" + deviceInfo.getDeviceName()
                            + "mac:" + deviceInfo.getAddress()
                            + "version:" + deviceInfo.getVersion()
                            + "deviceState:" + deviceInfo.getDeviceState());
                } else {
                    Toast.makeText(MainActivity.this,
                            "读取设备信息失败",
                            Toast.LENGTH_SHORT).show();
                    Log.e("ConnLockManager", "读取设备信息失败");
                }
            }
        });

    }

    private void sendUpgradePackage(final byte[] data, final String macAddress) {
        Log.e(tag, "sendUpgradePackage");
//        hud.dismiss();
//        showOtaDialog();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCenterManager.getConnLockManager().firmwareUpgrade(macAddress, data, OperationType.OPERA_OTA2,
                        new OnFirmwareUpgradeListener() {
                            @Override
                            public void startUpgrade() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }

                            @Override
                            public void updateProgress(final int index) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(tag, "升级中 " + index + "%");
                                    }
                                });

                            }

                            @Override
                            public void upgradeSuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(tag, "升级成功 ");
                                    }
                                });
                                mCenterManager.getConnLockManager().disConnect(macAddress);
                                sendOtaData = false;
                            }

                            @Override
                            public void upgradeFailed() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(tag, "升级失败 ");
                                        sendOtaData = false;
                                    }
                                });
                            }
                        });
            }
        });
    }

}
