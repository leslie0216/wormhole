package ca.usask.chl848.wormhole;

import android.content.Context;
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
    private int m_color;

    private String m_message;

    public class RemotePhoneInfo {
        String m_id;
        int m_color;
        WormholeInfo m_wormholeInfo;
    }

    private final static int m_wormhole_number = 8;
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

    private static final float m_ballRadius = 50.0f;
    private float m_ballBornX;
    private float m_ballBornY;

    private boolean m_showRemoteNames;
    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        @Override
        public void run() {
            setShowRemoteNames(true);
            invalidate();
        }
    };

    public MainView (Context context) {
        super(context);

        m_paint = new Paint();
        m_remotePhones = new ArrayList<>();
        initWormholes();
        setBackgroundColor(Color.WHITE);
        m_message = "No Message";
        m_id = ((MainActivity)(context)).getBTName();
        Random rnd = new Random();
        m_color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        m_touchedBallId = -1;
        m_balls = new ArrayList<>();
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        m_ballBornX = displayMetrics.widthPixels * 0.5f;
        m_ballBornY = displayMetrics.heightPixels * 0.75f - m_ballRadius * 2.0f;

        setShowRemoteNames(false);


        updateRemotePhone("jojo", 0);
        updateRemotePhone("selene", 0);
        updateRemotePhone("renee", 0);
        /*
        updateRemotePhone("shushu", 0);
        updateRemotePhone("jiaxin", 0);
        updateRemotePhone("baby", 0);
        updateRemotePhone("yina", 0);
        updateRemotePhone("chi", 0);*/

    }

    private void initWormholeXY() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        m_wormholeX[0] = displayMetrics.widthPixels * 0.15f;
        m_wormholeY[0] = displayMetrics.heightPixels * 0.1f;

        m_wormholeX[1] = displayMetrics.widthPixels * 0.5f;
        m_wormholeY[1] = displayMetrics.heightPixels * 0.1f;

        m_wormholeX[2] = displayMetrics.widthPixels * 0.85f;
        m_wormholeY[2] = displayMetrics.heightPixels * 0.1f;

        m_wormholeX[3] = displayMetrics.widthPixels * 0.3f;
        m_wormholeY[3] = displayMetrics.heightPixels * 0.3f;

        m_wormholeX[4] = displayMetrics.widthPixels * 0.7f;
        m_wormholeY[4] = displayMetrics.heightPixels * 0.3f;

        m_wormholeX[5] = displayMetrics.widthPixels * 0.15f;
        m_wormholeY[5] = displayMetrics.heightPixels * 0.5f;

        m_wormholeX[6] = displayMetrics.widthPixels * 0.5f;
        m_wormholeY[6] = displayMetrics.heightPixels * 0.5f;

        m_wormholeX[7] = displayMetrics.widthPixels * 0.85f;
        m_wormholeY[7] = displayMetrics.heightPixels * 0.5f;

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
    }

    public void showBoundary(Canvas canvas) {
        m_paint.setColor(Color.RED);
        m_paint.setStrokeWidth(10);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        canvas.drawLine(0, displayMetrics.heightPixels * 0.75f, displayMetrics.widthPixels, displayMetrics.heightPixels * 0.75f, m_paint);
    }

    public void showMessage(Canvas canvas) {
        m_paint.setTextSize(50);
        m_paint.setColor(Color.GREEN);
        m_paint.setStrokeWidth(2);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        canvas.drawText(m_message, displayMetrics.widthPixels * 0.3f, displayMetrics.heightPixels * 0.8f, m_paint);
    }

    public void showRemotePhones(Canvas canvas) {
        m_paint.setTextSize(50);
        m_paint.setColor(Color.BLACK);

        for (RemotePhoneInfo remotePhoneInfo : m_remotePhones) {
            if (getShowRemoteNames()) {
                m_paint.setStrokeWidth(2);
                m_paint.setStyle(Paint.Style.FILL_AND_STROKE);

                float textX = remotePhoneInfo.m_wormholeInfo.m_x - remotePhoneInfo.m_wormholeInfo.m_radius;
                float textY = remotePhoneInfo.m_wormholeInfo.m_y - remotePhoneInfo.m_wormholeInfo.m_radius;
                if (remotePhoneInfo.m_id.length() > 5) {
                    textX = remotePhoneInfo.m_wormholeInfo.m_x - remotePhoneInfo.m_wormholeInfo.m_radius * 2.0f;
                }
                canvas.drawText(remotePhoneInfo.m_id, textX,  textY, m_paint);
            }

            m_paint.setStrokeWidth(8);
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

                m_paint.setStrokeWidth(2);
                float textX = ball.m_ballX - m_ballRadius;
                float textY = ball.m_ballY - m_ballRadius;
                if (ball.m_name.length() > 5) {
                    textX = ball.m_ballX - m_ballRadius * 2.0f;
                }
                canvas.drawText(ball.m_name, textX, textY, m_paint);
            }
        }
    }

    public void setMessage (String msg) {
        m_message = msg;
    }

    public void updateRemotePhone(String id, int color){
        if (id.equalsIgnoreCase(m_id)) {
            return;
        }

        int size = m_remotePhones.size();
        boolean isFound = false;
        for (int i = 0; i<size; ++i) {
            RemotePhoneInfo info = m_remotePhones.get(i);
            if (info.m_id.equalsIgnoreCase(id)) {
                isFound = true;
                break;
            }
        }

        if (!isFound && size < m_wormhole_number) {
            RemotePhoneInfo info = new RemotePhoneInfo();
            info.m_id = id;
            info.m_color = color;
            info.m_wormholeInfo = getAvailableWormhole();

            m_remotePhones.add(info);
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
        float radius = event.getTouchMajor();

        int ballCount = m_balls.size();
        switch (eventaction) {
            case MotionEvent.ACTION_DOWN:
                if (!canTouch(X, Y)) {
                    break;
                }
                m_touchedBallId = -1;
                for (int i = 0; i < ballCount; ++i){
                    Ball ball = m_balls.get(i);
                    ball.m_isTouched = false;

                    double dist;
                    dist = Math.sqrt(Math.pow((X - ball.m_ballX), 2) + Math.pow((Y - ball.m_ballY), 2));
                    if (dist <= radius) {
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

                        if (dist <= (radius + remotePhone.m_wormholeInfo.m_radius)) {
                            show = true;
                            break;
                        }
                    }

                    if (show) {
                        handler.postDelayed(mLongPressed, 1000);
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
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
                            String id = isSending(X, Y);
                            if (id.equalsIgnoreCase(""))
                            {
                                ball.m_ballX = X;
                                ball.m_ballY = Y;
                                this.invalidate();
                            }
                            else {
                                sendBall(ball, id);
                                ((MainActivity)getContext()).showToast("send ball to : " + id);
                                removeBall(ball.m_id);
                                this.invalidate();
                            }
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
                rt = remotePhone.m_id;
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
        ball.m_name = "chengzhao";
        m_balls.add(ball);
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

    public void sendBall(Ball ball, String receiverId ) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("ballId", ball.m_id);
            jo.put("ballColor", ball.m_ballColor);
            jo.put("receiverId", receiverId);
            jo.put("isSendingBall", true);
            jo.put("id", m_id);
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
            jo.put("id", m_id);
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

    public String getPhoneId() {
        return m_id;
    }

    public void clearRemotePhoneInfo() {
        m_remotePhones.clear();
    }
}
