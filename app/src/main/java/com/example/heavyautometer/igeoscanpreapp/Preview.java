package com.example.heavyautometer.igeoscanpreapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.hoho.android.usbserial.hardcopy.Constants;
import com.hoho.android.usbserial.hardcopy.SerialConnector;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.UVCCameraHandler;
import com.serenegiant.usbcameratest.CameraDialog;
import com.serenegiant.widget.UVCCameraTextureView;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.joystick_and_buttons.Movable_Layout_Class;



public class Preview extends Activity implements OnClickListener {

/***************************************카메라 관련**************************************************************************************/

    private Button connect;
    private USBMonitor mUSBMonitor;
    private UVCCameraHandler mUVCHandler;
    private UVCCameraTextureView mUVCCameraView;
    private LinearLayout expand_camera_layout;

/***************************************시리얼 연결 관련**************************************************************************************/


    private Button serial_connect;
//    private FTDriver mSerial;
//    private UsbReceiver mUsbReceiver;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final String ACTION_USB_PERMISSION = "kr.co.andante.mobiledgs.USB_PERMISSION";
    private String TAG = "HDJ";
    private Boolean SHOW_DEBUG = false;
    private int mBaudrate;
    private int select_mode = 0;




    private ActivityHandler mHandler = null;

    private SerialListener mListener = null;
    private SerialConnector mSerialConn = null;


    private TextView mTextLog = null;
    private TextView mTextInfo = null;




/*************************************표시뷰 관련************************************************************************************/

    private TextView dataview_1_img_spota_check;
    private TextView dataview_1_img_spotb_check;

    private ImageView cameraview_1_aim;
    private ImageView cameraview_2_aim;

    /* 사용자설명서 버튼 */
    private Button btn_help;

    /* 데이터뷰(전체레이아웃 왼쪽) - 카메라확대 관련 버튼 */
    private ImageButton back;

    /* 데이터뷰 - 카메라캡처 관련 버튼, 뷰 */
    private Button btn_dataview_1_img_spota;
    private Button btn_dataview_1_img_spotb;
    private ImageView dataview_1_imgView_spota;
    private ImageView dataview_1_imgView_spotb;


/*************************************데이터뷰 관련************************************************************************************/

    /* 카메라뷰 - 현재 스팟표시 및 거리,높이 표시 */
    private TextView cameraview_1_spot_point;
    private TextView cameraview_4_distance;
    private TextView cameraview_4_height;

    /* 데이터뷰 - 아두이노에서 받아오는 값 저장하는 객체변수 */
    private ArduinoData aData;
    private ArduinoData bData;

    /* 데이터뷰 - 각각데이터(거리/높이/기울기)뷰 */
    private TextView dataview_2_each_data_spota_distance;
    private TextView dataview_2_each_data_spota_height;
    private TextView dataview_2_each_data_spota_gradient;
    private TextView dataview_2_each_data_spotb_distance;
    private TextView dataview_2_each_data_spotb_height;
    private TextView dataview_2_each_data_spotb_gradient;

    /* 데이터뷰 - 합산데이터(거리/높이/기울기)뷰 */
    private TextView dataview_3_compare_data_distance;
    private TextView dataview_3_compare_data_height;
    private TextView dataview_3_compare_data_gradient;

    private RelativeLayout cameralayout;
    private FrameLayout defaultCameraSection;
    private FrameLayout extCameraSection;
    /* 확장 카메라 데이터뷰 */
    private TextView extend_point;
    private TextView extend_distance;
    private TextView extend_height;


    private final static int CAMERA_DELAY = 950;


    /* 시리얼 데이터 처리 변수 */
    private boolean is_appending = false; //시작 문자 '!' 가 들어오면 true, 끝문자가 들어오면 다시 false
    private String appended_receive_data = ""; //조금씩 끊어져 들어오는 데이터를 모아서 완성시키는 String



    /***************************** 모터 이동 버튼*******************/
    private ViewGroup mainLayout;
    private ViewGroup direction_arrow_frame;
    Movable_Layout_Class direction_arrow_frame_moving;

    private ImageButton left_up;
    private ImageButton middle_up;
    private ImageButton right_up;

    private ImageButton left;
    private ImageButton middle;
    private ImageButton right;

    private ImageButton left_down;
    private ImageButton middle_down;
    private ImageButton right_down;

    /******************** 데이터 표시뷰 플로팅 ****************/
    private ViewGroup data_viewing_area;
    Movable_Layout_Class data_viewing_area_moving;
    private TextView standard_height;
    private TextView comparison_height;
    private TextView height_difference;
    private boolean is_starting_measure= false;
    private boolean execute_standard_value_saving = false;
    private int[] standard_value = new int[6];

    /*****************카메라 확대 버튼 플로팅 ***************/

    private int is_camera_extended = 0;
    private ViewGroup camera_extend_button_area;
    Movable_Layout_Class camera_extend_button_moving;

    /*************UI 스케일 조정값 저장 ****************/
    SharedPreferences UI_scale_size_value;
    SharedPreferences.Editor UI_scale_size_value_editor;
    private float direction_arrow_frame_current_scale_size;
    private float data_viewing_area_current_scale;
    private float camera_current_scale_size;
    private Button direction_arrow_frame_scale_size_btn;
    private Button data_viewing_area_scale_size_btn;
    private ImageButton camera_scale_size_up_btn;
    private ImageButton camera_scale_size_down_btn;

