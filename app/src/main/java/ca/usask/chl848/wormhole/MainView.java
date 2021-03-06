package ca.usask.chl848.wormhole;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Created by chl848 on 05/14/2015.
 */
public class MainView extends View {
    Paint m_paint;

    private String m_id;
    private String m_name;
    private int m_color;
    private static final int m_messageTextSize = 70;
    private static final int m_textStrokeWidth = 2;
    private static final int m_boundaryStrokeWidth = 10;

    private String m_message;

    public class RemotePhoneInfo {
        String m_name;
        int m_color;
        WormholeInfo m_wormholeInfo;
    }

    private final static int m_wormhole_number = 3;
    private float[] m_wormholeX = new float[m_wormhole_number];
    private float[] m_wormholeY = new float[m_wormhole_number];
    private float m_wormholeRadius;

    private ArrayList<RemotePhoneInfo> m_remotePhones;

    public class WormholeInfo {
        int m_id;
        float m_x;
        float m_y;
        float m_radius;
    }

    private ArrayList<WormholeInfo> m_wormholes;

    private class Ball {
        public int m_ballColor;
        public float m_ballX;
        public float m_ballY;
        public boolean m_isTouched;
        public String m_id;
        public String m_name;

    }
    private ArrayList<Ball> m_balls;
    private int m_touchedBallId;

    private float m_ballRadius;
    private float m_ballBornX;
    private float m_ballBornY;