    boolean direction_arrow_frame_scale_up_down = true;
    boolean data_viewing_area_scale_up_down = true;




    /************* 나머지 설정 버튼 관련****************/

    /* 보정 계수 관련 */
    private Button correction_value_btn;
    private int first_value;
    private float correction_value = 0.0f;
    private boolean is_starting_finding_correction_value = false;



    private Button move_original_button_location; //버튼 위치 안보일 때
    private CheckBox move_button_hold; //버튼 위치 고정

    private ImageButton setting_button; //세팅 메뉴 보이기/안보이기





    /*************************************************************************************************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);


/* 플로팅을 위한 베이스 레이아웃 */
        mainLayout = (FrameLayout) findViewById(R.id.main);





        /*********모터 방향 버튼 관련 **********/

        String[] direction_arrow_location = new String[2];
        direction_arrow_location[0] = "direction_arrow_location_x";
        direction_arrow_location[1] = "direction_arrow_location_y";
        String direction_arrow_scale = "direction_arrow_scale";
        direction_arrow_frame = (FrameLayout) findViewById(R.id.direction_arrow_frame);
        direction_arrow_frame_moving = new Movable_Layout_Class(this, mainLayout, direction_arrow_frame, direction_arrow_location, direction_arrow_scale);
        direction_arrow_frame_current_scale_size = direction_arrow_frame_moving.Saved_scale_size();



        left_up = (ImageButton) findViewById(R.id.left_up);
        middle_up = (ImageButton) findViewById(R.id.middle_up);
        right_up = (ImageButton) findViewById(R.id.right_up);

        left = (ImageButton) findViewById(R.id.left);
        middle = (ImageButton) findViewById(R.id.middle);
        right = (ImageButton) findViewById(R.id.right);

        left_down = (ImageButton) findViewById(R.id.left_down);
        middle_down = (ImageButton) findViewById(R.id.middle_down);
        right_down = (ImageButton) findViewById(R.id.right_down);

        left_up.setOnTouchListener(arrow_button);
        middle_up.setOnTouchListener(arrow_button);
        right_up.setOnTouchListener(arrow_button);
        left.setOnTouchListener(arrow_button);

        right.setOnTouchListener(arrow_button);
        left_down.setOnTouchListener(arrow_button);
        middle_down.setOnTouchListener(arrow_button);
        right_down.setOnTouchListener(arrow_button);

        middle.setOnClickListener(this);


/*************카메라 확대 버튼 **********/
        String[] cemera_extend_button_location = new String[2];
        cemera_extend_button_location[0] = "cemera_extend_button_location_x";
        cemera_extend_button_location[1] = "cemera_extend_button_location_y";
        camera_extend_button_area = (FrameLayout) findViewById(R.id.camera_extend_button_area);
        camera_extend_button_moving = new Movable_Layout_Class(this, mainLayout, camera_extend_button_area, cemera_extend_button_location);
        camera_scale_size_up_btn = (ImageButton)findViewById(R.id.camera_scale_size_up_btn);
        camera_scale_size_up_btn.setOnClickListener(this);




 /**********데이터 표시뷰 플로팅 *************/

        String[] data_view_location = new String[2];
        data_view_location[0] = "data_view_location_x";
        data_view_location[1] = "data_view_location_y";
        String data_view_scale = "data_view_scale";
        data_viewing_area = (FrameLayout) findViewById(R.id.data_viewing_area);
        data_viewing_area_moving = new Movable_Layout_Class(this, mainLayout, data_viewing_area, data_view_location, data_view_scale);
        data_viewing_area_current_scale = data_viewing_area_moving.Saved_scale_size();

        standard_height = (TextView) findViewById(R.id.standard_height);
        comparison_height = (TextView) findViewById(R.id.comparison_height);
        height_difference = (TextView) findViewById(R.id.height_difference);






/***************************************카메라 관련**************************************************************************************/

        connect = (Button)findViewById(R.id.connect);
        connect.setOnClickListener(this);

        mUVCHandler = UVCCameraHandler.createHandler(this);
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);  //Preview.java에 UsbMonitor를 추가

        // 2016. 09. 22. 권태성 수정 카메라를 출력하는 레이아웃에 FrameLayout을 하나씩 만들었습니다.
        defaultCameraSection = (FrameLayout)this.findViewById(R.id.default_camera_sec);         // 메인 화면에서 출력되는 카메라 뷰를 넣을 곳
        extCameraSection = (FrameLayout)this.findViewById(R.id.ext_camera_sec);                 // 확대된 카메라 뷰를 넣을 곳

        /* 일반 카메라 모드 */
        mUVCCameraView = (UVCCameraTextureView) findViewById(R.id.UVCCameraTextureView1);
        mUVCCameraView.setSurfaceTextureListener(mSurfaceTextureListener);
        mUVCCameraView.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH/(float)UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mUVCCameraView.setRotation(270);

        cameralayout = (RelativeLayout) findViewById(R.id.camera_layout);

        if (!mUVCHandler.isOpened()) {
            CameraDialog.showDialog(Preview.this);
        } else {
        }


/***************************************시리얼 연결 관련  (수정중)   **************************************************************************************/

        serial_connect = (Button)findViewById(R.id.serial_connect);
        serial_connect.setOnClickListener(this);

//        mSerial = new FTDriver((UsbManager) getSystemService(Context.USB_SERVICE));
//        mUsbReceiver = new UsbReceiver(this, mSerial);

//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(mUsbReceiver, filter);
//        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        mTextLog = (TextView) findViewById(R.id.dataview_2_each_data_spota_distance);
        mTextLog.setMovementMethod(new ScrollingMovementMethod());
        mTextInfo = (TextView) findViewById(R.id.dataview_2_each_data_spota_height);
        mTextInfo.setMovementMethod(new ScrollingMovementMethod());


        // Initialize
        mListener = new SerialListener();
        mHandler = new ActivityHandler();

        // Initialize Serial connector and starts Serial monitoring thread.
        mSerialConn = new SerialConnector(getApplicationContext(), mListener , mHandler);
        mSerialConn.initialize(getApplicationContext());


//        mSerial.setPermissionIntent(permissionIntent);
//        mBaudrate = mUsbReceiver.loadDefaultBaudrate();

//        if (mSerial.begin(mBaudrate)) {
//
//
//            if (SHOW_DEBUG) {
//                Log.d(TAG, "FTDriver began");
//            }
//            mUsbReceiver.loadDefaultSettingValues();
//            mUsbReceiver.mainloop();
//        } else {
//            if (SHOW_DEBUG) {
//                Log.d(TAG, "FTDriver no connection");
//            }
//            Toast.makeText(this, "no connection", Toast.LENGTH_SHORT).show();
//        }

/*************************************표시뷰 관련************************************************************************************/

		/* 카메라뷰(전체 레이아웃 왼쪽) 왼쪽 상단 : 지점 표시를 위한 텍스트뷰, 카메라뷰 중간 : 카메라Aim */
        cameraview_1_spot_point = (TextView)findViewById(R.id.cameraview_1_spot_point);
        cameraview_1_aim = (ImageView)findViewById(R.id.cameraview_1_aim);



        /* 사용자설명서 뷰 */
        btn_help = (Button) findViewById(R.id.btn_help);
        btn_help.setOnClickListener(this);

        /* 사진 캡처 관련 뷰 */
        btn_dataview_1_img_spota = (Button)findViewById(R.id.dataview_1_img_spota);
        btn_dataview_1_img_spota.setOnClickListener(this);
        btn_dataview_1_img_spotb = (Button)findViewById(R.id.dataview_1_img_spotb);
        btn_dataview_1_img_spotb.setOnClickListener(this);
        dataview_1_img_spota_check = (TextView)findViewById(R.id.dataview_1_img_spota_check);
        dataview_1_img_spotb_check = (TextView)findViewById(R.id.dataview_1_img_spotb_check);
        dataview_1_imgView_spota = (ImageView)findViewById(R.id.dataview_1_imgView_spota);
        dataview_1_imgView_spotb = (ImageView)findViewById(R.id.dataview_1_imgView_spotb);

        /* 카메라 확대 관련 뷰 */
        expand_camera_layout = (LinearLayout)findViewById(R.id.expand_layout);




        cameraview_2_aim = (ImageView)findViewById(R.id.cameraview_2_aim);
        back = (ImageButton)findViewById(R.id.back);
        back.setOnClickListener(this);

/*************************************데이터뷰 관련************************************************************************************/

		/* 데이터뷰(전체 레이아웃 오른쪽) : A/B 지점 각각 데이터 표시하는 부분(거리/높이/기울기) */
        dataview_2_each_data_spota_distance = (TextView) findViewById(R.id.dataview_2_each_data_spota_distance);
        dataview_2_each_data_spota_height = (TextView) findViewById(R.id.dataview_2_each_data_spota_height);
        dataview_2_each_data_spota_gradient = (TextView) findViewById(R.id.dataview_2_each_data_spota_gradient);

        dataview_2_each_data_spotb_distance = (TextView) findViewById(R.id.dataview_2_each_data_spotb_distance);
        dataview_2_each_data_spotb_height = (TextView) findViewById(R.id.dataview_2_each_data_spotb_height);
        dataview_2_each_data_spotb_gradient = (TextView) findViewById(R.id.dataview_2_each_data_spotb_gradient);

        dataview_3_compare_data_distance = (TextView) findViewById(R.id.dataview_3_compare_data_distance);
        dataview_3_compare_data_height = (TextView) findViewById(R.id.dataview_3_compare_data_height);
        dataview_3_compare_data_gradient = (TextView) findViewById(R.id.dataview_3_compare_data_gradient);

        /* 확장 카메라 데이터뷰 */
        extend_point = (TextView) findViewById(R.id.extend_point);
        extend_distance = (TextView) findViewById(R.id.extend_distance);
        extend_height = (TextView) findViewById(R.id.extend_height);


        /**************화면 크기에 따른 버튼 크기 조정 ***************/
        DisplayMetrics mdisplayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = mdisplayMetrics.widthPixels;
        int height = mdisplayMetrics.heightPixels;