    private boolean m_showRemoteNames;
    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        @Override
        public void run() {
            setShowRemoteNames(true);
            m_numberOfLongPress++;
            invalidate();
        }
    };

    /**
     * experiment begin
     */

    private ArrayList<String> m_ballNames;
    private long m_trailStartTime;
    private int m_numberOfDrops;
    private int m_numberOfErrors;
    private int m_numberOfTouch;
    private int m_numberOfTouchBall;
    private int m_numberOfLongPress;
    private int m_numberOfRelease;
    private String m_receiverName;
    private int m_maxBlocks;
    private int m_maxTrails;
    private int m_currentBlock;
    private int m_currentTrail;
    private class WormholeSequence {
        int m_left;
        int m_middle;
        int m_right;
    }
    private ArrayList<WormholeSequence> m_wormholeSequences;

    private static final int m_experimentPhoneNumber = 3;

    private MainLogger m_logger;

    private boolean m_isExperimentInitialised;
    /**
     * experiment end
     */

    public MainView (Context context) {
        super(context);

        m_paint = new Paint();
        m_remotePhones = new ArrayList<>();
        initWormholes();
        setBackgroundColor(Color.WHITE);
        m_message = "No Message";
        m_id = ((MainActivity)(context)).getUserId();
        m_name = ((MainActivity)(context)).getUserName();
        //Random rnd = new Random();
        //m_color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        m_color = ((MainActivity)(context)).getUserColor();

        m_touchedBallId = -1;
        m_balls = new ArrayList<>();
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        m_ballRadius = displayMetrics.widthPixels * 0.08f;
        m_ballBornX = displayMetrics.widthPixels * 0.5f;
        m_ballBornY = displayMetrics.heightPixels * 0.75f - m_ballRadius * 2.0f;

        setShowRemoteNames(false);

        resetCounters();

        m_isExperimentInitialised = false;


/*
        updateRemotePhone("chengzhao", Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        updateRemotePhone("selene", Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        updateRemotePhone("renee", Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));

        updateRemotePhone("shushu", 0);
        updateRemotePhone("jiaxin", 0);
        updateRemotePhone("baby", 0);
        updateRemotePhone("yina", 0);
        updateRemotePhone("chi", 0);*/

    }

    private void initWormholeXY() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        m_wormholeX[0] = displayMetrics.widthPixels * 0.5f;
        m_wormholeY[0] = displayMetrics.heightPixels * 0.1f;

        m_wormholeX[1] = displayMetrics.widthPixels * 0.15f;
        m_wormholeY[1] = displayMetrics.heightPixels * 0.3f;

        m_wormholeX[2] = displayMetrics.widthPixels * 0.85f;
        m_wormholeY[2] = displayMetrics.heightPixels * 0.3f;

        m_wormholeRadius = displayMetrics.widthPixels * 0.05f;
    }

    private void initWormholes() {
        initWormholeXY();
        m_wormholes = new ArrayList<>();

        for (int i=0; i<m_wormhole_number; ++i) {
            WormholeInfo wormholeInfo = new WormholeInfo();
            wormholeInfo.m_id = i;
            wormholeInfo.m_x = m_wormholeX[i];
            wormholeInfo.m_y = m_wormholeY[i];
            wormholeInfo.m_radius = m_wormholeRadius;

            m_wormholes.add(wormholeInfo);
        }
    }

    private void setShowRemoteNames(boolean show) {
        m_showRemoteNames = show;
    }

    private boolean getShowRemoteNames() {
        return m_showRemoteNames;
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        showBoundary(canvas);
        showMessage(canvas);
        showRemotePhones(canvas);
        showBalls(canvas);
        //showProgress(canvas);
    }

    public void showBoundary(Canvas canvas) {
        m_paint.setColor(Color.RED);
        m_paint.setStrokeWidth(m_boundaryStrokeWidth);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        canvas.drawLine(0, displayMetrics.heightPixels * 0.75f, displayMetrics.widthPixels, displayMetrics.heightPixels * 0.75f, m_paint);
    }

    public void showMessage(Canvas canvas) {
        m_paint.setTextSize(m_messageTextSize);
        m_paint.setColor(Color.GREEN);
        m_paint.setStrokeWidth(m_textStrokeWidth);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        canvas.drawText(m_message, displayMetrics.widthPixels * 0.3f, displayMetrics.heightPixels * 0.8f, m_paint);
    }

    public void showRemotePhones(Canvas canvas) {
        m_paint.setTextSize(m_messageTextSize);

        for (RemotePhoneInfo remotePhoneInfo : m_remotePhones) {
            m_paint.setColor(remotePhoneInfo.m_color);
            if (getShowRemoteNames()) {
                m_paint.setStrokeWidth(m_textStrokeWidth);
                m_paint.setStyle(Paint.Style.FILL_AND_STROKE);

                float textX = remotePhoneInfo.m_wormholeInfo.m_x - remotePhoneInfo.m_wormholeInfo.m_radius;
                float textY = remotePhoneInfo.m_wormholeInfo.m_y - remotePhoneInfo.m_wormholeInfo.m_radius * 1.5f;
                if (remotePhoneInfo.m_name.length() > 5) {
                    textX = remotePhoneInfo.m_wormholeInfo.m_x - remotePhoneInfo.m_wormholeInfo.m_radius * 2.0f;
                }
                canvas.drawText(remotePhoneInfo.m_name, textX,  textY, m_paint);
            }

            m_paint.setStrokeWidth(12);
            m_paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(remotePhoneInfo.m_wormholeInfo.m_x, remotePhoneInfo.m_wormholeInfo.m_y, remotePhoneInfo.m_wormholeInfo.m_radius, m_paint);
        }
    }

    public void showBalls(Canvas canvas) {
        if (!m_balls.isEmpty()) {
            for (Ball ball : m_balls) {
                m_paint.setColor(ball.m_ballColor);
                m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawCircle(ball.m_ballX, ball.m_ballY, m_ballRadius, m_paint);

                /**
                 * experiment begin
                 */
                m_paint.setStrokeWidth(m_textStrokeWidth);
                m_paint.setTextSize(m_messageTextSize);
                float textX = ball.m_ballX - m_ballRadius;
                float textY = ball.m_ballY - m_ballRadius;
                if (ball.m_name.length() > 5) {
                    textX = ball.m_ballX - m_ballRadius * 2.0f;
                }
                canvas.drawText(ball.m_name, textX, textY, m_paint);
                /**
                 * experiment end
                 */
            }
        }
    }

    public void showProgress(Canvas canvas) {
        m_paint.setTextSize(m_messageTextSize);
        m_paint.setColor(Color.BLUE);
        m_paint.setStrokeWidth(m_textStrokeWidth);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        String block = "Block: " + m_currentBlock +"/" + m_maxBlocks;
        canvas.drawText(block, (int) (displayMetrics.widthPixels * 0.75), (int) (displayMetrics.heightPixels * 0.1), m_paint);

        String trial = "Trial: " + m_currentTrail +"/" + m_maxTrails;
        canvas.drawText(trial, (int) (displayMetrics.widthPixels * 0.75), (int) (displayMetrics.heightPixels * 0.15), m_paint);
    }

    public void setMessage (String msg) {
        m_message = msg;
    }

    public void updateRemotePhone(String name, int color){
        if (name.isEmpty() || name.equalsIgnoreCase(m_name)) {
            return;
        }

        int size = m_remotePhones.size();
        boolean isFound = false;
        for (int i = 0; i<size; ++i) {
            RemotePhoneInfo info = m_remotePhones.get(i);
            if (info.m_name.equalsIgnoreCase(name)) {
                info.m_color = color;
                isFound = true;
                break;
            }
        }

        if (!isFound && size < m_wormhole_number) {
            RemotePhoneInfo info = new RemotePhoneInfo();
            info.m_name = name;
            info.m_color = color;
            info.m_wormholeInfo = getAvailableWormhole();

            m_remotePhones.add(info);

            /**
             * experiment end
             */
            if (m_remotePhones.size() == m_experimentPhoneNumber && !m_isExperimentInitialised) {
                initExperiment();
            }
            /**
             * experiment end
             */
        }
    }

    private WormholeInfo getAvailableWormhole() {
        ArrayList<WormholeInfo> wormholeInfos = new ArrayList<>();

        for (WormholeInfo wormholeInfo : m_wormholes) {
            boolean isAvailable = true;
            for (RemotePhoneInfo remotePhoneInfo : m_remotePhones) {
                if (wormholeInfo.m_id == remotePhoneInfo.m_wormholeInfo.m_id) {
                    isAvailable = false;
                    break;
                }
            }

            if (isAvailable) {
                wormholeInfos.add(wormholeInfo);
            }
        }

        if (wormholeInfos.isEmpty()) {
            WormholeInfo wormholeInfo = new WormholeInfo();
            wormholeInfo.m_id = -1;
            return wormholeInfo;
        } else {
            Random rnd = new Random();
            return wormholeInfos.get(rnd.nextInt(wormholeInfos.size()));
        }
    }

    public ArrayList<RemotePhoneInfo> getRemotePhones() {
        return m_remotePhones;
    }

    public void removePhones(ArrayList<RemotePhoneInfo> phoneInfos) {
        m_remotePhones.removeAll(phoneInfos);
    }

    public int getBallCount() {
        return m_balls.size();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();

        float X = event.getX();
        float Y = event.getY();
        float touchRadius = event.getTouchMajor();

        int ballCount = m_balls.size();
        switch (eventaction) {
            case MotionEvent.ACTION_DOWN:
                m_numberOfTouch++;
                if (!canTouch(X, Y)) {
                    break;
                }
                m_touchedBallId = -1;
                for (int i = 0; i < ballCount; ++i){
                    Ball ball = m_balls.get(i);
                    ball.m_isTouched = false;

                    double dist;
                    dist = Math.sqrt(Math.pow((X - ball.m_ballX), 2) + Math.pow((Y - ball.m_ballY), 2));
                    if (dist <= (touchRadius + m_ballRadius)) {
                        ball.m_isTouched = true;
                        m_touchedBallId = i;

                        boolean isOverlap = false;
                        for (int j = 0; j < ballCount; ++j) {
                            if (j != m_touchedBallId) {
                                Ball ball2 = m_balls.get(j);

                                double dist2 = Math.sqrt(Math.pow((X - ball2.m_ballX), 2) + Math.pow((Y - ball2.m_ballY), 2));
                                if (dist2 <= m_ballRadius * 2) {
                                    isOverlap = true;
                                }
                            }
                        }

                        if (!isOverlap && !isBoundary(X, Y)) {
                            ball.m_ballX = X;
                            ball.m_ballY = Y;
                            this.invalidate();
                        }
                    }

                    if (m_touchedBallId > -1)
                    {
                        break;
                    }
                }

                if (m_touchedBallId == -1) {

                    boolean show = false;

                    for (RemotePhoneInfo remotePhone : m_remotePhones) {
                        double dist = Math.sqrt(Math.pow((X - remotePhone.m_wormholeInfo.m_x),2) + Math.pow((Y - remotePhone.m_wormholeInfo.m_y), 2));

                        if (dist <= (touchRadius + remotePhone.m_wormholeInfo.m_radius)) {
                            show = true;
                            break;
                        }
                    }

                    if (show) {
                        handler.postDelayed(mLongPressed, 500);
                    }
                } else {
                    m_numberOfTouchBall++;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (getShowRemoteNames()) {
                    boolean show = false;

                    for (RemotePhoneInfo remotePhone : m_remotePhones) {
                        double dist = Math.sqrt(Math.pow((X - remotePhone.m_wormholeInfo.m_x),2) + Math.pow((Y - remotePhone.m_wormholeInfo.m_y), 2));

                        if (dist <= (touchRadius + remotePhone.m_wormholeInfo.m_radius)) {
                            show = true;
                            break;
                        }
                    }

                    if (!show) {
                        handler.removeCallbacks(mLongPressed);
                        setShowRemoteNames(false);
                        invalidate();
                    }
                }

                if (m_touchedBallId > -1) {
                    Ball ball = m_balls.get(m_touchedBallId);
                    if (ball.m_isTouched) {
                        boolean isOverlap = false;

                        for (int j = 0; j < ballCount; ++j) {
                            if (j != m_touchedBallId) {
                                Ball ball2 = m_balls.get(j);

                                double dist = Math.sqrt(Math.pow((X - ball2.m_ballX), 2) + Math.pow((Y - ball2.m_ballY), 2));
                                if (dist <= m_ballRadius * 2) {
                                    isOverlap = true;
                                }
                            }
                        }

                        if (!isOverlap && !isBoundary(X, Y)) {
                            ball.m_ballX = X;
                            ball.m_ballY = Y;
                            this.invalidate();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(mLongPressed);
                if (getShowRemoteNames()) {
                    setShowRemoteNames(false);
                    invalidate();
                }

                m_numberOfRelease++;

                if (m_touchedBallId > -1) {
                    m_numberOfDrops += 1;
                    Ball ball = m_balls.get(m_touchedBallId);
                    if (ball.m_isTouched) {
                        boolean isOverlap = false;

                        for (int j = 0; j < ballCount; ++j) {
                            if (j != m_touchedBallId) {
                                Ball ball2 = m_balls.get(j);

                                double dist = Math.sqrt(Math.pow((X - ball2.m_ballX), 2) + Math.pow((Y - ball2.m_ballY), 2));
                                if (dist <= m_ballRadius * 2) {
                                    isOverlap = true;
                                }
                            }
                        }

                        if (!isOverlap && !isBoundary(X, Y)) {
                            String name = isSending(ball.m_ballX, ball.m_ballY);
                            if (!ball.m_name.isEmpty() && !name.isEmpty()){
                                if (name.equalsIgnoreCase(ball.m_name)) {
                                    //((MainActivity) getContext()).showToast("send ball to : " + id);
                                    //sendBall(ball, name);
                                    removeBall(ball.m_id);
                                    this.invalidate();
                                    endTrail();
                                } else {
                                    m_numberOfErrors += 1;
                                }
                            } else {
                                ball.m_ballX = X;
                                ball.m_ballY = Y;
                                this.invalidate();
                            }
                        }
                    }
                }
                for (Ball ball : m_balls) {
                    ball.m_isTouched = false;
                }
                break;
        }

        return  true;
    }

    private boolean isBoundary(float x, float y) {
        boolean rt = false;
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        if ((y-m_ballRadius <= 0.0f) || (y+m_ballRadius >= (displayMetrics.heightPixels * 0.75f)) || (x-m_ballRadius <= 0.0f) || (x+m_ballRadius >= displayMetrics.widthPixels)) {
            rt = true;
        }

        return rt;
    }

    private boolean canTouch(float x, float y) {
        boolean rt = true;
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        if ((y <= 0.0f) || (y >= (displayMetrics.heightPixels * 0.75f)) || (x <= 0.0f) || (x >= displayMetrics.widthPixels)) {
            rt = false;
        }

        return rt;
    }

    private String isSending(float x, float y) {
        String rt = "";
        for (RemotePhoneInfo remotePhone : m_remotePhones) {
            double dist = Math.sqrt(Math.pow((x - remotePhone.m_wormholeInfo.m_x),2) + Math.pow((y - remotePhone.m_wormholeInfo.m_y), 2));

            if (dist <= (m_ballRadius + remotePhone.m_wormholeInfo.m_radius)) {
                rt = remotePhone.m_name;
                break;
            }
        }

        return rt;
    }

    public void addBall() {
        Ball ball = new Ball();
        Random rnd = new Random();
        ball.m_ballColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        ball.m_ballX = m_ballBornX;
        ball.m_ballY = m_ballBornY;
        ball.m_isTouched = false;
        ball.m_id = UUID.randomUUID().toString();
        ball.m_name = getBallName();
        m_balls.add(ball);
        m_receiverName = ball.m_name;
        this.invalidate();
    }

    public  void removeBall(String id) {
        for (Ball ball : m_balls) {
            if (ball.m_id.equalsIgnoreCase(id)) {
                m_balls.remove(ball);
                m_touchedBallId = -1;
                break;
            }
        }
    }

    public void receivedBall(String id, int color) {
        boolean isReceived = false;
        for (Ball ball : m_balls) {
            if (ball.m_id.equalsIgnoreCase(id)) {
                isReceived = true;
                break;
            }
        }

        if (!isReceived) {
            Ball ball = new Ball();
            ball.m_id = id;
            ball.m_ballColor = color;
            ball.m_isTouched = false;

            ball.m_ballX = m_ballBornX;
            ball.m_ballY = m_ballBornY;

            m_balls.add(ball);
        }
    }

    public void sendBall(Ball ball, String receiverName ) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("ballId", ball.m_id);
            jo.put("ballColor", ball.m_ballColor);
            jo.put("receiverName", receiverName);
            jo.put("isSendingBall", true);
            jo.put("name", m_name);
            jo.put("color", m_color);
            jo.put("x", 0);
            jo.put("y", 0);
            jo.put("z", 0);
        } catch (JSONException e){
            e.printStackTrace();
        }

        MainActivity ma = (MainActivity)getContext();
        if (ma != null) {
            ma.addMessage(jo.toString());
        }
    }

    public void sendPhoneInfo(){
        JSONObject jo = new JSONObject();
        try {
            jo.put("isSendingBall", false);
            jo.put("name", m_name);
            jo.put("color", m_color);
            jo.put("x", 0);
            jo.put("y", 0);
            jo.put("z", 0);
        } catch (JSONException e){
            e.printStackTrace();
        }

        MainActivity ma = (MainActivity)getContext();
        if (ma != null) {
            ma.addMessage(jo.toString());
        }
    }

    public void clearRemotePhoneInfo() {
        m_remotePhones.clear();
    }

    /**
     * experiment begin
     */
    private void initExperiment() {
        m_isExperimentInitialised = true;

        // init ball names
        m_ballNames = new ArrayList<>();

        m_maxBlocks = 5;
        m_maxTrails = 9;

        m_currentBlock = 0;
        m_currentTrail = 0;

        initWormholeSequence();

        resetBlock();

        m_logger = null;
        //m_logger = new MainLogger(getContext(), m_id+"_"+m_name+"_"+getResources().getString(R.string.app_name));
        //<participantID> <participantName> <condition> <block#> <trial#> <receiver name> <elapsed time for this trial> <number of errors for this trial> <number of release for this trial> <number of drops for this trial> <number of touch for this trial> <number of touch ball for this trial> <number of long press for this trial> <timestamp>
         //m_logger.writeHeaders("participantID" + "," + "participantName" + "," + "condition" + "," + "block" + "," + "trial" + "," + "receiverName" + "," + "elapsedTime" + "," + "errors" + "," + "release" + "," + "drops" + "," + "touch" + "," + "touchBall" + "," + "longPress" + "," + "timestamp");

        ((MainActivity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) getContext()).setStartButtonEnabled(true);
                ((MainActivity) getContext()).setContinueButtonEnabled(false);
            }
        });
    }

    private void initWormholeSequence() {
        m_wormholeSequences = new ArrayList<>();

        WormholeSequence wormholeSequence1 = new WormholeSequence();
        wormholeSequence1.m_left = 0;
        wormholeSequence1.m_middle = 1;
        wormholeSequence1.m_right = 2;
        m_wormholeSequences.add(wormholeSequence1);

        WormholeSequence wormholeSequence2 = new WormholeSequence();
        wormholeSequence2.m_left = 0;
        wormholeSequence2.m_middle = 2;
        wormholeSequence2.m_right = 1;
        m_wormholeSequences.add(wormholeSequence2);

        WormholeSequence wormholeSequence3 = new WormholeSequence();
        wormholeSequence3.m_left = 1;
        wormholeSequence3.m_middle = 0;
        wormholeSequence3.m_right = 2;
        m_wormholeSequences.add(wormholeSequence3);

        WormholeSequence wormholeSequence4 = new WormholeSequence();
        wormholeSequence4.m_left = 1;
        wormholeSequence4.m_middle = 2;
        wormholeSequence4.m_right = 0;
        m_wormholeSequences.add(wormholeSequence4);

        WormholeSequence wormholeSequence5 = new WormholeSequence();
        wormholeSequence5.m_left = 2;
        wormholeSequence5.m_middle = 0;
        wormholeSequence5.m_right = 1;
        m_wormholeSequences.add(wormholeSequence5);

        WormholeSequence wormholeSequence6 = new WormholeSequence();
        wormholeSequence6.m_left = 2;
        wormholeSequence6.m_middle = 1;
        wormholeSequence6.m_right = 0;
        m_wormholeSequences.add(wormholeSequence6);
    }

    private String getBallName() {
        if (m_ballNames.isEmpty()) {
            return "";
        }

        Random rnd = new Random();
        int index = rnd.nextInt(m_ballNames.size());
        String name = m_ballNames.get(index);
        m_ballNames.remove(index);
        return name;
    }

    public boolean isFinished() {
        return m_currentBlock == m_maxBlocks;
    }

    public void nextBlock() {
        ((MainActivity)getContext()).setStartButtonEnabled(true);
        ((MainActivity)getContext()).setContinueButtonEnabled(false);
    }

    public void resetBlock() {
        // reset ball names
        m_ballNames.clear();
        for (RemotePhoneInfo remotePhoneInfo : m_remotePhones){
            for(int i=0; i<3; i++){
                m_ballNames.add(remotePhoneInfo.m_name);
            }
        }

        // reset wormholes
        for (RemotePhoneInfo remotePhoneInfo : m_remotePhones) {
            remotePhoneInfo.m_wormholeInfo = null;
        }

        Random rnd = new Random();
        int index = rnd.nextInt(m_wormholeSequences.size());
        WormholeSequence wormholeSequence = m_wormholeSequences.get(index);
        m_wormholeSequences.remove(index);

        if (m_remotePhones.size() == m_experimentPhoneNumber) {
            m_remotePhones.get(0).m_wormholeInfo = m_wormholes.get(wormholeSequence.m_left);
            m_remotePhones.get(1).m_wormholeInfo = m_wormholes.get(wormholeSequence.m_middle);
            m_remotePhones.get(2).m_wormholeInfo = m_wormholes.get(wormholeSequence.m_right);
        }

        // reset self color
        //m_color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        resetCounters();
    }

    public void startBlock() {
        m_currentBlock += 1;
        m_currentTrail = 0;
        resetBlock();
        startTrial();
        ((MainActivity)getContext()).setStartButtonEnabled(false);
        ((MainActivity)getContext()).setContinueButtonEnabled(false);
    }

    public void endBlock() {
        if (isFinished() && (m_logger != null)) {
            closeLogger();
        }
        new AlertDialog.Builder(getContext()).setTitle("Warning").setMessage("You have completed block " + m_currentBlock + ", please wait for other participants.").setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
        ((MainActivity) getContext()).setContinueButtonEnabled(true);
        ((MainActivity)getContext()).setStartButtonEnabled(false);
        m_currentTrail = 0;
    }

    public void startTrial() {
        m_trailStartTime = System.currentTimeMillis();
        m_currentTrail += 1;
        resetCounters();
        addBall();
    }

    public void endTrail() {
        long trailEndTime = System.currentTimeMillis();
        long timeElapse = trailEndTime - m_trailStartTime;

        if (m_currentBlock == 0) {
            ++m_currentBlock;
        }

        if (m_currentTrail == 0) {
            ++m_currentTrail;
        }

        //<participantID> <participantName> <condition> <block#> <trial#> <receiver name> <elapsed time for this trial> <number of errors for this trial> <number of release for this trial> <number of drops for this trial> <number of touch for this trial> <number of touch ball for this trial> <number of long press for this trial> <timestamp>
        if (m_logger != null) {
            //m_logger.write(m_id + "," + m_name + "," + getResources().getString(R.string.app_name) + "," + m_currentBlock + "," + m_currentTrail + "," + m_receiverName + "," + timeElapse + "," + m_numberOfErrors + "," + m_numberOfRelease + "," + m_numberOfDrops + "," + m_numberOfTouch + "," + m_numberOfTouchBall + "," + m_numberOfLongPress + "," + trailEndTime, true);
        }

        if (m_currentTrail < m_maxTrails) {
            startTrial();
        } else {
            endBlock();
        }
    }

    public void closeLogger() {
        if (m_logger != null) {
            m_logger.close();
        }
    }

    private void resetCounters() {
        m_numberOfDrops = 0;
        m_numberOfErrors = 0;
        m_numberOfTouch = 0;
        m_numberOfTouchBall = 0;
        m_numberOfLongPress = 0;
        m_numberOfRelease = 0;
        m_receiverName = "";
    }
    /**
     * experiment end
     */
}