//        Toast.makeText(this,width + " : " + height,Toast.LENGTH_LONG).show();
        Toast.makeText(this, mainLayout.getScaleX()+ ":"+mainLayout.getScaleY(),Toast.LENGTH_LONG).show();



/*저장한 UI 스케일 사이즈 불러오기 및 스케일 조절 버튼 등록 */
        UI_scale_size_value = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        UI_scale_size_value_editor = UI_scale_size_value.edit();


        direction_arrow_frame_scale_size_btn = (Button) findViewById(R.id.direction_arrow_frame_scale_size_btn);
        direction_arrow_frame_scale_size_btn.setOnClickListener(this);

        data_viewing_area_scale_size_btn = (Button) findViewById(R.id.data_viewing_area_scale_size_btn);
        data_viewing_area_scale_size_btn.setOnClickListener(this);



        /************** 나머지 설정 버튼 관련 *****************/

        /* 보정 계수 관련 */
        correction_value_btn = (Button) findViewById(R.id.correction_value_btn);
        correction_value_btn.setOnClickListener(this);

        move_original_button_location = (Button) findViewById(R.id.move_original_button_location);
        move_button_hold = (CheckBox) findViewById(R.id.move_button_hold);
        setting_button = (ImageButton) findViewById(R.id.setting_button);
        move_original_button_location.setOnClickListener(this);
        move_button_hold.setOnClickListener(this);
        setting_button.setOnClickListener(this);

        /*설정 버튼 제일 앞으로*/
        connect.setVisibility(View.INVISIBLE);
        serial_connect.setVisibility(View.INVISIBLE);
        direction_arrow_frame_scale_size_btn.setVisibility(View.INVISIBLE);
        data_viewing_area_scale_size_btn.setVisibility(View.INVISIBLE);
        correction_value_btn.setVisibility(View.INVISIBLE);
        move_original_button_location.setVisibility(View.INVISIBLE);
        move_button_hold.setVisibility(View.INVISIBLE);









    }
/*************************************************************************************************************************************/


    @Override
    public void onResume() {
        super.onResume();
        mUSBMonitor.register();
    }


    @Override
    public void onDestroy() {
        if(mUVCHandler != null){
            mUVCHandler.release();
            mUVCHandler = null;
        }

        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }

        mUVCCameraView = null;
//        mUsbReceiver.closeUsbSerial();
//        unregisterReceiver(mUsbReceiver);
//        mUsbReceiver = null;

        super.onDestroy();
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
//			if (resultCode == Activity.RESULT_OK) {
//				btService.getDeviceInfo(data);
//			}
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
//				btService.scanDevice();
                } else {
                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }
    }


/*************************************데이터(거리/높이/기울기) 전달 관련 메소드************************************************************************************/
        public void onSetText(String buf) {

        int arduino_data_distance;
        int arduino_data_height;
        //int arduino_data_gradient;

        Message msg = handler.obtainMessage();
        // 2016. 10. 23. 권태성 수정 부분
            /**
             * 아두이노 전송 방식이 ! 데이터 ! 로 ! 가 감싸져 있음
             * 첫 맨 앞 !를 사라지게 분리 시키기 위한 코드
             * */
        int firstIdx = buf.indexOf("!");
        int lastIdx = buf.lastIndexOf("!");     // 현재는 필요 없는 코드임
//            Toast.makeText(getApplicationContext(), buf, Toast.LENGTH_LONG).show();
        buf= buf.substring(firstIdx+1, buf.length());
            // 권태성 수정 종료
        String[] data = buf.split(":");

        try {
            select_mode = Integer.parseInt(data[0]);
            arduino_data_distance = Integer.parseInt(data[1]);
            arduino_data_height = Integer.parseInt(data[2]);
            //arduino_data_gradient = Integer.parseInt(data[]); 기울기값 구해야함

            switch (select_mode){
                case 1:
                    if(Integer.parseInt(data[0]) == 1){
                        msg.arg1 = select_mode;
                        aData = new ArduinoData();
                        aData.setEachData(arduino_data_distance, arduino_data_height);
                    }
                    break;

                case 2:
                    if(Integer.parseInt(data[0]) == 2){
                        msg.arg1 = select_mode;
                        bData = new ArduinoData();
                        bData.setEachData(arduino_data_distance, arduino_data_height);
                    }
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "유효하지 않은 데이터 : " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        msg.what = 200;
        handler.sendMessage(msg);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (msg.arg1 == 1 && aData != null) {
                        cameraview_1_spot_point.setText("A");
                        extend_point.setText("A");

                        cameraview_4_distance.setText(aData.each_data[0] + "mm");
                        cameraview_4_height.setText(aData.each_data[1] + "mm");

                        dataview_2_each_data_spota_distance.setText(aData.each_data[0] + "mm");
                        dataview_2_each_data_spota_height.setText(aData.each_data[1] + "mm");

                        extend_distance.setText(aData.each_data[0] + "mm");
                        extend_height.setText(aData.each_data[1] + "mm");
                    } else if (msg.arg1 == 2 && bData != null) {
                        cameraview_1_spot_point.setText("B");
                        extend_point.setText("B");

                        cameraview_4_distance.setText(bData.each_data[0] + "mm");
                        cameraview_4_height.setText(bData.each_data[1] + "mm");

                        dataview_2_each_data_spotb_distance.setText(bData.each_data[0] + "mm");
                        dataview_2_each_data_spotb_height.setText(bData.each_data[1] + "mm");

                        extend_distance.setText(bData.each_data[0] + "mm");
                        extend_height.setText(bData.each_data[1] + "mm");
                    }
                    /* A지점 B지점 모두 데이터가 들어왔을 경우에 비교 데이터가 입력되도록 함. */
                    if(aData != null && bData != null){
                        dataview_3_compare_data_distance.setText(aData.each_data[0] - bData.each_data[0] + "mm");
                        dataview_3_compare_data_height.setText(aData.each_data[1] - bData.each_data[1] + "mm");
                    }

                    break;
            }
        }

    };


/*************************************카메라 연결 리스너 ************************************************************************************/

    /* 카메라(핸들러) 연결 리스너 + 메소드 */
    private void startPreview() {
        mUVCHandler.startPreview();
//        camera_extension_button.setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraview_1_aim.setVisibility(View.VISIBLE);
                cameraview_2_aim.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }
    private void stopPreview() {
        if (mUVCHandler != null){
            mUVCHandler.close();
            cameraview_1_aim.setVisibility(View.INVISIBLE);
            cameraview_2_aim.setVisibility(View.INVISIBLE);

            //16.10.11 수정(박진형) : 카메라 연결을 해제했을 때 캡쳐한 사진을 기본 이미지로 바꿈.
            dataview_1_img_spota_check.setBackgroundResource(R.drawable.dataview_1_bg_title_noimage);
            dataview_1_img_spotb_check.setBackgroundResource(R.drawable.dataview_1_bg_title_noimage);
            dataview_1_imgView_spota.setImageResource(R.drawable.dataview_1_noimage);
            dataview_1_imgView_spotb.setImageResource(R.drawable.dataview_1_noimage);
        }
    }

    /* 16.10.14 수정(박진형) : 리스너에서 onAttach, onDettach는 usb device에 상관없이 반응을 함.그래서 구분할 수 있는 메소드를 사용해서 구분을 지음.(onAttach는 가끔 device가 null로 찍혀서 아예 아무것도 넣지 않음),
     * device.getProductId()가 13209인게 카메라임. */
    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) { //device가 null로 들어와서 구분을 할 수 없음. 여기에 코드 넣으면 모든 usb에 반응하기때문에 넣으면 안됨.
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) { //연결버튼을 눌러서 연결시켰을 때

            //Log.d("device-info", device.getDeviceClass() + ", " + device.getDeviceSubclass() + ", " + device.getVendorId() + ", " + device.getProductId() + ", " + device.getDeviceProtocol());


//            if(device.getProductId() == 13209){ //UVCCamera일 때만 동작하도록 설정(카메라 연결 버튼을 누르고 나서)
//                Toast.makeText(getApplicationContext(), "카메라가 연결되었습니다.", Toast.LENGTH_SHORT).show();
                mUVCHandler.open(ctrlBlock);
                startPreview();
//            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) { //임의버튼에 의해서 Usb연결을 해제했을 때
//            if(device.getProductId() == 13209){ //UVCCamera일 때만 동작하도록 설정(카메라 연결 버튼을 누르고 나서)
                stopPreview();
//            }
        }

        @Override
        public void onCancel() {
//            Toast.makeText(getApplicationContext(), "연결취소", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDettach(final UsbDevice device) { //Usb를 뺐을 때

//            if(device.getProductId() == 13209) {
//                Toast.makeText(getApplicationContext(), "카메라와의 연결이 끊겼습니다.", Toast.LENGTH_SHORT).show();
                stopPreview();
//            }
        }
    };

    /**
     * to access from CameraDialog
     * @return
     */
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            final Surface _surface = new Surface(surface);
            mUVCHandler.addSurface(surface.hashCode(), _surface, false, null);
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            mUVCHandler.removeSurface(surface.hashCode());
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
            // TODO Auto-generated method stub
        }

    };


 /******************************아두이노 시리얼 ch340 연결 리스너*********************************/

 public class SerialListener {
     public void onReceive(int msg, int arg0, int arg1, String arg2, Object arg3) {
         switch(msg) {
             case Constants.MSG_DEVICD_INFO:
                 mTextLog.append(arg2);
                 break;
             case Constants.MSG_DEVICE_COUNT:
                 mTextLog.append(Integer.toString(arg0) + " device(s) found \n");
                 break;
             case Constants.MSG_READ_DATA_COUNT:
                 mTextLog.append(Integer.toString(arg0) + " buffer received \n");
                 break;
             case Constants.MSG_READ_DATA:
                 if(arg3 != null) {
                     mTextInfo.setText((String)arg3);
                     mTextLog.append((String)arg3);
                     mTextLog.append("\n");
                     mTextLog.append("\n");
                 }
                 break;
             case Constants.MSG_SERIAL_ERROR:
                 mTextLog.append(arg2);
                 break;
             case Constants.MSG_FATAL_ERROR_FINISH_APP:
                 finish();
                 break;
         }
     }
 }





 public class ActivityHandler extends Handler {
     @Override
     public void handleMessage(Message msg) {
         switch(msg.what) {
             case Constants.MSG_DEVICD_INFO:
                 mTextLog.append((String)msg.obj);
                 break;
             case Constants.MSG_DEVICE_COUNT:
                 mTextLog.append(Integer.toString(msg.arg1) + " device(s) found \n");
                 break;

             /*****수신 데이터 처리 ***/
             case Constants.MSG_READ_DATA_COUNT:
                 mTextLog.append(((String)msg.obj) + "\n");
                 String receive_string = (String)msg.obj;
                 String[] spilt_data;
                 int [] spilt_data_to_int = new int[6];

                 for(int i=0; i < receive_string.length();i++){

                     if(is_appending){
                         appended_receive_data += receive_string.charAt(i);
                         spilt_data = receive_string.split(":"); //수신 string을 spilt함
                         for(int j=1; j < 7; j++){
                             try {
                                 //spilt된 string을 int로 바꿈
                                 spilt_data_to_int[j - 1] = Integer.parseInt(spilt_data[j]);
                                 if(execute_standard_value_saving){ //기준 잡기위한 버튼 누르면 true
                                     standard_value[j-1] = spilt_data_to_int[j-1];
                                 }

                             }catch(Exception e){
                                 Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                             }
                         }
                         if(execute_standard_value_saving){ // 기준 데이터를 넣고 나면 false로 바꿔줌
                             execute_standard_value_saving = false;
                             standard_height.setText(standard_value[1]+"");

                         }

                         try {
                             comparison_height.setText(spilt_data_to_int[1] + "");
                             height_difference.setText(standard_value[1] - spilt_data_to_int[1] + "");
                             if(is_starting_finding_correction_value){
                                 double degree_x = Math.acos(Math.toRadians(((double)standard_value[1])/((double)spilt_data_to_int[0]))); //높이가 같을때 각도x : 높이1/거리2
                                 double temp_degree2 = Math.toRadians(((double) spilt_data_to_int[2])/100.0); //실측한 각도
                                 double temp_correction_value = degree_x/temp_degree2; //각도 보정계수 계산
                                 double corrected_height = ((double) spilt_data_to_int[0]) * Math.cos(temp_correction_value * ((double)spilt_data_to_int[2]/100.0)); //보정된 높이
                                 height_difference.setText("보정계수:" + temp_correction_value*1000 + "/    " + corrected_height); // 보정 계수와 보정 높이 출력

                                 try {
//                                     double temp_correction_value = Math.acos(Math.toRadians((double) standard_value[1] / ((double) spilt_data_to_int[0]))) / ((double)spilt_data_to_int[2]);
////                                     double temp_correction_value = Math.acos((double) standard_value[1] / ((double) spilt_data_to_int[0]))/ ((double)spilt_data_to_int[2]/100.0);
//                                     double corrected_height = ((double) spilt_data_to_int[0] * Math.cos(temp_correction_value * spilt_data_to_int[2]/100.0)); //보정된 높이
//                                     height_difference.setText("보정계수:" + temp_correction_value + "/    " + (int) corrected_height); // 보정 계수와 보정 높이 출력
                                 }catch(Exception e){

                                 }
                             }
                         }catch(Exception e){
                             Toast.makeText(getApplicationContext(), "integer err", Toast.LENGTH_SHORT).show();
                         }



//                         Toast.makeText(getApplicationContext(), spilt_data_to_int[0]+":"+spilt_data_to_int[1]+":"+spilt_data_to_int[2]+":"+spilt_data_to_int[5]+":",Toast.LENGTH_SHORT).show();


                     }

                     if(receive_string.charAt(i) == '@'){ // 데이터 끝났을때
                         is_appending = false;
//                         Toast.makeText(getApplicationContext(),appended_receive_data ,Toast.LENGTH_SHORT).show();
                         appended_receive_data ="";

                     }else if(receive_string.charAt(i) == '!'){ // 데이터 시작할때
                         is_appending = true;
                     }
                 }


                 break;
             case Constants.MSG_READ_DATA:
                 if(msg.obj != null) {
                     mTextLog.append(((String)msg.obj) );
                     mTextLog.append(" end"+ "\n");
                 }
                 break;
             case Constants.MSG_SERIAL_ERROR:
                 mTextLog.append((String)msg.obj);
                 break;
         }
     }
 }




/***************************************************버튼 리스너******************************************************************************************/

    /* 16.10.11 수정(박진형) : 카메라, 아두이노 연결 버튼 각각 따로 둠. */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect) {
            //16.10.11 수정(박진형) : 카메라 연결이 해제되었을 때 연결버튼 기능을 다시 살림.
            if (!mUVCHandler.isOpened()) {
                CameraDialog.showDialog(Preview.this);
            } else {
                //16.10.11 수정(박진형) : 카메라 연결이 되어있을 때 아무런 동작 안하도록 그리고 캡쳐될 때 mUVCHandler가 false로 동작하기 때문에 여기에 넣는 코드가 동작함
            }

        }

//        if (v.getId() == R.id.9) {
//            //수정할 것 : 데이터가 돌아가고 있으면 커넥트 버튼 누를 때 토스트로 연결되어있다고 띄울 것.
//             mUsbReceiver.openUsbSerial();
//        }


        if (v.getId() == R.id.btn_help) {
            //수정할 것 : 안내화면 나오도록 할 것.
        }

        /* 조이스틱 버튼 기능 임의 추가 */
            if (v.getId() == R.id.dataview_1_img_spota) {
                select_mode = 1;
                dataview_1_img_spota_check.setBackgroundResource(R.drawable.dataview_1_bg_title_yesimage);
                dataview_1_img_spotb_check.setBackgroundResource(R.drawable.dataview_1_bg_title_noimage);


            // 수정할 것 : 두 기능 모두 사용가능할 때 동작하도록 바꿀 것.

            /* 카메라 캡처 */
                if (mUVCHandler.isOpened()) {
                    CaptureSoilTask capture = new CaptureSoilTask(getApplicationContext(), mUVCHandler, dataview_1_imgView_spota);
                    capture.execute(new Integer(select_mode));
                }

                /* 데이터 받아오기 */
//                mUsbReceiver.writeDataToSerial("d");

                final Button btn = (Button)findViewById(v.getId());
                btn.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btn.setEnabled(true);
                    }
                }, CAMERA_DELAY);

            }

        /* 조이스틱 버튼 기능 임의 추가 */
            if (v.getId() == R.id.dataview_1_img_spotb) {
                select_mode = 2;
                dataview_1_img_spota_check.setBackgroundResource(R.drawable.dataview_1_bg_title_noimage);
                dataview_1_img_spotb_check.setBackgroundResource(R.drawable.dataview_1_bg_title_yesimage);

            /* 카메라 캡처 */
                if (mUVCHandler.isOpened()) {
                    Log.e("Is Recording : ", ":"+mUVCHandler.isRecording());

                    CaptureSoilTask capture = new CaptureSoilTask(getApplicationContext(), mUVCHandler, dataview_1_imgView_spotb);
                    capture.execute(new Integer(select_mode));
                }
                /* 데이터 받아오기 */
//                mUsbReceiver.writeDataToSerial("d");

                final Button btn = (Button)findViewById(v.getId());
                btn.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btn.setEnabled(true);
                    }
                }, CAMERA_DELAY);

            }

        /* 카메라 확대모드 관련 뷰 */
            if (v.getId() == R.id.camera_scale_size_up_btn) {
                // mUVCCameraView를 공유하는 방식
                // 메인화면에서 보여지는 카메라 뷰 영역에서 mUVCCamera 를 빼내서
                // 확대된 카메라 뷰를 영역에 집어넣었어요

                if(is_camera_extended == 0) {
                    mUVCCameraView.setScaleX(1.5f);
                    mUVCCameraView.setScaleY(1.5f);
//                    defaultCameraSection.removeView(mUVCCameraView);
//                    extCameraSection.addView(mUVCCameraView);
                    is_camera_extended = 1;

                }else if(is_camera_extended == 1){
                    mUVCCameraView.setScaleX(2.0f);
                    mUVCCameraView.setScaleY(2.0f);
//                    extCameraSection.removeView(mUVCCameraView);
//                    defaultCameraSection.addView(mUVCCameraView);
                    is_camera_extended = 2;
                    camera_scale_size_up_btn.setBackgroundResource(R.drawable.camera_scale_down_btn);

                }else if(is_camera_extended == 2){
                    mUVCCameraView.setScaleX(1.0f);
                    mUVCCameraView.setScaleY(1.0f);
//                    extCameraSection.removeView(mUVCCameraView);
//                    defaultCameraSection.addView(mUVCCameraView);
                    is_camera_extended = 0;
                    camera_scale_size_up_btn.setBackgroundResource(R.drawable.camera_scale_up_btn);

                }

//                expand_camera_layout.setVisibility(View.VISIBLE);
            }

            if (v.getId() == R.id.back) {
                extCameraSection.removeView(mUVCCameraView);
                defaultCameraSection.addView(mUVCCameraView);
                expand_camera_layout.setVisibility(View.INVISIBLE);
            }


            //측정 시작 버튼
            if (v.getId() == R.id.middle) {
                if (mSerialConn != null) { //시리얼 연결됬을때만

                    if(!is_starting_measure) {
                        mSerialConn.sendCommand("d");
                        is_starting_measure = true;
                    }else if(is_starting_measure){
//                        try {
//                            standard_height.setText(comparison_height.getText());
//                            standard_value[1] = Integer.parseInt((String) comparison_height.getText());
//                        }catch (Exception e){
//                            Toast.makeText(getApplicationContext(), e+"",Toast.LENGTH_LONG).show();
//                        }
                        execute_standard_value_saving = true;

                    }
                }
            }

            //크기 조절 버튼
            if(v.getId() == R.id.direction_arrow_frame_scale_size_btn){
                //방향 버튼 크기 조절
                if(direction_arrow_frame_current_scale_size > 1.5){
                    direction_arrow_frame_scale_up_down = false;
                }else if(direction_arrow_frame_current_scale_size < 0.3){
                    direction_arrow_frame_scale_up_down = true;
                }

                if(direction_arrow_frame_scale_up_down){
                    direction_arrow_frame_current_scale_size += 0.1;
                }else{
                    direction_arrow_frame_current_scale_size -= 0.1;
                }
                direction_arrow_frame_moving.Scale_size_adjustment(direction_arrow_frame_current_scale_size);


            }else if(v.getId() == R.id.data_viewing_area_scale_size_btn){
                //데이터 뷰 크기 조절
                if(data_viewing_area_current_scale > 1.5){
                    data_viewing_area_scale_up_down = false;
                }else if(data_viewing_area_current_scale < 0.3){
                    data_viewing_area_scale_up_down = true;
                }

                if(data_viewing_area_scale_up_down){
                    data_viewing_area_current_scale += 0.1;
                }else{
                    data_viewing_area_current_scale -= 0.1;
                }
                data_viewing_area_moving.Scale_size_adjustment(data_viewing_area_current_scale);
            }


            //보정계수 계산 버튼
            if(v.getId() == R.id.correction_value_btn){

                if(is_starting_finding_correction_value == false) {
                    is_starting_finding_correction_value = true;
                }else if(is_starting_finding_correction_value == true){
                    is_starting_finding_correction_value = false;
                }


            }

            //세팅 버튼들 숨겼다 보이기
            if(v.getId() == R.id.setting_button){
                setting_buttons_group_visible();
            }





        }//onclicklistener 끝



    private View.OnTouchListener arrow_button = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {


              /* 방향 버튼 눌릴때 */
            int action = event.getAction();

            if(action == MotionEvent.ACTION_DOWN) {
//                Toast.makeText(getApplicationContext(),"버튼누름",Toast.LENGTH_SHORT).show();


                if (mSerialConn != null) { //시리얼 연결됬을때만
                    switch (v.getId()){
                        case R.id.left_up :
                            mSerialConn.motor_contol_command(1);
                            break;
                        case R.id.middle_up :
                            mSerialConn.motor_contol_command(2);
                            break;
                        case R.id.right_up :
                            mSerialConn.motor_contol_command(3);
                            break;
                        case R.id.left :
                            mSerialConn.motor_contol_command(4);
                            break;
                        //middle is onClicklistener
//                        case R.id.middle :
//                            mSerialConn.motor_contol_command(5);
//                            break;
                        case R.id.right :
                            mSerialConn.motor_contol_command(6);
                            break;
                        case R.id.left_down :
                            mSerialConn.motor_contol_command(7);
                            break;
                        case R.id.middle_down :
                            mSerialConn.motor_contol_command(8);
                            break;
                        case R.id.right_down :
                            mSerialConn.motor_contol_command(9);
                            break;

                    }
                }
            }


            /* 방향 버튼 뗄 때 */
            if(action == MotionEvent.ACTION_UP) {

                if (mSerialConn != null) { //시리얼 연결됬을때만
                    switch (v.getId()){
                        case R.id.left_up :
//                            Toast.makeText(getApplicationContext(),"땜",Toast.LENGTH_SHORT).show();
                            mSerialConn.motor_contol_command(0);
                            break;
                        case R.id.middle_up :
                            mSerialConn.motor_contol_command(0);
                            break;
                        case R.id.right_up :
                            mSerialConn.motor_contol_command(0);
                            break;
                        case R.id.left :
                            mSerialConn.motor_contol_command(0);
                            break;
                        //middle is onClicklistener
//                        case R.id.middle :
//                            mSerialConn.motor_contol_command(0);
//                            break;
                        case R.id.right :
                            mSerialConn.motor_contol_command(0);
                            break;
                        case R.id.left_down :
                            mSerialConn.motor_contol_command(0);
                            break;
                        case R.id.middle_down :
                            mSerialConn.motor_contol_command(0);
                            break;
                        case R.id.right_down :
                            mSerialConn.motor_contol_command(0);
                            break;

                    }
                }
            }


            return false;
        }
    };//ontouchlistener 끝

    private void setting_buttons_group_visible(){
        if(serial_connect.getVisibility() == View.VISIBLE) {
            connect.setVisibility(View.INVISIBLE);
            serial_connect.setVisibility(View.INVISIBLE);
            direction_arrow_frame_scale_size_btn.setVisibility(View.INVISIBLE);
            data_viewing_area_scale_size_btn.setVisibility(View.INVISIBLE);
            correction_value_btn.setVisibility(View.INVISIBLE);
            move_original_button_location.setVisibility(View.INVISIBLE);
            move_button_hold.setVisibility(View.INVISIBLE);
        }else if(serial_connect.getVisibility() == View.INVISIBLE){
            connect.setVisibility(View.VISIBLE);
            serial_connect.setVisibility(View.VISIBLE);
            direction_arrow_frame_scale_size_btn.setVisibility(View.VISIBLE);
            data_viewing_area_scale_size_btn.setVisibility(View.VISIBLE);
            correction_value_btn.setVisibility(View.VISIBLE);
            move_original_button_location.setVisibility(View.VISIBLE);
            move_button_hold.setVisibility(View.VISIBLE);
        }


        connect.bringToFront();
        serial_connect.bringToFront();
        direction_arrow_frame_scale_size_btn.bringToFront();
        data_viewing_area_scale_size_btn.bringToFront();
        correction_value_btn.bringToFront();
        move_original_button_location.bringToFront();
        move_button_hold.bringToFront();


    }







}